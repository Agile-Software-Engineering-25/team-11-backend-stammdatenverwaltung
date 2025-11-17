package com.ase.stammdatenverwaltung.exceptions;

import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to create a user in Keycloak that already exists.
 *
 * <p>This exception indicates a 409 Conflict response from the Keycloak API, which should be mapped
 * to an HTTP 409 Conflict response to the client.
 *
 * <p>Common causes:
 *
 * <ul>
 *   <li>Username or email already exists in Keycloak
 *   <li>Retry of a user creation request that previously succeeded
 *   <li>Race condition between multiple concurrent creation requests
 * </ul>
 */
public class KeycloakUserAlreadyExistsException extends RuntimeException
    implements ApplicationException {

  private final String username;
  private final String keycloakUserId;

  /**
   * Creates a new exception for a user that already exists in Keycloak.
   *
   * @param username the username that conflicts
   * @param keycloakUserId the existing Keycloak user ID, or null if unknown
   */
  public KeycloakUserAlreadyExistsException(String username, String keycloakUserId) {
    super(
        "User with username '"
            + username
            + "' already exists in Keycloak"
            + (keycloakUserId != null ? " (ID: " + keycloakUserId + ")" : ""));
    this.username = username;
    this.keycloakUserId = keycloakUserId;
  }

  /**
   * Creates a new exception for a user that already exists in Keycloak.
   *
   * @param username the username that conflicts
   */
  public KeycloakUserAlreadyExistsException(String username) {
    this(username, null);
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public String getErrorCode() {
    return "KEYCLOAK_CONFLICT_001";
  }

  @Override
  public String getErrorCategory() {
    return "Keycloak User Conflict";
  }

  @Override
  public String getUserMessage() {
    return "A user with this username already exists. Please use a different username.";
  }

  @Override
  public String getTechnicalMessage() {
    return getMessage();
  }

  @Override
  public Map<String, String> getContextMap() {
    Map<String, String> context = new java.util.HashMap<>();
    context.put("username", username);
    if (keycloakUserId != null) {
      context.put("keycloakUserId", keycloakUserId);
    }
    return context;
  }

  @Override
  public Throwable getCause() {
    return super.getCause();
  }

  public String getUsername() {
    return username;
  }

  public String getKeycloakUserId() {
    return keycloakUserId;
  }
}
