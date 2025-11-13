package com.ase.stammdatenverwaltung.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service for publishing user deletion events to the Bitfrost message broker.
 *
 * <p>This service encapsulates the integration with Bitfrost, a message broker used for notifying
 * other services about user deletions. The service handles HTTP communication and error handling,
 * maintaining separation of concerns from the controller layer.
 *
 * <p>Configuration is provided through application properties:
 *
 * <ul>
 *   <li>bitfrost.api-url: Base URL of the Bitfrost API
 *   <li>bitfrost.service-name: Name of this service as registered in Bitfrost
 *   <li>bitfrost.topic-name: Topic name for user deletion events
 *   <li>bitfrost.project-secret: Secret token for API authentication
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BitfrostNotificationService {

  private static final int HTTP_SUCCESS_START = 200;
  private static final int HTTP_SUCCESS_END = 300;

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  @Value("${bitfrost.api-url:https://bitfrost.sau-portal.de/api/v1/messages/publish}")
  private String bitfrostApiUrl;

  @Value("${bitfrost.service-name:Stammdatenverwaltung}")
  private String serviceName;

  @Value("${bitfrost.topic-name:user:deletion}")
  private String topicName;

  @Value("${bitfrost.project-secret:#{null}}")
  private String projectSecret;

  /**
   * Publishes a user deletion event to the Bitfrost message broker.
   *
   * <p>This method sends a notification to Bitfrost whenever a user is deleted, allowing other
   * services to react accordingly (e.g., cleanup profiles, deactivate accounts). The operation is
   * synchronous and will log failures without propagating exceptions, ensuring deletion always
   * succeeds even if notification fails.
   *
   * <p><b>Error Handling Policy:</b> This service implements a "fire-and-forget" notification
   * pattern where failures are non-fatal. All exceptions, including {@link InterruptedException},
   * are caught and logged without propagation. Callers should treat the return value as
   * informational: {@code false} indicates the notification failed, but the primary operation (user
   * deletion) has already completed successfully. Callers cannot use the return value to determine
   * whether the primary operation succeeded.
   *
   * <p><b>Thread Interruption:</b> If the HTTP client is interrupted, this method restores the
   * thread's interrupt status via {@link Thread#interrupt()} to maintain contract compliance for
   * potential thread pool usage. However, the method returns {@code false} rather than propagating
   * the exception, allowing the calling thread to continue without throwing.
   *
   * @param userId The ID of the deleted user
   * @return true if notification was sent successfully, false if an error occurred
   */
  public boolean notifyUserDeletion(String userId) {
    if (projectSecret == null || projectSecret.isBlank()) {
      log.debug(
          "Bitfrost notification skipped for user {} - project secret not configured", userId);
      return true;
    }

    try {
      Map<String, Object> payload = new HashMap<>();
      payload.put("user-id", userId);
      String jsonPayload = objectMapper.writeValueAsString(payload);

      URI publishUri =
          UriComponentsBuilder.fromUriString(bitfrostApiUrl)
              .path("/{serviceName}/{topicName}")
              .buildAndExpand(serviceName, topicName)
              .toUri();

      HttpRequest bitfrostRequest =
          HttpRequest.newBuilder()
              .uri(publishUri)
              .header("authorization", "Executor " + serviceName + ":" + projectSecret)
              .header("content-type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
              .build();

      HttpResponse<String> response =
          httpClient.send(bitfrostRequest, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() >= HTTP_SUCCESS_START && response.statusCode() < HTTP_SUCCESS_END) {
        log.debug("Successfully notified Bitfrost of user deletion for user ID: {}", userId);
        return true;
      } else {
        log.warn(
            "Bitfrost notification failed for user {}: HTTP {} - {}",
            userId,
            response.statusCode(),
            response.body());
        return false;
      }
    } catch (InterruptedException e) {
      // Restore interrupt status: required by Java contract for interrupted threads.
      // This allows potential thread pool executors to correctly handle thread state,
      // even though we don't propagate the exception (notification is non-fatal).
      Thread.currentThread().interrupt();
      log.error(
          "Bitfrost notification interrupted for user {}: {}",
          userId,
          e.getClass().getSimpleName());
      log.debug("Bitfrost notification interrupted", e);
      return false;
    } catch (Exception e) {
      log.error(
          "Bitfrost notification failed for user {}: {} ({})",
          userId,
          e.getMessage(),
          e.getClass().getSimpleName());
      log.debug("Bitfrost notification error details", e);
      return false;
    }
  }
}
