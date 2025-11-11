package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ase.stammdatenverwaltung.config.MinioConfig;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureDeletionException;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureRetrievalException;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for MinIOService. Tests verify service behavior when MinIO operations succeed or fail.
 * A live MinIO instance is not required - all I/O operations are mocked through the
 * MinioStorageClient abstraction.
 *
 * <p>The abstraction enables complete test coverage including error paths for putObject, which were
 * previously untestable due to MinIO client library initialization issues.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MinIOService Tests")
class MinIOServiceTest {

  @Mock private MinioConfig minioConfig;

  @Mock private MinioStorageClient storageClient;

  private MinIOService minIOService;

  private static final String TEST_USER_ID = "user-123";
  private static final String TEST_BUCKET = "test-bucket";

  @BeforeEach
  void setUp() {
    minIOService = new MinIOService(minioConfig, storageClient);
    when(minioConfig.getBucketName()).thenReturn(TEST_BUCKET);
  }

  @Test
  @DisplayName("getProfilePicture_WhenMinioThrowsException_WrapsInProfilePictureRetrievalException")
  void getProfilePicture_WhenMinioThrowsException_WrapsInProfilePictureRetrievalException()
      throws Exception {
    // Given - Storage client throws an exception
    when(storageClient.getObject(TEST_BUCKET, TEST_USER_ID))
        .thenThrow(new RuntimeException("MinIO connection error"));

    // When & Then - Service wraps the exception
    assertThatThrownBy(() -> minIOService.getProfilePicture(TEST_USER_ID))
        .isInstanceOf(ProfilePictureRetrievalException.class)
        .hasMessageContaining("Failed to retrieve profile picture for user: " + TEST_USER_ID)
        .hasMessageContaining("RuntimeException");

    verify(storageClient).getObject(TEST_BUCKET, TEST_USER_ID);
  }

  @Test
  @DisplayName("setProfilePicture_WhenMinioThrowsException_WrapsInProfilePictureStorageException")
  void setProfilePicture_WhenMinioThrowsException_WrapsInProfilePictureStorageException()
      throws Exception {
    // Given - Mock putObject to throw exception
    doThrow(new RuntimeException("MinIO connection error"))
        .when(storageClient)
        .putObject(
            eq(TEST_BUCKET),
            eq(TEST_USER_ID),
            any(),
            any(Long.class),
            any(Long.class),
            any(String.class));

    // When & Then - Service should wrap exception
    byte[] testPictureData = "test-image-data".getBytes();
    assertThatThrownBy(
            () -> minIOService.setProfilePicture(TEST_USER_ID, testPictureData, "image/png"))
        .isInstanceOf(ProfilePictureStorageException.class)
        .hasMessageContaining("Failed to set profile picture for user: " + TEST_USER_ID)
        .hasMessageContaining("RuntimeException");

    verify(storageClient)
        .putObject(
            eq(TEST_BUCKET),
            eq(TEST_USER_ID),
            any(),
            any(Long.class),
            any(Long.class),
            any(String.class));
  }

  @Test
  @DisplayName("deleteProfilePicture_CallsMinioClientWithCorrectArgs")
  void deleteProfilePicture_CallsMinioClientWithCorrectArgs() throws Exception {
    // When - Service tries to delete profile picture
    minIOService.deleteProfilePicture(TEST_USER_ID);

    // Then - Verify storage client was called
    verify(storageClient).removeObject(TEST_BUCKET, TEST_USER_ID);
  }

  @Test
  @DisplayName(
      "deleteProfilePicture_WhenMinioThrowsException_WrapsInProfilePictureDeletionException")
  void deleteProfilePicture_WhenMinioThrowsException_WrapsInProfilePictureDeletionException()
      throws Exception {
    // Given
    doThrow(new RuntimeException("MinIO connection error"))
        .when(storageClient)
        .removeObject(TEST_BUCKET, TEST_USER_ID);

    // When & Then
    assertThatThrownBy(() -> minIOService.deleteProfilePicture(TEST_USER_ID))
        .isInstanceOf(ProfilePictureDeletionException.class)
        .hasMessageContaining("Failed to delete profile picture for user: " + TEST_USER_ID)
        .hasMessageContaining("RuntimeException");

    verify(storageClient).removeObject(TEST_BUCKET, TEST_USER_ID);
  }
}
