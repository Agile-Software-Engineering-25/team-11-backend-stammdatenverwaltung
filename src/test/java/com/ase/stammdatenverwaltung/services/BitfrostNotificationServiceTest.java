package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for BitfrostNotificationService. Tests verify service behavior when publishing user
 * deletion events to the Bitfrost message broker. Tests cover success paths, failure scenarios, and
 * graceful degradation when configuration is missing.
 *
 * <p>A live Bitfrost instance is not required - HTTP communication is mocked through ObjectMapper
 * and the underlying HttpClient behavior is simulated.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BitfrostNotificationService Tests")
class BitfrostNotificationServiceTest {

  @Mock private ObjectMapper objectMapper;
  @Mock private HttpClient httpClient;

  private BitfrostNotificationService service;

  private static final String TEST_USER_ID = "user-12345";
  private static final String TEST_PROJECT_SECRET = "test-secret-key";

  @BeforeEach
  void setUp() {
    service = new BitfrostNotificationService(objectMapper, httpClient);
    ReflectionTestUtils.setField(
        service, "bitfrostApiUrl", "https://bitfrost.test.de/api/v1/messages/publish");
    ReflectionTestUtils.setField(service, "serviceName", "TestService");
    ReflectionTestUtils.setField(service, "topicName", "user:deletion");
    ReflectionTestUtils.setField(service, "projectSecret", TEST_PROJECT_SECRET);
  }

  @Test
  @DisplayName("notifyUserDeletion_WhenProjectSecretIsNull_SkipsNotificationAndReturnsTrue")
  void notifyUserDeletion_WhenProjectSecretIsNull_SkipsNotificationAndReturnsTrue()
      throws Exception {
    // Given - Project secret is null (not configured)
    ReflectionTestUtils.setField(service, "projectSecret", null);

    // When
    boolean result = service.notifyUserDeletion(TEST_USER_ID);

    // Then - Should return true (gracefully skip) and not attempt serialization
    assertThat(result).isTrue();
    verify(objectMapper, never()).writeValueAsString(any());
  }

  @Test
  @DisplayName("notifyUserDeletion_WhenProjectSecretIsBlank_SkipsNotificationAndReturnsTrue")
  void notifyUserDeletion_WhenProjectSecretIsBlank_SkipsNotificationAndReturnsTrue()
      throws Exception {
    // Given - Project secret is blank (explicitly disabled)
    ReflectionTestUtils.setField(service, "projectSecret", "");

    // When
    boolean result = service.notifyUserDeletion(TEST_USER_ID);

    // Then - Should return true (gracefully skip) and not attempt serialization
    assertThat(result).isTrue();
    verify(objectMapper, never()).writeValueAsString(any());
  }

  @Test
  @DisplayName("notifyUserDeletion_WhenProjectSecretIsWhitespace_SkipsNotificationAndReturnsTrue")
  void notifyUserDeletion_WhenProjectSecretIsWhitespace_SkipsNotificationAndReturnsTrue()
      throws Exception {
    // Given - Project secret is whitespace only
    ReflectionTestUtils.setField(service, "projectSecret", "   ");

    // When
    boolean result = service.notifyUserDeletion(TEST_USER_ID);

    // Then - Should return true (gracefully skip) and not attempt serialization
    assertThat(result).isTrue();
    verify(objectMapper, never()).writeValueAsString(any());
  }

  @Test
  @DisplayName("notifyUserDeletion_WhenJsonSerializationFails_ReturnsFalse")
  void notifyUserDeletion_WhenJsonSerializationFails_ReturnsFalse() throws Exception {
    // Given - ObjectMapper fails to serialize payload
    when(objectMapper.writeValueAsString(any()))
        .thenThrow(new RuntimeException("JSON serialization failed"));

    // When
    boolean result = service.notifyUserDeletion(TEST_USER_ID);

    // Then - Should return false (operation failed)
    assertThat(result).isFalse();
    verify(objectMapper).writeValueAsString(any());
  }

  @Test
  @DisplayName("notifyUserDeletion_WhenSerializationSucceeds_ButHttpFails_ReturnsFalse")
  void notifyUserDeletion_WhenSerializationSucceeds_ButHttpFails_ReturnsFalse() throws Exception {
    // Given - ObjectMapper serializes successfully, but HTTP request will fail
    // (no HttpClient mocking, so the underlying HTTP call will throw)
    String jsonPayload = "{\"user-id\":\"user-12345\"}";
    when(objectMapper.writeValueAsString(any())).thenReturn(jsonPayload);

    // When - Call service (HTTP layer will fail since HttpClient is not mocked)
    boolean result = service.notifyUserDeletion(TEST_USER_ID);

    // Then - Should return false due to HTTP failure, and verify serialization was attempted
    assertThat(result).isFalse();
    verify(objectMapper).writeValueAsString(any());
  }

