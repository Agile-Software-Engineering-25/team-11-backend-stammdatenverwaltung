package com.ase.stammdatenverwaltung.logging;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized logging helper for structured exception logging.
 *
 * <p>Provides consistent logging patterns across the application:
 *
 * <ul>
 *   <li><strong>Error level (ERROR)</strong>: Production errors requiring immediate attention (500,
 *       503, etc.)
 *   <li><strong>Warning level (WARN)</strong>: Client/auth errors (400, 401, 403, 404, etc.)
 *   <li><strong>Debug level (DEBUG)</strong>: Full exception stack traces and detailed context
 * </ul>
 *
 * <p>All logs include: - Error code and category for tracking - User message and technical message
 * - Contextual information (user ID, endpoint, method, etc.) - Root cause extraction for
 * cross-cutting error analysis
 *
 * <p>Usage:
 *
 * <pre>
 * ExceptionContext ctx = ExceptionContext.builder()
 *     .errorCode("USER_004")
 *     .errorCategory("Profile Picture Storage")
 *     .status(HttpStatus.INTERNAL_SERVER_ERROR)
 *     .userMessage("Failed to save profile picture")
 *     .technicalMessage("MinIO connection timeout")
 *     .withContext("userId", "user123")
 *     .withContext("endpoint", "/api/users/123/profile-picture")
 *     .cause(minioException)
 *     .build();
 *
 * LoggingHelper.log(ctx);
 * </pre>
 */
@Slf4j
public class LoggingHelper {

  private LoggingHelper() {
    // Utility class
  }

  /**
   * Logs an exception context with appropriate severity level based on HTTP status.
   *
   * <p>Logs ERROR for 500+ status codes, WARN for 400-499, and DEBUG for full stack traces.
   *
   * @param ctx the exception context containing error details
   */
  public static void log(ExceptionContext ctx) {
    if (ctx == null) {
      log.error("ExceptionContext was null in LoggingHelper.log()");
      return;
    }

    String baseMessage = formatBaseMessage(ctx);
    String contextDetails = formatContextDetails(ctx);

    // Choose log level based on HTTP status
    if (ctx.getStatus().is5xxServerError()) {
      log.error("{} | {}", baseMessage, contextDetails);
    } else if (ctx.getStatus().is4xxClientError()) {
      log.warn("{} | {}", baseMessage, contextDetails);
    } else {
      log.info("{} | {}", baseMessage, contextDetails);
    }

    // Always log full stack trace at debug level
    if (ctx.getCause() != null) {
      log.debug("Full exception details for error code {}", ctx.getErrorCode(), ctx.getCause());
    }
  }

  /**
   * Logs a security-related exception (authentication/authorization).
   *
   * <p>Security logs always use WARN level to alert operations team of auth failures without
   * exposing sensitive information in error logs.
   *
   * @param ctx the exception context
   * @param sensitiveFieldsToOmit field names to exclude from logging (e.g., "credentials")
   */
  public static void logSecurity(ExceptionContext ctx, String... sensitiveFieldsToOmit) {
    if (ctx == null) {
      log.warn("Security event: ExceptionContext was null");
      return;
    }

    String baseMessage = formatBaseMessage(ctx);
    String contextDetails = formatContextDetailsSanitized(ctx, sensitiveFieldsToOmit);

    log.warn("SECURITY: {} | {}", baseMessage, contextDetails);

    if (ctx.getCause() != null) {
      log.debug("Security exception details for {}", ctx.getErrorCode(), ctx.getCause());
    }
  }

  private static String formatBaseMessage(ExceptionContext ctx) {
    return String.format(
        "[%s - %s] HTTP %d | User: %s | Tech: %s",
        ctx.getErrorCode(),
        ctx.getErrorCategory(),
        ctx.getStatus().value(),
        ctx.getUserMessage(),
        ctx.getTechnicalMessage());
  }

  private static String formatContextDetails(ExceptionContext ctx) {
    StringBuilder sb = new StringBuilder();
    ctx.getContext()
        .forEach(
            (key, value) -> {
              if (sb.length() > 0) {
                sb.append(" | ");
              }
              sb.append(key).append("=").append(sanitizeValue(value));
            });
    return sb.toString();
  }

  private static String formatContextDetailsSanitized(
      ExceptionContext ctx, String... fieldsToOmit) {
    StringBuilder sb = new StringBuilder();
    ctx.getContext()
        .forEach(
            (key, value) -> {
              if (shouldOmitField(key, fieldsToOmit)) {
                return;
              }
              if (sb.length() > 0) {
                sb.append(" | ");
              }
              sb.append(key).append("=").append(sanitizeValue(value));
            });
    return sb.toString();
  }

  private static boolean shouldOmitField(String fieldName, String[] fieldsToOmit) {
    if (fieldsToOmit == null || fieldsToOmit.length == 0) {
      return false;
    }
    for (String field : fieldsToOmit) {
      if (fieldName.equalsIgnoreCase(field)) {
        return true;
      }
    }
    return false;
  }

  private static String sanitizeValue(String value) {
    final int maxLogValueLength = 200;
    if (value == null) {
      return "null";
    }
    // Truncate very long values to avoid log flooding
    if (value.length() > maxLogValueLength) {
      return value.substring(0, maxLogValueLength) + "...";
    }
    return value;
  }
}
