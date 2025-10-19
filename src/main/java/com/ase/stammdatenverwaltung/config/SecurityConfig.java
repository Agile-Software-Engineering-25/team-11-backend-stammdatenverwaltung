package com.ase.stammdatenverwaltung.config;

import com.ase.stammdatenverwaltung.security.JwtAuthConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class that provides profile-specific security settings. Defines different
 * security configurations for development and production environments with Keycloak OAuth2
 * integration.
 *
 * <p>Based on:
 * https://www.javacodegeeks.com/2025/07/spring-boot-keycloak-role-based-authorization.html
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  /**
   * Development security configuration with relaxed permissions and dual authentication support
   * (Basic Auth for development tools + JWT for API testing).
   */
  @Bean
  @Profile("dev")
  SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new JwtAuthConverter());

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
                    // User creation endpoints - temporarily public until JWT auth is re-enabled
                    .requestMatchers("/api/v1/users/**")
                    .permitAll()
                    // Admin endpoints require ADMIN role
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    // All other API endpoints require authentication
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .csrf(
            csrf ->
                csrf.ignoringRequestMatchers(
                    "/h2-console/**", "/api/**")) // Ignore CSRF for H2 console and API endpoints
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
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new JwtAuthConverter());

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
                    // User creation endpoints - temporarily public until JWT auth is re-enabled
                    .requestMatchers("/api/v1/users/**")
                    .permitAll()
                    // Admin endpoints require ADMIN role
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    // All other API endpoints require authentication
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Ignore CSRF for API endpoints
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
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new JwtAuthConverter());

    http.authorizeHttpRequests(
            authz ->
                authz
                    // Only allow health endpoint publicly in production
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    // Public API endpoints (adjust as needed)
                    .requestMatchers("/api/v1/public/**")
                    .permitAll()
                    // User creation endpoints - temporarily public until JWT auth is re-enabled
                    // TODO: add JWT auth protection if keycloak is integrated
                    .requestMatchers("/api/v1/users/**")
                    .permitAll()
                    // Admin endpoints require ADMIN role
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    // All other API endpoints require authentication
                    .requestMatchers("/api/**")
                    .authenticated()
                    // Permit all access to actuator endpoints except health
                    .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**")
                    .permitAll()
                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Ignore CSRF for API endpoints
        .headers(
            headers -> headers.frameOptions(frame -> frame.deny()) // Security hardening
            )
        // JWT-only authentication in production
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

    return http.build();
  }
}
