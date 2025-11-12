// based on this tutorial:
// https://www.javacodegeeks.com/2025/07/spring-boot-keycloak-role-based-authorization.html

package com.ase.stammdatenverwaltung.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converts JWT tokens from Keycloak into Spring Security authorities. Extracts roles from the
 * "groups" claim in the JWT token.
 */
@Slf4j
public class JwtAuthConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  @Override
  public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
    var roles = jwt.getClaimAsStringList("groups");

    log.debug("JWT Subject (user ID): {}", jwt.getSubject());
    log.debug("JWT Issuer: {}", jwt.getIssuer());
    log.debug("JWT Client ID (azp): {}", jwt.getClaimAsString("azp"));

    if (roles == null || roles.isEmpty()) {
      log.warn("No 'groups' claim found in JWT token. User may not have any roles assigned.");
      log.debug("Available JWT claims: {}", jwt.getClaims().keySet());
      return new ArrayList<>();
    }

    log.debug("Raw roles from JWT 'groups' claim: {}", roles);

    Collection<GrantedAuthority> authorities =
        roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());

    log.debug(
        "Converted authorities for Spring Security: {}",
        authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

    return authorities;
  }
}
