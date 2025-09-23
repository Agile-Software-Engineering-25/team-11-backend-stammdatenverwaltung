package com.ase.stammdatenverwaltung.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller to demonstrate Keycloak authentication and authorization. This controller
 * provides endpoints to test different security scenarios and role-based access control.
 */
@RestController
@RequestMapping("/api/v1")
public class AuthTestController {

  /**
   * Public endpoint accessible without authentication. Useful for health checks and public
   * information.
   */
  @GetMapping("/public/hello")
  public ResponseEntity<String> publicHello() {
    return ResponseEntity.ok("Hello from public endpoint! No authentication required.");
  }

  /**
   * Protected endpoint requiring any valid authentication. Returns basic user information from the
   * JWT token.
   */
  @GetMapping("/hello")
  public ResponseEntity<Map<String, String>> hello(
      @AuthenticationPrincipal Jwt jwt, Principal principal) {
    Map<String, String> response = new HashMap<>();
    response.put("message", "Hello from protected endpoint!");
    response.put("user", principal.getName());
    if (jwt != null) {
      response.put("subject", jwt.getSubject());
      response.put("preferredUsername", jwt.getClaimAsString("preferred_username"));
    } else {
      response.put("subject", "n/a (basic auth)");
      response.put("preferredUsername", principal.getName());
    }
    return ResponseEntity.ok(response);
  }

  /** Admin-only endpoint requiring ADMIN role. Demonstrates role-based access control. */
  @GetMapping("/admin/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> adminUsers(
      @AuthenticationPrincipal Jwt jwt, Principal principal) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Admin endpoint accessed successfully!");
    response.put(
        "admin", jwt != null ? jwt.getClaimAsString("preferred_username") : principal.getName());

    // Safely extract realm roles
    if (jwt != null) {
      var realmAccess = jwt.getClaimAsMap("realm_access");
      if (realmAccess != null && realmAccess.get("roles") != null) {
        response.put("roles", realmAccess.get("roles"));
      }
    } else {
      response.put("roles", "[BASIC_AUTH_ROLE_CHECK]");
    }

    return ResponseEntity.ok(response);
  }

  /**
   * User endpoint requiring USER role. Demonstrates role-based access control for regular users.
   */
  @GetMapping("/user/profile")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Map<String, String>> userProfile(
      @AuthenticationPrincipal Jwt jwt, Principal principal) {
    Map<String, String> response = new HashMap<>();
    response.put("message", "User profile accessed successfully!");
    if (jwt != null) {
      response.put("user", jwt.getClaimAsString("preferred_username"));
      response.put("email", jwt.getClaimAsString("email"));
      response.put("firstName", jwt.getClaimAsString("given_name"));
      response.put("lastName", jwt.getClaimAsString("family_name"));
    } else {
      response.put("user", principal.getName());
    }
    return ResponseEntity.ok(response);
  }

  /**
   * Token info endpoint showing all JWT claims. Useful for debugging and understanding token
   * structure.
   */
  @GetMapping("/token-info")
  public ResponseEntity<Map<String, Object>> tokenInfo(
      @AuthenticationPrincipal Jwt jwt, Principal principal) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "JWT Token Information");
    if (jwt != null) {
      response.put("claims", jwt.getClaims());
      response.put("issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : "null");
      response.put("audience", jwt.getAudience() != null ? jwt.getAudience().toString() : "null");
      var issuedAt = jwt.getIssuedAt();
      response.put("issuedAt", issuedAt != null ? issuedAt.toString() : "null");
      var expiresAt = jwt.getExpiresAt();
      response.put("expiresAt", expiresAt != null ? expiresAt.toString() : "null");
    } else {
      response.put("info", "No JWT present (basic auth)");
      response.put("principal", principal != null ? principal.getName() : "anonymous");
    }
    return ResponseEntity.ok(response);
  }
}
