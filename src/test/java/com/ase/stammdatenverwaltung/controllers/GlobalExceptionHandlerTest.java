package com.ase.stammdatenverwaltung.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ase.stammdatenverwaltung.exceptions.ProfilePictureDeletionException;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureRetrievalException;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureStorageException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.WebRequest;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 *
 * <p>Tests the centralized exception handling for all error types: authentication, authorization,
 * domain-specific, and general system errors.
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private WebRequest mockRequest;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    mockRequest = mock(WebRequest.class);
    // Mock the getDescription method to return a valid URI
    when(mockRequest.getDescription(false)).thenReturn("uri=/api/test");
  }

  @Nested
  @DisplayName("Authentication Errors (401)")
  class AuthenticationErrors {

    @Test
    @DisplayName("should handle generic AuthenticationException")
    void testHandleAuthenticationException() {
      AuthenticationException ex = new AuthenticationException("Auth failed") {};
      mockRequest.getDescription(false);

      ResponseEntity<Map<String, Object>> response =
          handler.handleAuthenticationException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).containsKeys("error", "message", "timestamp", "path");
      assertThat(response.getBody().get("error")).isEqualTo("AUTH_001");
    }

    @Test
    @DisplayName("should handle BadCredentialsException")
    void testHandleBadCredentialsException() {
      BadCredentialsException ex = new BadCredentialsException("Bad credentials");

      ResponseEntity<Map<String, Object>> response =
          handler.handleBadCredentialsException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody().get("error")).isEqualTo("AUTH_002");
      assertThat((String) response.getBody().get("message")).contains("Invalid credentials");
    }

    @Test
    @DisplayName("should handle InsufficientAuthenticationException")
    void testHandleInsufficientAuthenticationException() {
      InsufficientAuthenticationException ex =
          new InsufficientAuthenticationException("Insufficient auth");

      ResponseEntity<Map<String, Object>> response =
          handler.handleInsufficientAuthenticationException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody().get("error")).isEqualTo("AUTH_003");
    }
  }

  @Nested
  @DisplayName("Profile Picture Errors")
  class ProfilePictureErrors {

    @Test
    @DisplayName("should handle ProfilePictureRetrievalException")
    void testHandleProfilePictureRetrievalException() {
      ProfilePictureRetrievalException ex =
          new ProfilePictureRetrievalException("Picture not found", "user-123");

      ResponseEntity<Map<String, Object>> response =
          handler.handleProfilePictureException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody().get("error")).isEqualTo("USER_001");
      assertThat(response.getBody().get("context")).isNotNull();
    }

    @Test
    @DisplayName("should handle ProfilePictureStorageException")
    void testHandleProfilePictureStorageException() {
      ProfilePictureStorageException ex =
          new ProfilePictureStorageException("Storage failed", "user-456");

      ResponseEntity<Map<String, Object>> response =
          handler.handleProfilePictureException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody().get("error")).isEqualTo("USER_002");
    }

    @Test
    @DisplayName("should handle ProfilePictureDeletionException")
    void testHandleProfilePictureDeletionException() {
      ProfilePictureDeletionException ex =
          new ProfilePictureDeletionException("Deletion failed", "user-789");

      ResponseEntity<Map<String, Object>> response =
          handler.handleProfilePictureException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody().get("error")).isEqualTo("USER_003");
    }

    @Test
    @DisplayName("should return 404 when ProfilePictureRetrievalException caused by NoSuchKey")
    void testHandleProfilePictureRetrievalException_NotFound() {
      // Create a mock NoSuchKeyException to properly simulate the cause
      class NoSuchKeyException extends Exception {
        NoSuchKeyException(String message) {
          super(message);
        }
      }

      Exception cause = new NoSuchKeyException("Key not found");
      ProfilePictureRetrievalException ex =
          new ProfilePictureRetrievalException("Picture not found", "user-123", cause);

      ResponseEntity<Map<String, Object>> response =
          handler.handleProfilePictureException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("Entity Not Found Errors (404)")
  class EntityNotFoundErrors {

    @Test
    @DisplayName("should handle EntityNotFoundException")
    void testHandleEntityNotFoundException() {
      EntityNotFoundException ex =
          new EntityNotFoundException("Person not found with ID: user-123");

      ResponseEntity<Map<String, Object>> response =
          handler.handleEntityNotFoundException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody()).containsKeys("error", "message", "timestamp", "path");
      assertThat(response.getBody().get("error")).isEqualTo("NOT_FOUND_001");
      assertThat(response.getBody().get("category")).isEqualTo("Entity Not Found");
      assertThat((String) response.getBody().get("message"))
          .isEqualTo("The requested resource does not exist");
    }
  }

  @Nested
  @DisplayName("Input Validation Errors (400)")
  class ValidationErrors {

    @Test
    @DisplayName("should handle IllegalArgumentException")
    void testHandleIllegalArgumentException() {
      IllegalArgumentException ex = new IllegalArgumentException("Invalid file size");

      ResponseEntity<Map<String, Object>> response =
          handler.handleIllegalArgumentException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody().get("error")).isEqualTo("VALIDATION_001");
    }
  }

  @Nested
  @DisplayName("File I/O Errors (500)")
  class IOErrors {

    @Test
    @DisplayName("should handle IOException")
    void testHandleIOException() {
      java.io.IOException ex = new java.io.IOException("File read error");

      ResponseEntity<Map<String, Object>> response = handler.handleIOException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody().get("error")).isEqualTo("IO_001");
    }
  }

  @Nested
  @DisplayName("Response Format")
  class ResponseFormat {

    @Test
    @DisplayName("should include all required fields in error response")
    void testErrorResponseFormat() {
      AuthenticationException ex = new AuthenticationException("Auth failed") {};

      ResponseEntity<Map<String, Object>> response =
          handler.handleAuthenticationException(ex, mockRequest);

      Map<String, Object> body = response.getBody();
      assertThat(body).containsKeys("error", "message", "details", "category", "path", "timestamp");
      assertThat(body.get("timestamp")).isInstanceOf(Long.class);
    }

    @Test
    @DisplayName("should include context map when available")
    void testContextIncludedInResponse() {
      ProfilePictureRetrievalException ex =
          new ProfilePictureRetrievalException("Not found", "user-123");

      ResponseEntity<Map<String, Object>> response =
          handler.handleProfilePictureException(ex, mockRequest);

      Map<String, Object> body = response.getBody();
      assertThat(body).containsKey("context");
      @SuppressWarnings("unchecked")
      Map<String, String> context = (Map<String, String>) body.get("context");
      assertThat(context).containsKey("userId");
    }
  }

  @Nested
  @DisplayName("Fallback Exception Handler")
  class FallbackHandler {

    @Test
    @DisplayName("should handle unexpected exceptions")
    void testHandleUnexpectedException() {
      Exception ex = new RuntimeException("Unexpected error");

      ResponseEntity<Map<String, Object>> response =
          handler.handleGeneralException(ex, mockRequest);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody().get("error")).isEqualTo("SYS_001");
    }
  }
}
