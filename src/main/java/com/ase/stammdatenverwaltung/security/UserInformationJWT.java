package com.ase.stammdatenverwaltung.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Helper class to extract user information from JWT tokens. Provides convenient static methods to
 * access authenticated user data including user ID, email, first/last names, and roles.
 *
 * <p>All roles are collected from multiple sources in the JWT:
 *
 * <ul>
 *   <li>groups claim
 *   <li>realm_access.roles
 *   <li>resource_access.account.roles
 * </ul>
 *
 * <p>This class provides a consistent interface for accessing user information across the
 * application without needing direct access to the JWT token.
 */
public class UserInformationJWT {

  private static final Logger LOG = LoggerFactory.getLogger(UserInformationJWT.class);

  /** Private constructor to prevent instantiation of utility class. */
  private UserInformationJWT() {
    throw new UnsupportedOperationException(
        "This is a utility class and cannot be instantiated");
  }

  /**
   * Get the current JWT token from the security context
   *
   * @return JWT token or null if not authenticated
   */
  private static Jwt getCurrentJwt() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication instanceof JwtAuthenticationToken) {
      return ((JwtAuthenticationToken) authentication).getToken();
    }

    return null;
  }

  /**
   * Get the user ID from the JWT subject claim
   *
   * @return User ID or null if not available
   */
  public static String getUserId() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getSubject() : null;
  }

  /**
   * Get the user's email address from the JWT
   *
   * @return Email or null if not available
   */
  public static String getEmail() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString("email") : null;
  }

  /**
   * Get the username from the JWT preferred_username claim
   *
   * @return Username or null if not available
   */
  public static String getUsername() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString("preferred_username") : null;
  }

  /**
   * Get the user's first name from the JWT given_name claim
   *
   * @return First name or null if not available
   */
  public static String getFirstName() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString("given_name") : null;
  }

  /**
   * Get the user's last name from the JWT family_name claim
   *
   * @return Last name or null if not available
   */
  public static String getLastName() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString("family_name") : null;
  }

  /**
   * Get all roles and groups of the user from multiple JWT claim sources. This method combines
   * roles from:
   *
   * <ul>
   *   <li>"groups" claim - direct group memberships
   *   <li>"realm_access.roles" - realm-level roles from Keycloak
   *   <li>"resource_access.account.roles" - client-specific roles
   * </ul>
   *
   * <p>Duplicates are automatically removed.
   *
   * @return List of all unique roles/groups or empty list if none found
   */
  public static List<String> getRoles() {
    Jwt jwt = getCurrentJwt();
    if (jwt == null) {
      return List.of();
    }

    List<String> allRoles = new ArrayList<>();

    // Collect from "groups" claim
    addClaimToRoles(allRoles, jwt.getClaimAsStringList("groups"));

    // Collect from realm_access.roles
    addRealmAccessRoles(allRoles, jwt);

    // Collect from resource_access.account.roles
    addResourceAccessRoles(allRoles, jwt);

    // Return unique roles
    return allRoles.stream().distinct().toList();
  }

  /**
   * Helper method to add roles from the "groups" claim.
   *
   * @param allRoles List to accumulate roles into
   * @param groups The groups claim from JWT
   */
  private static void addClaimToRoles(List<String> allRoles, List<String> groups) {
    if (groups != null) {
      allRoles.addAll(groups);
    }
  }

  /**
   * Helper method to add roles from realm_access claim.
   *
   * @param allRoles List to accumulate roles into
   * @param jwt The JWT token
   */
  private static void addRealmAccessRoles(List<String> allRoles, Jwt jwt) {
    try {
      Map<String, Object> realmAccess = jwt.getClaim("realm_access");
      if (realmAccess != null && realmAccess.get("roles") instanceof List) {
        @SuppressWarnings("unchecked")
        List<String> realmRoles = (List<String>) realmAccess.get("roles");
        if (realmRoles != null) {
          allRoles.addAll(realmRoles);
        }
      }
    } catch (ClassCastException | NullPointerException e) {
      LOG.debug(
          "Failed to parse realm_access.roles from JWT. Token may have unexpected structure.", e);
    }
  }

  /**
   * Helper method to add roles from resource_access.account claim.
   *
   * @param allRoles List to accumulate roles into
   * @param jwt The JWT token
   */
  private static void addResourceAccessRoles(List<String> allRoles, Jwt jwt) {
    try {
      Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
      if (resourceAccess != null && resourceAccess.get("account") instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> accountAccess = (Map<String, Object>) resourceAccess.get("account");
        if (accountAccess != null && accountAccess.get("roles") instanceof List) {
          @SuppressWarnings("unchecked")
          List<String> accountRoles = (List<String>) accountAccess.get("roles");
          if (accountRoles != null) {
            allRoles.addAll(accountRoles);
          }
        }
      }
    } catch (ClassCastException | NullPointerException e) {
      LOG.debug(
          "Failed to parse resource_access.account.roles from JWT. Token may have unexpected structure.",
          e);
    }
  }

  /**
   * Check if the user has a specific role or group membership. The comparison is case-insensitive
   * to handle potential Keycloak variations.
   *
   * <p>Searches through all available roles from multiple JWT claim sources (groups,
   * realm_access.roles, and resource_access.account.roles).
   *
   * @param role The role or group name to check (e.g., "HVS-Admin", "Area-3.Team-11.Read.User")
   * @return true if user has the role, false otherwise
   */
  public static boolean hasRole(String role) {
    if (role == null) {
      return false;
    }

    List<String> roles = getRoles();
    return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
  }

  /**
   * Get a custom claim from the JWT by name
   *
   * @param claimName Name of the claim to retrieve
   * @return Claim value or null if not available
   */
  public static Object getClaim(String claimName) {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaim(claimName) : null;
  }

  /**
   * Get a custom claim from the JWT as a String
   *
   * @param claimName Name of the claim to retrieve
   * @return Claim value as String or null if not available
   */
  public static String getClaimAsString(String claimName) {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString(claimName) : null;
  }

  /**
   * Check if a user is currently authenticated with a valid JWT token
   *
   * @return true if authenticated, false otherwise
   */
  public static boolean isAuthenticated() {
    return getCurrentJwt() != null;
  }
}
