package com.ase.stammdatenverwaltung.config;

import com.ase.stammdatenverwaltung.security.CustomAuthenticationEntryPoint;
import com.ase.stammdatenverwaltung.security.JwtAuthConverter;
import com.ase.stammdatenverwaltung.security.RoleAwareAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
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
public class SecurityConfig {

  /** Provides a centralized access denied handler for logging role information on auth failures */
  @Bean
  RoleAwareAccessDeniedHandler roleAwareAccessDeniedHandler() {
    return new RoleAwareAccessDeniedHandler();
  }

  /** Development security configuration with relaxed permissions */
  @Bean
  @Profile("dev")
  SecurityFilterChain devSecurityFilterChain(
      HttpSecurity http, RoleAwareAccessDeniedHandler accessDeniedHandler) throws Exception {
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new JwtAuthConverter());

    http.authorizeHttpRequests(
            authz ->
                authz
                    // Allow public access to these endpoints in development
                    .requestMatchers(
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    // Public API endpoints (adjust as needed)
                    .requestMatchers("/api/v1/public/**")
                    .permitAll()
                    // User endpoints
                    .requestMatchers("/api/v1/users/**")
                    .permitAll()
                    // Group endpoints
                    .requestMatchers("/api/v1/group/**")
                    .permitAll()
                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler))
        .csrf(
            csrf ->
                csrf.ignoringRequestMatchers(
                    "/h2-console/**", "/api/**")) // Ignore CSRF for H2 console and API endpoints
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));
    return http.build();
  }

  /**
   * Test security configuration with relaxed permissions. Similar to dev but optimized for
   * automated tests.
   */
  @Bean
  @Profile("test")
  SecurityFilterChain testSecurityFilterChain(
      HttpSecurity http, RoleAwareAccessDeniedHandler accessDeniedHandler) throws Exception {
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new JwtAuthConverter());

    http.authorizeHttpRequests(
            authz ->
                authz
                    // Allow public access to these endpoints in tests
                    .requestMatchers(
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    // Public API endpoints
                    .requestMatchers("/api/v1/public/**")
                    .permitAll()
                    // User endpoints
                    .requestMatchers("/api/v1/users/**")
                    .authenticated()
                    // Group endpoints
                    .requestMatchers("/api/v1/group/**")
                    .authenticated()
                    // Profile picture endpoints
                    .requestMatchers("/api/v1/profile-picture/**")
                    .authenticated()
                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler))
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Ignore CSRF for API endpoints
        // Support JWT (for API testing)
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

    return http.build();
  }

  /**
   * Production security configuration with strict JWT-only authentication and role-based access
   * control. All endpoints require valid JWT tokens from Keycloak with appropriate roles.
   *
   * <p>Based on documentation:
   * https://agile-software-engineering-25.github.io/documentation/service-definitions/base-data-and-course-management/auth-overview
   *
   * <p>In production, only the Keycloak instance is used for authentication (no basic auth). Users
   * must provide valid JWT tokens in the Authorization header for all protected endpoints.
   */
  @Bean
  @Profile("prod")
  SecurityFilterChain prodSecurityFilterChain(
      HttpSecurity http, RoleAwareAccessDeniedHandler accessDeniedHandler) throws Exception {
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new JwtAuthConverter());

    http.authorizeHttpRequests(
            authz ->
                authz
                    // Allow health endpoint access
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    // Permit Swagger UI and API docs
                    .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**")
                    .permitAll()
                    // Public API endpoints
                    .requestMatchers("/api/v1/public/**")
                    .permitAll()
                    // User endpoints require authenticated Keycloak JWT
                    // Role-based access control is enforced at the controller method level
                    // using @PreAuthorize annotations
                    .requestMatchers("/api/v1/users/**")
                    .authenticated()
                    // Profile picture endpoints require authenticated Keycloak JWT
                    // Role-based access control is enforced at the controller method level
                    // using @PreAuthorize annotations
                    .requestMatchers("/api/v1/profile-picture/**")
                    .authenticated()
                    // Group endpoints require authenticated Keycloak JWT
                    // Role-based access control is enforced at the controller method level
                    // using @PreAuthorize annotations
                    .requestMatchers("/api/v1/group/**")
                    .authenticated()
                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler))
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Ignore CSRF for API endpoints
        .headers(headers -> headers.frameOptions(frame -> frame.deny()))
        // JWT-only authentication in production
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

    return http.build();
  }
}
