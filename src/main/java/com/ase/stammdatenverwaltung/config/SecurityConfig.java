package com.ase.stammdatenverwaltung.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class that provides profile-specific security settings. Defines different
 * security configurations for development and production environments.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtSecurityProperties.class)
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

  @Autowired private Environment environment;

  @Autowired(required = false)
  private KeycloakJwtAuthenticationConverter jwtConverter;

  /**
   * Creates a new security configuration. The JWT converter is only available in production
   * profile.
   */
  public SecurityConfig() {
    // Constructor for dependency injection
  }

  /** Logs the active security configuration at startup for clarity. */
  @jakarta.annotation.PostConstruct
  public void logSecurityMode() {
    if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of("dev"))) {
      LOG.info("🔓 DEV MODE: Using Basic Authentication with in-memory users");
      LOG.info("   Users: dev-user/dev-password (USER), dev-admin/dev-password (ADMIN)");
      LOG.info("   No external dependencies required - perfect for local development!");
    } else if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of("test"))) {
      LOG.info("🧪 TEST MODE: Using Basic Authentication with in-memory users");
      LOG.info("   Optimized for automated testing with no external dependencies");
    } else if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of("prod"))) {
      LOG.info("🔒 PROD MODE: Using Keycloak JWT authentication");
      LOG.info("   Requires KEYCLOAK_ISSUER_URI and KEYCLOAK_API_AUDIENCE environment variables");
      LOG.warn("   Do NOT use production profile for local development!");
    } else {
      LOG.warn("⚠️  No recognized profile active - using default Spring Security configuration");
    }
  }

  /** Extracts common authorization rules shared across all profiles. */
  private void configureCommonAuthorization(
      org.springframework.security.config.annotation.web.configurers
                      .AuthorizeHttpRequestsConfigurer<
                  HttpSecurity>
              .AuthorizationManagerRequestMatcherRegistry
          authz) {
    authz
        // Public API endpoints
        .requestMatchers("/api/v1/public/**")
        .permitAll()
        // Admin endpoints require ADMIN role
        .requestMatchers("/api/v1/admin/**")
        .hasRole("ADMIN")
        // All other API endpoints require authentication
        .requestMatchers("/api/**")
        .authenticated();
  }

  /**
   * Development security configuration with relaxed permissions and Basic Auth only. No external
   * dependencies required - perfect for local development.
   */
  @Bean
  @Profile("dev")
  SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authz -> {
              // Allow public access to development tools
              authz
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
                  .permitAll();
              // Apply common authorization rules
              configureCommonAuthorization(authz);
              authz.anyRequest().authenticated();
            })
        .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
        .headers(
            headers -> headers.frameOptions(frame -> frame.sameOrigin()) // For H2 console
            )
        // Basic Auth only for development
        .httpBasic(basic -> basic.realmName("Stammdatenverwaltung Development"));

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
   * Test security configuration with relaxed permissions and Basic Auth only. Optimized for
   * automated tests with no external dependencies.
   */
  @Bean
  @Profile("test")
  SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authz -> {
              // Allow public access to testing tools
              authz
                  .requestMatchers("/actuator/**")
                  .permitAll()
                  .requestMatchers("/swagger-ui/**")
                  .permitAll()
                  .requestMatchers("/swagger-ui.html")
                  .permitAll()
                  .requestMatchers("/api-docs/**")
                  .permitAll()
                  .requestMatchers("/v3/api-docs/**")
                  .permitAll();
              // Apply common authorization rules
              configureCommonAuthorization(authz);
              authz.anyRequest().authenticated();
            })
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
        // Basic Auth only for testing
        .httpBasic(basic -> basic.realmName("Stammdatenverwaltung Test"));

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
   * control. Requires Keycloak server for JWT validation.
   */
  @Bean
  @Profile("prod")
  SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authz -> {
              // Only allow health endpoint publicly in production
              authz.requestMatchers("/actuator/health").permitAll();
              // Apply common authorization rules
              configureCommonAuthorization(authz);
              // All other endpoints require authentication
              authz.anyRequest().authenticated();
            })
        .headers(
            headers -> headers.frameOptions(frame -> frame.deny()) // Security hardening
            )
        // JWT-only authentication in production
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

    return http.build();
  }
}
