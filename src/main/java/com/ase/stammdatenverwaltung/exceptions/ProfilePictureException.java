package com.ase.stammdatenverwaltung.exceptions;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Base exception thrown when profile picture operations fail. Encapsulates errors that occur during
 * profile picture storage, retrieval, or deletion operations in MinIO.
 *
 * <p>Implements {@link ApplicationException} to provide structured error information to the global
 * exception handler.
 */
public abstract class ProfilePictureException extends RuntimeException
    implements ApplicationException {
  private final String userId;

  /**
   * Constructs a new ProfilePictureException with detailed error information.
   *
   * @param message descriptive error message
   * @param userId the ID of the user whose profile picture operation failed
   * @param cause the underlying cause exception
   */
  public ProfilePictureException(String message, String userId, Throwable cause) {
    super(message, cause);
    this.userId = userId;
  }

  /**
   * Constructs a new ProfilePictureException with detailed error information.
   *
   * @param message descriptive error message
   * @param userId the ID of the user whose profile picture operation failed
   */
  public ProfilePictureException(String message, String userId) {
    super(message);
    this.userId = userId;
  }

  /**
   * Gets the ID of the user for which the profile picture operation failed.
   *
   * @return the user ID
   */
  public String getUserId() {
    return userId;
  }

  @Override
  public HttpStatus getHttpStatus() {
    // Derived classes may override for specific status codes (e.g., 404 for not found)
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public String getErrorCategory() {
    return this.getClass().getSimpleName();
  }

  @Override
  public String getUserMessage() {
    return "Profile picture operation failed";
  }

  @Override
  public String getTechnicalMessage() {
    return getMessage();
  }

  @Override
  public Map<String, String> getContextMap() {
    Map<String, String> context = new HashMap<>();
    context.put("userId", userId);
    return context;
  }
}