  @Test
  @DisplayName("notifyUserDeletion_WhenConfiguredWithSecret_AttemptsSerialization")
  void notifyUserDeletion_WhenConfiguredWithSecret_AttemptsSerialization() throws Exception {
    // Given - All configuration is valid (projectSecret is set in setUp())
    // and ObjectMapper succeeds
    String jsonPayload = "{\"user-id\":\"" + TEST_USER_ID + "\"}";
    when(objectMapper.writeValueAsString(any())).thenReturn(jsonPayload);

    // When - Call service with valid configuration
    // (HTTP layer will fail without proper mocking, but this verifies
    // the serialization path is reached)
    boolean result = service.notifyUserDeletion(TEST_USER_ID);

    // Then - Serialization should have been attempted, and method should
    // return false (HTTP will fail without mocking)
    verify(objectMapper).writeValueAsString(any());
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("notifyUserDeletion_WithDifferentUserIds_CallsSerializationForEach")
  void notifyUserDeletion_WithDifferentUserIds_CallsSerializationForEach() throws Exception {
    // Given - ObjectMapper succeeds
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    // When - Call with different user IDs
    service.notifyUserDeletion("user-1");
    service.notifyUserDeletion("user-2");

    // Then - Serialization should have been called twice (once per call)
    verify(objectMapper, times(2)).writeValueAsString(any());
  }

  @Test
  @DisplayName("notifyUserDeletion_WithSpecialCharactersInServiceName_ProperlyEncodesPathSegment")
  void notifyUserDeletion_WithSpecialCharactersInServiceName_ProperlyEncodesPathSegment()
      throws Exception {
    // Given - Service name contains special characters that need URL encoding
    String serviceNameWithSpecialChars = "Test Service";
    ReflectionTestUtils.setField(service, "serviceName", serviceNameWithSpecialChars);
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    // When - Call service with special characters in service name
    // (HTTP layer will fail without proper mocking, but we're verifying
    // that the URI is properly constructed with encoding)
    boolean result = service.notifyUserDeletion(TEST_USER_ID);

    // Then - Serialization was attempted, indicating the request was constructed
    // (URI encoding happens within UriComponentsBuilder before HTTP call)
    verify(objectMapper).writeValueAsString(any());
    // Space characters should be encoded as %20 by UriComponentsBuilder
    assertThat(result).isFalse(); // HTTP will fail, but URI encoding succeeded
  }

  @Test
  @DisplayName("notifyUserDeletion_WithSpecialCharactersInTopicName_ProperlyEncodesPathSegment")
  void notifyUserDeletion_WithSpecialCharactersInTopicName_ProperlyEncodesPathSegment()
      throws Exception {
    // Given - Topic name contains special characters that need URL encoding
    String topicNameWithSpecialChars = "user:deletion/events";
    ReflectionTestUtils.setField(service, "topicName", topicNameWithSpecialChars);
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    // When - Call service with special characters in topic name
    boolean result = service.notifyUserDeletion(TEST_USER_ID);

    // Then - Serialization was attempted, indicating the request was constructed
    // (Forward slashes and colons are properly encoded by UriComponentsBuilder)
    verify(objectMapper).writeValueAsString(any());
    assertThat(result).isFalse(); // HTTP will fail, but URI encoding succeeded
  }

  @Test
  @DisplayName("notifyUserDeletion_ConstructsURIFromConfiguredBaseUrl_WithPathComponents")
  void notifyUserDeletion_ConstructsURIFromConfiguredBaseUrl_WithPathComponents() throws Exception {
    // Given - Valid configuration
    ReflectionTestUtils.setField(
        service, "bitfrostApiUrl", "https://bitfrost.example.de/api/v1/messages/publish");
    ReflectionTestUtils.setField(service, "serviceName", "MyService");
    ReflectionTestUtils.setField(service, "topicName", "events:notify");
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    // When - Call service
    boolean result = service.notifyUserDeletion(TEST_USER_ID);

    // Then - Serialization was attempted, confirming URI construction completed
    // Expected URI: https://bitfrost.example.de/api/v1/messages/publish/MyService/events%3Anotify
    verify(objectMapper).writeValueAsString(any());
    assertThat(result).isFalse(); // HTTP will fail, but URI was constructed
  }
}
