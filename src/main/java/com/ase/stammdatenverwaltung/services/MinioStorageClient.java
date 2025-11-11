package com.ase.stammdatenverwaltung.services;

import java.io.InputStream;

/**
 * Abstraction layer for MinIO client operations. Decouples MinIOService from MinIO client
 * implementation, enabling proper mocking in unit tests and supporting alternative storage
 * backends.
 *
 * <p>This interface provides low-level storage operations for profile pictures: - Getting objects
 * (profile pictures) - Putting objects (uploading profile pictures) - Removing objects (deleting
 * profile pictures)
 *
 * <p>By abstracting MinIO behind this interface, we can: - Mock storage operations in unit tests
 * without MinIO client initialization issues - Test error handling paths that are difficult to
 * trigger with the real MinIO client - Swap storage implementations (S3, Azure Blob Storage, etc.)
 * without changing service code - Achieve true separation of concerns (CLEAN principle)
 */
public interface MinioStorageClient {

  /**
   * Retrieves an object from the storage backend.
   *
   * @param bucket the bucket name
   * @param objectName the object name/key
   * @return an InputStream containing the object data
   * @throws Exception if the operation fails
   */
  InputStream getObject(String bucket, String objectName) throws Exception;

  /**
   * Stores an object in the storage backend.
   *
   * @param bucket the bucket name
   * @param objectName the object name/key
   * @param stream the input stream containing object data
   * @param size the size of the data in bytes
   * @param partSize the part size for multipart uploads
   * @param contentType the MIME type of the object
   * @throws Exception if the operation fails
   */
  void putObject(
      String bucket,
      String objectName,
      InputStream stream,
      long size,
      long partSize,
      String contentType)
      throws Exception;

  /**
   * Deletes an object from the storage backend.
   *
   * @param bucket the bucket name
   * @param objectName the object name/key
   * @throws Exception if the operation fails
   */
  void removeObject(String bucket, String objectName) throws Exception;
}
