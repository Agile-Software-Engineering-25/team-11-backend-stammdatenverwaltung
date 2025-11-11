package com.ase.stammdatenverwaltung.controllers;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for authentication and authorization exceptions.
 *
 * <p>Logs all 401 (Unauthorized) and 403 (Forbidden) responses with detailed information about:
 *
 * <ul>
 *   <li>Endpoint that was accessed
 *   <li>Authentication status
 *   <li>Exception type and message
 *   <li>Timestamp of the failure
 * </ul>
 *
 * <p>This handler catches exceptions thrown by Spring Security during authentication and
 * authorization checks. Note: Spring Security's filter chain may also catch some authentication
 * failures before they reach the controller layer; see AuthenticationEntryPoint configuration for
 * those cases.
 */
@ControllerAdvice
@Slf4j
public class AuthenticationExceptionHandler {

  /**
   * Handles generic AuthenticationException and its subclasses.
   *
   * <p>Logs details about failed authentication attempts with the requesting endpoint and reason.
   *
   * @param ex the authentication exception
   * @param request the web request that triggered the exception
   * @return 401 Unauthorized response with error details
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(
      AuthenticationException ex, WebRequest request) {
    String requestUri = request.getDescription(false).replace("uri=", "");

    log.warn(
        "Authentication failed for endpoint: '{}' | Exception: {} | Message: '{}'",
        requestUri,
        ex.getClass().getSimpleName(),
        ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "Unauthorized");
    errorResponse.put("message", "Authentication failed");
    errorResponse.put("details", ex.getMessage());
    errorResponse.put("endpoint", requestUri);
    errorResponse.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /**
   * Handles BadCredentialsException specifically.
   *
   * <p>Logs invalid credentials attempts (wrong username/password). Does NOT log the actual
   * credentials for security reasons.
   *
   * @param ex the bad credentials exception
   * @param request the web request
   * @return 401 Unauthorized response
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
      BadCredentialsException ex, WebRequest request) {
    String requestUri = request.getDescription(false).replace("uri=", "");

    log.warn(
        "Bad credentials provided for endpoint: '{}' | Exception: {} | Message: '{}'",
        requestUri,
        ex.getClass().getSimpleName(),
        ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "Unauthorized");
    errorResponse.put("message", "Invalid credentials");
    errorResponse.put("endpoint", requestUri);
    errorResponse.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /**
   * Handles InsufficientAuthenticationException.
   *
   * <p>Occurs when an authenticated principal is not found in the security context, or the token is
   * missing/invalid.
   *
   * @param ex the exception
   * @param request the web request
   * @return 401 Unauthorized response
   */
  @ExceptionHandler(InsufficientAuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleInsufficientAuthenticationException(
      InsufficientAuthenticationException ex, WebRequest request) {
    String requestUri = request.getDescription(false).replace("uri=", "");

    log.warn(
        "Insufficient authentication for endpoint: '{}' | Exception: {} | Message: '{}'",
        requestUri,
        ex.getClass().getSimpleName(),
        ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "Unauthorized");
    errorResponse.put("message", "Missing or invalid authentication");
    errorResponse.put("endpoint", requestUri);
    errorResponse.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /**
   * Handles AccessDeniedException (403 Forbidden).
   *
   * <p>Occurs when an authenticated user lacks the required permissions/roles for an endpoint. Logs
   * the attempt to access restricted resources.
   *
   * @param ex the access denied exception
   * @param request the web request
   * @return 403 Forbidden response with error details
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
      AccessDeniedException ex, WebRequest request) {
    String requestUri = request.getDescription(false).replace("uri=", "");

    log.warn(
        "Access denied for endpoint: '{}' | Exception: {} | Message: '{}'",
        requestUri,
        ex.getClass().getSimpleName(),
        ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "Forbidden");
    errorResponse.put("message", "Access denied - insufficient permissions");
    errorResponse.put("details", ex.getMessage());
    errorResponse.put("endpoint", requestUri);
    errorResponse.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }
}
