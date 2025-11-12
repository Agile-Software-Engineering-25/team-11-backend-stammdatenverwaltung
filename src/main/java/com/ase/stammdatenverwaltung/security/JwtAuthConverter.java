// based on this tutorial:
// https://www.javacodegeeks.com/2025/07/spring-boot-keycloak-role-based-authorization.html

package com.ase.stammdatenverwaltung.security;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converts JWT tokens from Keycloak into Spring Security authorities. Extracts roles from the
 * "groups" claim in the JWT token.
 */
public class JwtAuthConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  @Override
  public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
    var roles = jwt.getClaimAsStringList("groups");

    // you can check the roles here if you want to
    // for (String role : roles) {
    //   System.out.println("Role from JWT: " + role);
    // }
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toList());
  }
}
