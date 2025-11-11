package com.ase.stammdatenverwaltung.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Config Class for MinIO Store */
@Configuration
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
@Slf4j
public class MinioConfig {

  /** MinIO endpoint URL - can be with or without protocol (https://, http://) */
  private String endpoint;

  private int port = 9000;

  /** Whether to use TLS/HTTPS for MinIO connection */
  private boolean tls = true;

  private String accessKey;
  private String secretKey;
  private String bucketName;

  /**
   * Client for MinIO Operations. Only created if MinIO is enabled in configuration. Disabled by
   * default in development environments without MinIO connectivity.
   */
  @Bean
  @ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
  MinioClient minioClient() {
    // Strip protocol from endpoint if present, as MinIO client expects only hostname
    String cleanEndpoint = endpoint;
    if (cleanEndpoint.startsWith("https://")) {
      cleanEndpoint = cleanEndpoint.substring("https://".length());
    } else if (cleanEndpoint.startsWith("http://")) {
      cleanEndpoint = cleanEndpoint.substring("http://".length());
    }

    log.info(
        "Initializing MinIO client - endpoint={}, port={}, tls={}, bucket={}",
        cleanEndpoint,
        port,
        tls,
        bucketName);

    try {
      MinioClient client =
          MinioClient.builder()
              .endpoint(cleanEndpoint, port, tls)
              .credentials(accessKey, secretKey)
              .build();
      log.debug(
          "MinIO client successfully initialized - endpoint={}, port={}, tls={}, "
              + "bucket={}, originalEndpoint={}",
          cleanEndpoint,
          port,
          tls,
          bucketName,
          endpoint);
      return client;
    } catch (Exception e) {
      log.error(
          "MinIO client initialization failed - endpoint={}, port={}, tls={}, "
              + "errorType={}, message={}",
          cleanEndpoint,
          port,
          tls,
          e.getClass().getSimpleName(),
          e.getMessage());
      log.debug("MinIO initialization error details", e);
      throw new RuntimeException("Failed to initialize MinIO client", e);
    }
  }
}
