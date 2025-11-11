package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.exceptions.ProfilePictureDeletionException;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureRetrievalException;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureStorageException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for MinIO profile picture operations.
 *
 * <p>Maps custom ProfilePicture* exceptions to appropriate HTTP status codes and error responses.
 * Only registered if MinIO is enabled via configuration.
 */
@ControllerAdvice
@Slf4j
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
public class MinIOExceptionHandler {

  /**
   * Handles ProfilePictureRetrievalException.
   *
   * <p>Typically indicates the picture does not exist or a connection error occurred during
   * retrieval.
   *
   * @param ex the exception
   * @return 404 Not Found or 500 Internal Server Error depending on root cause
   */
  @ExceptionHandler(ProfilePictureRetrievalException.class)
  public ResponseEntity<Map<String, String>> handleProfilePictureRetrievalException(
      ProfilePictureRetrievalException ex) {
    log.error(
        "Profile picture retrieval failed for user ID: {} - {} ({})",
        ex.getUserId(),
        ex.getMessage(),
        ex.getClass().getSimpleName());
    log.debug("Profile picture retrieval failed", ex);

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    // If caused by "not found" (NoSuchKeyException), return 404
    if (ex.getCause() != null && ex.getCause().getClass().getSimpleName().contains("NoSuchKey")) {
      status = HttpStatus.NOT_FOUND;
    }

    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Profile picture retrieval failed");
    errorResponse.put("message", ex.getMessage());
    errorResponse.put("userId", ex.getUserId());

    return ResponseEntity.status(status).body(errorResponse);
  }

  /**
   * Handles ProfilePictureStorageException.
   *
   * <p>Indicates an error during picture upload, such as permission issues or storage unavailable.
   *
   * @param ex the exception
   * @return 500 Internal Server Error
   */
  @ExceptionHandler(ProfilePictureStorageException.class)
  public ResponseEntity<Map<String, String>> handleProfilePictureStorageException(
      ProfilePictureStorageException ex) {
    log.error(
        "Profile picture storage failed for user ID: {} - {} ({})",
        ex.getUserId(),
        ex.getMessage(),
        ex.getClass().getSimpleName());
    log.debug("Profile picture storage failed", ex);

    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Profile picture storage failed");
    errorResponse.put("message", ex.getMessage());
    errorResponse.put("userId", ex.getUserId());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Handles ProfilePictureDeletionException.
   *
   * <p>Indicates an error during picture deletion, such as permission issues or storage
   * unavailable.
   *
   * @param ex the exception
   * @return 500 Internal Server Error or 404 if picture not found
   */
  @ExceptionHandler(ProfilePictureDeletionException.class)
  public ResponseEntity<Map<String, String>> handleProfilePictureDeletionException(
      ProfilePictureDeletionException ex) {
    log.error(
        "Profile picture deletion failed for user ID: {} - {} ({})",
        ex.getUserId(),
        ex.getMessage(),
        ex.getClass().getSimpleName());
    log.debug("Profile picture deletion failed", ex);

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    // If caused by "not found", return 404; otherwise 500
    if (ex.getCause() != null && ex.getCause().getClass().getSimpleName().contains("NoSuchKey")) {
      status = HttpStatus.NOT_FOUND;
    }

    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Profile picture deletion failed");
    errorResponse.put("message", ex.getMessage());
    errorResponse.put("userId", ex.getUserId());

    return ResponseEntity.status(status).body(errorResponse);
  }

  /**
   * Handles IllegalArgumentException from input validation.
   *
   * <p>Thrown when file validation fails (size, content type, empty file, etc.).
   *
   * @param ex the exception
   * @return 400 Bad Request with validation error message
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    log.warn("Invalid input for profile picture operation: {}", ex.getMessage());

    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Invalid input");
    errorResponse.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handles IOException from file operations.
   *
   * <p>Thrown when reading uploaded file fails (e.g., file deleted mid-upload).
   *
   * @param ex the exception
   * @return 500 Internal Server Error
   */
  @ExceptionHandler(java.io.IOException.class)
  public ResponseEntity<Map<String, String>> handleIOException(java.io.IOException ex) {
    log.error(
        "File I/O error during profile picture operation: {} ({})",
        ex.getMessage(),
        ex.getClass().getSimpleName());
    log.debug("File I/O error during profile picture operation", ex);

    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "File read error");
    errorResponse.put("message", "Failed to read uploaded file");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
