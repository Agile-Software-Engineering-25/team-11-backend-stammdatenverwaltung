package com.ase.stammdatenverwaltung.logging;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Encapsulates contextual information about an exception for unified logging and response
 * generation.
 *
 * <p>Provides a consistent way to capture and communicate error details throughout the exception
 * handling pipeline:
 *
 * <ul>
 *   <li>HTTP status code and error category
 *   <li>User-facing error message and technical details
 *   <li>Context (user ID, endpoint, request method, etc.)
 *   <li>Root cause and exception chain information
 * </ul>
 *
 * <p>This context is used by {@link LoggingHelper} to emit structured logs and by exception
 * handlers to build consistent error responses.
 */
public class ExceptionContext {

  private final String errorCode;
  private final String errorCategory;
  private final HttpStatus status;
  private final String userMessage;
  private final String technicalMessage;
  private final Map<String, String> context;
  private final Throwable cause;
  private final Instant timestamp;

  ExceptionContext(Builder builder) {
    this.errorCode = builder.errorCode;
    this.errorCategory = builder.errorCategory;
    this.status = builder.status;
    this.userMessage = builder.userMessage;
    this.technicalMessage = builder.technicalMessage;
    this.context = builder.context;
    this.cause = builder.cause;
    this.timestamp = builder.timestamp;
  }

  /**
   * Creates a new builder for {@link ExceptionContext}.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets the error code.
   *
   * @return the error code
   */
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * Gets the error category.
   *
   * @return the error category
   */
  public String getErrorCategory() {
    return errorCategory;
  }

  /**
   * Gets the HTTP status.
   *
   * @return the HTTP status
   */
  public HttpStatus getStatus() {
    return status;
  }

  /**
   * Gets the user-facing message.
   *
   * @return the user message
   */
  public String getUserMessage() {
    return userMessage;
  }

  /**
   * Gets the technical message.
   *
   * @return the technical message
   */
  public String getTechnicalMessage() {
    return technicalMessage;
  }

  /**
   * Gets the context map.
   *
   * @return a copy of the context map
   */
  public Map<String, String> getContext() {
    return new HashMap<>(context);
  }

  /**
   * Gets the root cause exception.
   *
   * @return the cause exception, or null if none
   */
  public Throwable getCause() {
    return cause;
  }

  /**
   * Gets the timestamp.
   *
   * @return the timestamp
   */
  public Instant getTimestamp() {
    return timestamp;
  }

  /**
   * Builder for constructing {@link ExceptionContext} with fluent API.
   *
   * <p>Example usage:
   *
   * <pre>
   * ExceptionContext ctx = ExceptionContext.builder()
   *     .errorCode("AUTH_001")
   *     .errorCategory("Authentication")
   *     .status(HttpStatus.UNAUTHORIZED)
   *     .userMessage("Invalid credentials")
   *     .technicalMessage("BadCredentialsException: credentials mismatch")
   *     .withContext("endpoint", "/api/users/login")
   *     .withContext("method", "POST")
   *     .cause(badCredentialsException)
   *     .build();
   * </pre>
   */
  public static class Builder {

    private String errorCode;
    private String errorCategory;
    private HttpStatus status;
    private String userMessage;
    private String technicalMessage;
    private final Map<String, String> context = new HashMap<>();
    private Throwable cause;
    private final Instant timestamp = Instant.now();

    /**
     * Sets the error code.
     *
     * @param errorCode the error code
     * @return this builder
     */
    public Builder errorCode(String errorCode) {
      this.errorCode = errorCode;
      return this;
    }

    /**
     * Sets the error category.
     *
     * @param errorCategory the error category
     * @return this builder
     */
    public Builder errorCategory(String errorCategory) {
      this.errorCategory = errorCategory;
      return this;
    }

    /**
     * Sets the HTTP status.
     *
     * @param status the HTTP status
     * @return this builder
     */
    public Builder status(HttpStatus status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the user-facing message.
     *
     * @param userMessage the user message
     * @return this builder
     */
    public Builder userMessage(String userMessage) {
      this.userMessage = userMessage;
      return this;
    }

    /**
     * Sets the technical message.
     *
     * @param technicalMessage the technical message
     * @return this builder
     */
    public Builder technicalMessage(String technicalMessage) {
      this.technicalMessage = technicalMessage;
      return this;
    }

    /**
     * Adds a context key-value pair.
     *
     * @param key the context key
     * @param value the context value
     * @return this builder
     */
    public Builder withContext(String key, String value) {
      this.context.put(key, value);
      return this;
    }

    /**
     * Adds all entries from the provided context map.
     *
     * @param contextMap the context map to add
     * @return this builder
     */
    public Builder withContextMap(Map<String, String> contextMap) {
      this.context.putAll(contextMap);
      return this;
    }

    /**
     * Sets the root cause exception.
     *
     * @param cause the cause exception
     * @return this builder
     */
    public Builder cause(Throwable cause) {
      this.cause = cause;
      return this;
    }

    /**
     * Builds the {@link ExceptionContext} instance.
     *
     * @return the exception context
     * @throws IllegalStateException if required fields are not set
     */
    public ExceptionContext build() {
      if (errorCode == null || errorCategory == null || status == null || userMessage == null) {
        throw new IllegalStateException(
            "ExceptionContext requires errorCode, errorCategory, status, and userMessage");
      }
      if (technicalMessage == null) {
        technicalMessage = userMessage;
      }
      return new ExceptionContext(this);
    }
  }
}
