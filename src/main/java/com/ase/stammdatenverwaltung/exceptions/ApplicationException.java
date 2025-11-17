package com.ase.stammdatenverwaltung.exceptions;

import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Base interface for all application-specific exceptions.
 *
 * <p>Provides a consistent contract for domain exceptions to communicate structured error
 * information to the exception handler:
 *
 * <ul>
 *   <li>HTTP status code appropriate for the error
 *   <li>Error code for client error tracking (e.g., "USER_001", "DEPT_003")
 *   <li>Error category for grouping related errors
 *   <li>User-facing message (safe for client display)
 *   <li>Technical message for logging (implementation details)
 *   <li>Contextual data (user ID, resource ID, etc.)
 * </ul>
 *
 * <p>All application exceptions should implement this interface to ensure consistent logging and
 * error responses throughout the application.
 *
 * <p>Note: Implementing classes must extend RuntimeException or a similar Throwable subclass to
 * work with Spring's @ExceptionHandler.
 *
 * <p>Example implementation:
 *
 * <pre>
 * public class EntityNotFoundException extends RuntimeException implements ApplicationException {
 *   private final String resourceType;
 *   private final String resourceId;
 *
 *   public EntityNotFoundException(String resourceType, String resourceId) {
 *     super("Resource not found: " + resourceType + " " + resourceId);
 *     this.resourceType = resourceType;
 *     this.resourceId = resourceId;
 *   }
 *
 *   {@code @Override}
 *   public HttpStatus getHttpStatus() {
 *     return HttpStatus.NOT_FOUND;
 *   }
 *
 *   {@code @Override}
 *   public String getErrorCode() {
 *     return "NOT_FOUND_001";
 *   }
 *
 *   {@code @Override}
 *   public String getErrorCategory() {
 *     return "Entity Not Found";
 *   }
 *
 *   {@code @Override}
 *   public String getUserMessage() {
 *     return "The requested " + resourceType + " does not exist";
 *   }
 *
 *   {@code @Override}
 *   public String getTechnicalMessage() {
 *     return getMessage();
 *   }
 *
 *   {@code @Override}
 *   public Map{@code <}String, String{@code >} getContextMap() {
 *     return Map.of("resourceType", resourceType, "resourceId", resourceId);
 *   }
 * }
 * </pre>
 */
public interface ApplicationException {

  /**
   * Gets the HTTP status code appropriate for this error.
   *
   * @return the HTTP status (e.g., 400 for validation, 404 for not found, 500 for server error)
   */
  HttpStatus getHttpStatus();

  /**
   * Gets the error code for tracking and grouping.
   *
   * <p>Examples: "USER_001", "DEPT_003", "VALIDATION_002"
   *
   * @return the error code
   */
  String getErrorCode();

  /**
   * Gets the error category for classifying the error.
   *
   * <p>Examples: "User Management", "Department Management", "Input Validation"
   *
   * @return the category name
   */
  String getErrorCategory();

  /**
   * Gets the user-facing error message safe for client display.
   *
   * <p>Should be generic and not leak implementation details.
   *
   * <p>Example: "The requested user does not exist"
   *
   * @return the user-facing message
   */
  String getUserMessage();

  /**
   * Gets the technical error message for logging.
   *
   * <p>May include implementation details and stack trace context.
   *
   * <p>Example: "User record with ID 'abc123' not found in database"
   *
   * @return the technical message
   */
  String getTechnicalMessage();

  /**
   * Gets contextual data for the exception (user ID, resource ID, etc.).
   *
   * <p>This data will be included in logs and error responses.
   *
   * @return map of context key-value pairs
   */
  Map<String, String> getContextMap();

  /**
   * Gets the root cause exception if any.
   *
   * @return the cause exception, or null if none
   */
  Throwable getCause();
}
