package com.ase.stammdatenverwaltung.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Converts JWT tokens from Keycloak into Spring Security authentication tokens with proper role
 * extraction. Supports both realm roles and client-specific roles from Keycloak token structure.
 */
@Component
@Profile("prod")
public class KeycloakJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String REALM_ACCESS_CLAIM = "realm_access";
  private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
  private static final String ROLES_CLAIM = "roles";
  private static final String ROLE_PREFIX = "ROLE_";

  private final JwtSecurityProperties jwtProperties;

  /**
   * Creates a new Keycloak JWT authentication converter.
   *
   * @param jwtProperties configuration properties for JWT token processing
   */
  public KeycloakJwtAuthenticationConverter(JwtSecurityProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
  }

  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities);
  }

  /**
   * Extracts authorities from JWT token claims. Supports both realm roles and client-specific roles
   * from Keycloak.
   *
   * @param jwt The JWT token containing role claims
   * @return Collection of GrantedAuthority objects for Spring Security
   */
  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    var authorities = new ArrayList<GrantedAuthority>();

    // Extract realm roles: realm_access.roles -> ROLE_*
    if (jwtProperties.isExtractRealmRoles()) {
      extractRealmRoles(jwt, authorities);
    }

    // Extract client roles: resource_access.stammdatenverwaltung-api.roles -> ROLE_*
    if (jwtProperties.isExtractClientRoles()) {
      extractClientRoles(jwt, authorities);
    }

    return authorities;
  }

  /**
   * Extracts realm-level roles from the JWT token.
   *
   * @param jwt JWT token
   * @param authorities Collection to add authorities to
   */
  @SuppressWarnings("unchecked")
  private void extractRealmRoles(Jwt jwt, Collection<GrantedAuthority> authorities) {
    var realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
    if (realmAccess != null) {
      var roles = (Collection<String>) realmAccess.get(ROLES_CLAIM);
      if (roles != null) {
        authorities.addAll(
            roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .collect(Collectors.toList()));
      }
    }
  }

  /**
   * Extracts client-specific roles from the JWT token.
   *
   * @param jwt JWT token
   * @param authorities Collection to add authorities to
   */
  @SuppressWarnings("unchecked")
  private void extractClientRoles(Jwt jwt, Collection<GrantedAuthority> authorities) {
    var resourceAccess = jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM);
    if (resourceAccess != null) {
      var clientAccess = (Map<String, Object>) resourceAccess.get(jwtProperties.getClientId());
      if (clientAccess != null) {
        var roles = (Collection<String>) clientAccess.get(ROLES_CLAIM);
        if (roles != null) {
          authorities.addAll(
              roles.stream()
                  .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                  .collect(Collectors.toList()));
        }
      }
    }
  }
}
