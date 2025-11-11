package com.ase.stammdatenverwaltung.exceptions;

/**
 * Exception thrown when profile picture storage to MinIO fails. This indicates that the profile
 * picture could not be stored, potentially due to network issues, insufficient storage, or
 * permission problems.
 */
public class ProfilePictureStorageException extends ProfilePictureException {
  /**
   * Constructs a new ProfilePictureStorageException with detailed error information.
   *
   * @param message descriptive error message
   * @param userId the ID of the user for which storage failed
   * @param cause the underlying cause exception
   */
  public ProfilePictureStorageException(String message, String userId, Throwable cause) {
    super(message, userId, cause);
  }

  /**
   * Constructs a new ProfilePictureStorageException with detailed error information.
   *
   * @param message descriptive error message
   * @param userId the ID of the user for which storage failed
   */
  public ProfilePictureStorageException(String message, String userId) {
    super(message, userId);
  }
}
