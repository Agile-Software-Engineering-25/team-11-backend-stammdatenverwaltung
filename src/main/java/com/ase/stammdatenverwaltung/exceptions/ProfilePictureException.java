package com.ase.stammdatenverwaltung.exceptions;

/**
 * Base exception thrown when profile picture operations fail. Encapsulates errors that occur during
 * profile picture storage, retrieval, or deletion operations in MinIO.
 */
public class ProfilePictureException extends RuntimeException {
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
}
