package com.ase.stammdatenverwaltung.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class that provides profile-specific security settings. Defines different
 * security configurations for development and production environments with Keycloak OAuth2
 * integration.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtSecurityProperties.class)
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

  private final KeycloakJwtAuthenticationConverter jwtConverter;

  /**
   * Creates a new security configuration with the provided JWT converter.
   *
   * @param jwtConverter converter for processing Keycloak JWT tokens
   */
  public SecurityConfig(KeycloakJwtAuthenticationConverter jwtConverter) {
    this.jwtConverter = jwtConverter;
  }

  /**
   * Development security configuration with relaxed permissions and dual authentication support
   * (Basic Auth for development tools + JWT for API testing).
   */
  @Bean
  @Profile("dev")
  SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authz ->
                authz
                    // Allow public access to these endpoints in development
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/api-docs/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    // Public API endpoints (adjust as needed)
                    .requestMatchers("/api/v1/public/**")
                    .permitAll()
                    // Admin endpoints require ADMIN role
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    // All other API endpoints require authentication
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
        .headers(
            headers -> headers.frameOptions(frame -> frame.sameOrigin()) // For H2 console
            )
        // Support both Basic Auth (for dev tools) and JWT (for API)
        .httpBasic(basic -> basic.realmName("Stammdatenverwaltung Development"))
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

    return http.build();
  }

  // In-memory users for development to allow Basic Auth without Keycloak
  @Bean
  @Profile("dev")
  UserDetailsService inMemoryUsers() {
    var user = User.withUsername("dev-user").password("{noop}dev-password").roles("USER").build();
    var admin =
        User.withUsername("dev-admin")
            .password("{noop}dev-password")
            .roles("ADMIN", "USER")
            .build();
    return new InMemoryUserDetailsManager(user, admin);
  }

  /**
   * Test security configuration with relaxed permissions and dual authentication support (Basic
   * Auth for testing + JWT for API testing). Similar to dev but optimized for automated tests.
   */
  @Bean
  @Profile("test")
  SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authz ->
                authz
                    // Allow public access to these endpoints in tests
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/api-docs/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**")
                    .permitAll()
                    // Public API endpoints
                    .requestMatchers("/api/v1/public/**")
                    .permitAll()
                    // Admin endpoints require ADMIN role
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    // All other API endpoints require authentication
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
        // Support both Basic Auth (for tests) and JWT (for API testing)
        .httpBasic(basic -> basic.realmName("Stammdatenverwaltung Test"))
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

    return http.build();
  }

  // In-memory users for testing to allow Basic Auth without Keycloak
  @Bean
  @Profile("test")
  UserDetailsService testInMemoryUsers() {
    var user = User.withUsername("dev-user").password("{noop}dev-password").roles("USER").build();
    var admin =
        User.withUsername("dev-admin")
            .password("{noop}dev-password")
            .roles("ADMIN", "USER")
            .build();
    return new InMemoryUserDetailsManager(user, admin);
  }

  /**
   * Production security configuration with strict JWT-only authentication and role-based access
   * control.
   */
  @Bean
  @Profile("prod")
  SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authz ->
                authz
                    // Only allow health endpoint publicly in production
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    // Public API endpoints (adjust as needed)
                    .requestMatchers("/api/v1/public/**")
                    .permitAll()
                    // Admin endpoints require ADMIN role
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    // All other API endpoints require authentication
                    .requestMatchers("/api/**")
                    .authenticated()
                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .headers(
            headers -> headers.frameOptions(frame -> frame.deny()) // Security hardening
            )
        // JWT-only authentication in production
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

    return http.build();
  }
}
