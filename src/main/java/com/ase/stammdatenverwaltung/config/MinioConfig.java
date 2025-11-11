package com.ase.stammdatenverwaltung.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Config Class for MinIO Store */
@Configuration
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
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
  public MinioClient minioClient() {
    // Strip protocol from endpoint if present, as MinIO client expects only hostname
    String cleanEndpoint = endpoint;
    if (cleanEndpoint.startsWith("https://")) {
      cleanEndpoint = cleanEndpoint.substring("https://".length());
    } else if (cleanEndpoint.startsWith("http://")) {
      cleanEndpoint = cleanEndpoint.substring("http://".length());
    }

    return MinioClient.builder()
        .endpoint(cleanEndpoint, port, tls)
        .credentials(accessKey, secretKey)
        .build();
  }
}
