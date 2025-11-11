package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.config.MinioConfig;
import com.ase.stammdatenverwaltung.dto.ProfilePictureData;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureDeletionException;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureRetrievalException;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureStorageException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Service component for managing MinIO ProfilePicture Operations.
 *
 * <p>This service uses the MinioStorageClient abstraction for all storage operations. By depending
 * on the abstraction rather than the concrete MinIO client, we achieve: - Testability: Can mock
 * storage operations without MinIO client initialization - Flexibility: Can switch storage backends
 * without changing business logic - CLEAN Architecture: Low coupling to external dependencies
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
public class MinIOService {

  private final MinioConfig minioConfig;
  private final MinioStorageClient storageClient;

  private static final int BUFFER_SIZE = 8192;
  // MinIO multipart upload requires part size in bytes; 1024 represents 1 kilobyte
  private static final int PIC_SIZE = 1024;

  /**
   * Constructs MinIOService with configuration and storage client.
   *
   * @param minioConfig configuration for bucket names and settings
   * @param storageClient abstracted storage client for performing operations
   */
  public MinIOService(MinioConfig minioConfig, MinioStorageClient storageClient) {
    this.minioConfig = minioConfig;
    this.storageClient = storageClient;
  }

  /**
   * Gets a Users ProfilePic from the MinIO Object Store with metadata.
   *
   * @param id The ID of the user.
   * @return ProfilePictureData containing picture bytes, content-type, and existence flag
   * @throws ProfilePictureRetrievalException if retrieval fails
   */
  public ProfilePictureData getProfilePicture(String id) {
    log.debug("Getting Profile Picture of User with ID: {}", id);
    try (InputStream stream = storageClient.getObject(minioConfig.getBucketName(), id);
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead;
      while ((bytesRead = stream.read(buffer)) != -1) {
        baos.write(buffer, 0, bytesRead);
      }
      return new ProfilePictureData(baos.toByteArray(), "application/octet-stream");
    } catch (Exception e) {
      log.error("Error retrieving profile picture for user ID: {}", id, e);
      String errorMessage =
          String.format(
              "Failed to retrieve profile picture for user: %s (%s: %s)",
              id, e.getClass().getSimpleName(), e.getMessage());
      throw new ProfilePictureRetrievalException(errorMessage, id, e);
    }
  }

  /**
   * Sets a Users ProfilePic in the MinIO Object Store.
   *
   * @param id The ID of the user.
   * @param picture the picture data
   * @param contentType the content type of the picture
   * @throws ProfilePictureStorageException if storage fails
   */
  public void setProfilePicture(String id, byte[] picture, String contentType) {
    log.debug("Setting Profile Picture of User with ID: {}", id);
    try (InputStream stream = new ByteArrayInputStream(picture)) {
      long size = picture.length;
      long partSize = 5L * PIC_SIZE * PIC_SIZE;

      storageClient.putObject(minioConfig.getBucketName(), id, stream, size, partSize, contentType);

    } catch (Exception e) {
      log.error("Error setting profile picture for user ID: {}", id, e);
      String errorMessage =
          String.format(
              "Failed to set profile picture for user: %s (%s: %s)",
              id, e.getClass().getSimpleName(), e.getMessage());
      throw new ProfilePictureStorageException(errorMessage, id, e);
    }
  }

  /**
   * Delete a Users ProfilePic from the MinIO Object Store.
   *
   * @param id The ID of the user.
   * @throws ProfilePictureDeletionException if deletion fails
   */
  public void deleteProfilePicture(String id) {
    log.debug("Deleting Profile Picture of User with ID: {}", id);
    try {
      storageClient.removeObject(minioConfig.getBucketName(), id);
    } catch (Exception e) {
      log.error("Error deleting profile picture for user ID: {}", id, e);
      String errorMessage =
          String.format(
              "Failed to delete profile picture for user: %s (%s: %s)",
              id, e.getClass().getSimpleName(), e.getMessage());
      throw new ProfilePictureDeletionException(errorMessage, id, e);
    }
  }
}
