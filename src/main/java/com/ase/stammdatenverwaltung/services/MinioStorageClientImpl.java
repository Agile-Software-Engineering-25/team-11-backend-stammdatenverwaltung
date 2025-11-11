package com.ase.stammdatenverwaltung.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
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
public class MinioStorageClientImpl implements MinioStorageClient {

  private final MinioClient minioClient;

  @Override
  public InputStream getObject(String bucket, String objectName) throws Exception {
    return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
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
    minioClient.putObject(
        PutObjectArgs.builder().bucket(bucket).object(objectName).stream(stream, size, partSize)
            .contentType(contentType)
            .build());
  }

  @Override
  public void removeObject(String bucket, String objectName) throws Exception {
    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
  }
}
