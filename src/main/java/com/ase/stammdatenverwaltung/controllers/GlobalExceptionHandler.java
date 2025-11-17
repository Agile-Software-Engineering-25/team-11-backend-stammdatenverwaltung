package com.ase.stammdatenverwaltung.controllers;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for all 400 (Bad Request) and 500 (Internal Server Error) exceptions.
 *
 * <p>Logs comprehensive information about client errors (4xx) and server errors (5xx) with:
 *
 * <ul>
 *   <li>WARN level: Endpoint, HTTP method, status code, exception type, and message
 *   <li>DEBUG level: Full stack trace for troubleshooting
 * </ul>
 *
 * <p>This handler provides a catch-all for exceptions not handled by more specific exception
 * handlers such as AuthenticationExceptionHandler (401) and MinIOExceptionHandler (custom errors).
 * Ensures consistent error responses across all endpoints and controllers.
 *
 * <p>Note: This handler uses the lowest precedence, allowing specific handlers to take priority
 * before falling back to this global handler.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles all uncaught exceptions from all controllers.
   *
   * <p>Determines appropriate HTTP status code based on exception type:
   *
   * <ul>
   *   <li>4xx errors (client errors) → 400 Bad Request if exact status unknown
   *   <li>5xx errors (server errors) → 500 Internal Server Error
   *   <li>Other unexpected exceptions → 500 Internal Server Error
   * </ul>
   *
   * <p>Logs with WARN level for quick identification and DEBUG level for full stack trace context.
   *
   * @param ex the exception thrown by a controller
   * @param request the HTTP request that triggered the exception
   * @return 400 or 500 response with error details
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAllExceptions(
      Exception ex, HttpServletRequest request) {

    String requestUri = request.getRequestURI();
    String httpMethod = request.getMethod();
    HttpStatus status = determineHttpStatus(ex);

    // WARN level: Exception info + endpoint details (no stacktrace)
    log.warn(
        "{} {} | HTTP Status: {} | Exception: {} | Message: '{}'",
        httpMethod,
        requestUri,
        status.value(),
        ex.getClass().getSimpleName(),
        ex.getMessage());

    // DEBUG level: Full stack trace for troubleshooting
    log.debug(
        "{} {} | HTTP Status: {} | Exception: {} | Full stacktrace:",
        httpMethod,
        requestUri,
        status.value(),
        ex.getClass().getSimpleName(),
        ex);

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", status.getReasonPhrase());
    errorResponse.put("status", status.value());
    errorResponse.put(
        "message",
        ex.getMessage() != null
            ? ex.getMessage()
            : "No error message available (" + ex.getClass().getSimpleName() + ")");
    errorResponse.put("exception", ex.getClass().getSimpleName());
    errorResponse.put("endpoint", requestUri);
    errorResponse.put("method", httpMethod);
    errorResponse.put("timestamp", Instant.now());

    return ResponseEntity.status(status).body(errorResponse);
  }

  /**
   * Determines the appropriate HTTP status code for the given exception.
   *
   * <p>Strategy:
   *
   * <ul>
   *   <li>IllegalArgumentException → 400 Bad Request (invalid input)
   *   <li>IllegalStateException → 500 Internal Server Error (programming error)
   *   <li>NullPointerException → 500 Internal Server Error (programming error)
   *   <li>All other exceptions → 500 Internal Server Error (safest default)
   * </ul>
   *
   * @param ex the exception to evaluate
   * @return the appropriate HttpStatus for this exception
   */
  private HttpStatus determineHttpStatus(Exception ex) {
    if (ex instanceof IllegalArgumentException) {
      return HttpStatus.BAD_REQUEST;
    }

    if (ex instanceof IllegalStateException || ex instanceof NullPointerException) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    // Default: treat all other exceptions as server errors
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
