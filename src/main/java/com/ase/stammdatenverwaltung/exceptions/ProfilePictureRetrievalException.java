package com.ase.stammdatenverwaltung.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when profile picture retrieval from MinIO fails. This indicates that the
 * requested user's profile picture could not be retrieved from object storage, potentially due to
 * network issues, storage unavailability, or the picture not existing.
 *
 * <p>Status code determination:
 *
 * <ul>
 *   <li>404 Not Found if the picture does not exist (cause: NoSuchKeyException)
 *   <li>500 Internal Server Error for other retrieval failures
 * </ul>
 */
public class ProfilePictureRetrievalException extends ProfilePictureException {
  /**
   * Constructs a new ProfilePictureRetrievalException with detailed error information.
   *
   * @param message descriptive error message
   * @param userId the ID of the user whose profile picture could not be retrieved
   * @param cause the underlying cause exception
   */
  public ProfilePictureRetrievalException(String message, String userId, Throwable cause) {
    super(message, userId, cause);
  }

  /**
   * Constructs a new ProfilePictureRetrievalException with detailed error information.
   *
   * @param message descriptive error message
   * @param userId the ID of the user whose profile picture could not be retrieved
   */
  public ProfilePictureRetrievalException(String message, String userId) {
    super(message, userId);
  }

  @Override
  public String getErrorCode() {
    return "USER_001";
  }

  @Override
  public String getUserMessage() {
    return "Could not retrieve profile picture";
  }

  @Override
  public HttpStatus getHttpStatus() {
    // Return 404 if not found, otherwise 500
    if (getCause() != null && getCause().getClass().getSimpleName().contains("NoSuchKey")) {
      return HttpStatus.NOT_FOUND;
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
