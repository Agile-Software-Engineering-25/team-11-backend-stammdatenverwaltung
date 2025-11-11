package com.ase.stammdatenverwaltung.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Concrete implementation of MinioStorageClient using the MinIO Java client library. This adapter
 * wraps the MinIO client to provide the MinioStorageClient abstraction, allowing: - Unit tests to
 * mock the interface without MinIO client initialization issues - Production code to use the MinIO
 * client directly - Future implementations to support alternative storage backends
 *
 * <p>Only instantiated if MinIO is enabled via configuration property.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class MinioStorageClientImpl implements MinioStorageClient {

  private final MinioClient minioClient;

  @Override
  public InputStream getObject(String bucket, String objectName) throws Exception {
    log.debug("Retrieving object from MinIO - bucket={}, objectName={}", bucket, objectName);
    try {
      InputStream result =
          minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
      log.debug("Object retrieved successfully - bucket={}, objectName={}", bucket, objectName);
      return result;
    } catch (Exception e) {
      log.error(
          "Failed to retrieve object from MinIO - bucket={}, objectName={}, "
              + "errorType={}, message={}",
          bucket,
          objectName,
          e.getClass().getSimpleName(),
          e.getMessage());
      log.debug("Object retrieval error details", e);
      throw e;
    }
  }

  @Override
  public void putObject(
      String bucket,
      String objectName,
      InputStream stream,
      long size,
      long partSize,
      String contentType)
      throws Exception {
    log.debug(
        "Uploading object to MinIO - bucket={}, objectName={}, size={}, "
            + "partSize={}, contentType={}",
        bucket,
        objectName,
        size,
        partSize,
        contentType);
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectName).stream(stream, size, partSize)
              .contentType(contentType)
              .build());
      log.debug(
          "Object uploaded successfully to MinIO - bucket={}, objectName={}, size={}",
          bucket,
          objectName,
          size);
    } catch (Exception e) {
      log.error(
          "Failed to upload object to MinIO - bucket={}, objectName={}, size={}, "
              + "errorType={}, message={}",
          bucket,
          objectName,
          size,
          e.getClass().getSimpleName(),
          e.getMessage());
      log.debug("Object upload error details", e);
      throw e;
    }
  }

  @Override
  public void removeObject(String bucket, String objectName) throws Exception {
    log.debug("Deleting object from MinIO - bucket={}, objectName={}", bucket, objectName);
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
      log.debug(
          "Object deleted successfully from MinIO - bucket={}, objectName={}", bucket, objectName);
    } catch (Exception e) {
      log.error(
          "Failed to delete object from MinIO - bucket={}, objectName={}, "
              + "errorType={}, message={}",
          bucket,
          objectName,
          e.getClass().getSimpleName(),
          e.getMessage());
      log.debug("Object deletion error details", e);
      throw e;
    }
  }
}
