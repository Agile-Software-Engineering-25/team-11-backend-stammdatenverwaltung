package com.ase.stammdatenverwaltung.config;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class that provides profile-specific security settings. Defines different
 * security configurations for development and production environments with Keycloak OAuth2
 * integration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

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
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthConverter())));

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
        .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
        .headers(
            headers -> headers.frameOptions(frame -> frame.deny()) // Security hardening
            )
        // JWT-only authentication in production
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthConverter())));

    return http.build();
  }

  /**
   * JWT Authentication Converter that extracts roles from Keycloak token claims and converts them
   * to Spring Security authorities.
   *
   * @return JwtAuthenticationConverter configured for Keycloak token structure
   */
  @Bean
  JwtAuthenticationConverter keycloakJwtAuthConverter() {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
    return converter;
  }

  /**
   * Extracts authorities from JWT token claims. Supports both realm roles and client-specific roles
   * from Keycloak.
   *
   * @param jwt The JWT token containing role claims
   * @return Collection of GrantedAuthority objects for Spring Security
   */
  @SuppressWarnings("unchecked")
  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    var authorities = new ArrayList<GrantedAuthority>();

    // 1) Extract realm roles: realm_access.roles -> ROLE_*
    var realmAccess = jwt.getClaimAsMap("realm_access");
    if (realmAccess != null) {
      var roles = (Collection<String>) realmAccess.get("roles");
      if (roles != null) {
        authorities.addAll(
            roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .collect(Collectors.toList()));
      }
    }

    // 2) Extract client roles for this API: resource_access.stammdatenverwaltung-api.roles ->
    // ROLE_*
    var resourceAccess = jwt.getClaimAsMap("resource_access");
    if (resourceAccess != null) {
      var api = (Map<String, Object>) resourceAccess.get("stammdatenverwaltung-api");
      if (api != null) {
        var roles = (Collection<String>) api.get("roles");
        if (roles != null) {
          authorities.addAll(
              roles.stream()
                  .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                  .collect(Collectors.toList()));
        }
      }
    }

    return authorities;
  }
}
