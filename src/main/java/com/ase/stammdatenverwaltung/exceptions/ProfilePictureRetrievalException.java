package com.ase.stammdatenverwaltung.exceptions;

/**
 * Exception thrown when profile picture retrieval from MinIO fails. This indicates that the
 * requested user's profile picture could not be retrieved from object storage, potentially due to
 * network issues, storage unavailability, or the picture not existing.
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
}
