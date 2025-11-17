package com.ase.stammdatenverwaltung.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when profile picture deletion from MinIO fails. This indicates that the user's
 * profile picture could not be deleted, potentially due to network issues, storage unavailability,
 * or permission problems.
 */
public class ProfilePictureDeletionException extends ProfilePictureException {
  /**
   * Constructs a new ProfilePictureDeletionException with detailed error information.
   *
   * @param message descriptive error message
   * @param userId the ID of the user for which deletion failed
   * @param cause the underlying cause exception
   */
  public ProfilePictureDeletionException(String message, String userId, Throwable cause) {
    super(message, userId, cause);
  }

  /**
   * Constructs a new ProfilePictureDeletionException with detailed error information.
   *
   * @param message descriptive error message
   * @param userId the ID of the user for which deletion failed
   */
  public ProfilePictureDeletionException(String message, String userId) {
    super(message, userId);
  }

  @Override
  public String getErrorCode() {
    return "USER_003";
  }

  @Override
  public String getUserMessage() {
    return "Could not delete profile picture";
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
