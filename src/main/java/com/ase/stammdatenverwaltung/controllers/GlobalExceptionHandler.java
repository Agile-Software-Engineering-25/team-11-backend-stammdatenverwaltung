package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.exceptions.ProfilePictureException;
import com.ase.stammdatenverwaltung.logging.ExceptionContext;
import com.ase.stammdatenverwaltung.logging.LoggingHelper;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Unified global exception handler for the entire application.
 *
 * <p>Consolidates exception handling across authentication, authorization, domain-specific, and
 * general application errors into a single {@code @ControllerAdvice}.
 *
 * <p><strong>Exception Categories:</strong>
 *
 * <ul>
 *   <li><strong>Authentication (401)</strong>: Missing, invalid, or expired credentials
 *   <li><strong>Authorization (403)</strong>: Valid credentials but insufficient permissions
 *   <li><strong>Domain Errors (4xx/5xx)</strong>: Application-specific logic failures
 *   <li><strong>System Errors (500)</strong>: Unexpected runtime exceptions
 * </ul>
 *
 * <p><strong>Logging Principles:</strong> - All exceptions logged via {@link LoggingHelper} with
 * consistent structure - ERROR level for 500+ (server errors), WARN level for 400+ (client errors)
 * - Sensitive information (credentials) never logged - Full stack traces logged at DEBUG level only
 *
 * <p><strong>Response Format:</strong> All responses return JSON with: - {@code error}: Error code
 * (e.g., "AUTH_001", "USER_004") - {@code message}: User-facing error message - {@code details}:
 * Additional technical context if available - {@code timestamp}: Response timestamp (Unix
 * milliseconds) - {@code path}: Request endpoint
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // ==================== Authentication Errors (401) ====================

  /**
   * Handles generic AuthenticationException and its subclasses.
   *
   * @param ex the authentication exception
   * @param request the web request
   * @return 401 Unauthorized response
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(
      AuthenticationException ex, WebRequest request) {
    String requestUri = extractUri(request);

    ExceptionContext ctx =
        ExceptionContext.builder()
            .errorCode("AUTH_001")
            .errorCategory("Authentication")
            .status(HttpStatus.UNAUTHORIZED)
            .userMessage("Authentication failed")
            .technicalMessage(
                String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage()))
            .withContext("endpoint", requestUri)
            .withContext("method", "unknown")
            .cause(ex)
            .build();

    LoggingHelper.logSecurity(ctx, "password", "credentials", "token");

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse(ctx, requestUri));
  }

  /**
   * Handles BadCredentialsException specifically (wrong username/password).
   *
   * @param ex the bad credentials exception
   * @param request the web request
   * @return 401 Unauthorized response
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
      BadCredentialsException ex, WebRequest request) {
    String requestUri = extractUri(request);

    ExceptionContext ctx =
        ExceptionContext.builder()
            .errorCode("AUTH_002")
            .errorCategory("Authentication - Bad Credentials")
            .status(HttpStatus.UNAUTHORIZED)
            .userMessage("Invalid credentials provided")
            .technicalMessage("BadCredentialsException: credentials mismatch")
            .withContext("endpoint", requestUri)
            .cause(ex)
            .build();

    LoggingHelper.logSecurity(ctx, "password", "credentials");

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse(ctx, requestUri));
  }

  /**
   * Handles InsufficientAuthenticationException (missing/invalid token).
   *
   * @param ex the exception
   * @param request the web request
   * @return 401 Unauthorized response
   */
  @ExceptionHandler(InsufficientAuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleInsufficientAuthenticationException(
      InsufficientAuthenticationException ex, WebRequest request) {
    String requestUri = extractUri(request);

    ExceptionContext ctx =
        ExceptionContext.builder()
            .errorCode("AUTH_003")
            .errorCategory("Authentication - Insufficient")
            .status(HttpStatus.UNAUTHORIZED)
            .userMessage("Missing or invalid authentication")
            .technicalMessage("InsufficientAuthenticationException: " + ex.getMessage())
            .withContext("endpoint", requestUri)
            .cause(ex)
            .build();

    LoggingHelper.logSecurity(ctx, "token", "bearer");

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse(ctx, requestUri));
  }

  // ==================== Domain-Specific Errors ====================

  /**
   * Handles EntityNotFoundException (resource not found).
   *
   * @param ex the entity not found exception
   * @param request the web request
   * @return 404 Not Found response
   */
  @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
      jakarta.persistence.EntityNotFoundException ex, WebRequest request) {
    String requestUri = extractUri(request);

    ExceptionContext ctx =
        ExceptionContext.builder()
            .errorCode("NOT_FOUND_001")
            .errorCategory("Entity Not Found")
            .status(HttpStatus.NOT_FOUND)
            .userMessage("The requested resource does not exist")
            .technicalMessage("EntityNotFoundException: " + ex.getMessage())
            .withContext("endpoint", requestUri)
            .cause(ex)
            .build();

    LoggingHelper.log(ctx);

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(ctx, requestUri));
  }

  /**
   * Handles ProfilePictureException and its subtypes (retrieval, storage, deletion).
   *
   * @param ex the profile picture exception
   * @param request the web request
   * @return Appropriate HTTP response (404 if not found, 500 otherwise)
   */
  @ExceptionHandler(ProfilePictureException.class)
  @ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
  public ResponseEntity<Map<String, Object>> handleProfilePictureException(
      ProfilePictureException ex, WebRequest request) {
    String requestUri = extractUri(request);

    ExceptionContext ctx =
        ExceptionContext.builder()
            .errorCode(ex.getErrorCode())
            .errorCategory(ex.getErrorCategory())
            .status(ex.getHttpStatus())
            .userMessage(ex.getUserMessage())
            .technicalMessage(ex.getTechnicalMessage())
            .withContextMap(ex.getContextMap())
            .withContext("endpoint", requestUri)
            .cause(ex)
            .build();

    LoggingHelper.log(ctx);

    return ResponseEntity.status(ex.getHttpStatus()).body(buildErrorResponse(ctx, requestUri));
  }

  // ==================== Input Validation Errors (400) ====================

  /**
   * Handles IllegalArgumentException from input validation.
   *
   * @param ex the exception
   * @param request the web request
   * @return 400 Bad Request
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    String requestUri = extractUri(request);

    ExceptionContext ctx =
        ExceptionContext.builder()
            .errorCode("VALIDATION_001")
            .errorCategory("Input Validation")
            .status(HttpStatus.BAD_REQUEST)
            .userMessage("Invalid input provided")
            .technicalMessage("IllegalArgumentException: " + ex.getMessage())
            .withContext("endpoint", requestUri)
            .cause(ex)
            .build();

    LoggingHelper.log(ctx);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildErrorResponse(ctx, requestUri));
  }

  // ==================== File I/O Errors (500) ====================

  /**
   * Handles IOException from file operations.
   *
   * @param ex the exception
   * @param request the web request
   * @return 500 Internal Server Error
   */
  @ExceptionHandler(java.io.IOException.class)
  public ResponseEntity<Map<String, Object>> handleIOException(
      java.io.IOException ex, WebRequest request) {
    String requestUri = extractUri(request);

    ExceptionContext ctx =
        ExceptionContext.builder()
            .errorCode("IO_001")
            .errorCategory("File I/O Error")
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .userMessage("File operation failed")
            .technicalMessage("IOException: " + ex.getMessage())
            .withContext("endpoint", requestUri)
            .cause(ex)
            .build();

    LoggingHelper.log(ctx);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(buildErrorResponse(ctx, requestUri));
  }

  // ==================== Unexpected Errors (500) ====================

  /**
   * Catches all unhandled exceptions as a fallback.
   *
   * @param ex the exception
   * @param request the web request
   * @return 500 Internal Server Error
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneralException(
      Exception ex, WebRequest request) {
    String requestUri = extractUri(request);

    ExceptionContext ctx =
        ExceptionContext.builder()
            .errorCode("SYS_001")
            .errorCategory("Unexpected System Error")
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .userMessage("An unexpected error occurred")
            .technicalMessage(
                String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage()))
            .withContext("endpoint", requestUri)
            .cause(ex)
            .build();

    LoggingHelper.log(ctx);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(buildErrorResponse(ctx, requestUri));
  }

  // ==================== Helper Methods ====================

  /**
   * Builds a standardized error response body.
   *
   * @param ctx the exception context
   * @param requestUri the request endpoint
   * @return error response map
   */
  private Map<String, Object> buildErrorResponse(ExceptionContext ctx, String requestUri) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", ctx.getErrorCode());
    response.put("message", ctx.getUserMessage());
    response.put("details", ctx.getTechnicalMessage());
    response.put("category", ctx.getErrorCategory());
    response.put("path", requestUri);
    response.put("timestamp", System.currentTimeMillis());

    // Include context fields if not empty
    if (!ctx.getContext().isEmpty()) {
      response.put("context", ctx.getContext());
    }

    return response;
  }

  /**
   * Extracts the request URI from WebRequest.
   *
   * @param request the web request
   * @return the request URI
   */
  private String extractUri(WebRequest request) {
    return request.getDescription(false).replace("uri=", "");
  }
}
