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
    if (endpoint == null) {
      throw new IllegalStateException(
          "MinIO endpoint must not be null. Please set 'minio.endpoint' in your configuration.");
    }
    // Determine whether endpoint includes a scheme (https://, http://)
    boolean hasScheme = endpoint.startsWith("https://") || endpoint.startsWith("http://");

    if (hasScheme) {
      return initializeWithUrlEndpoint();
    } else {
      return initializeWithHostPortEndpoint();
    }
  }

  /**
   * Initialize MinIO client using full URL with scheme. The MinIO SDK will automatically extract
   * the port and protocol from the URL. This is the recommended approach for ingress-based setups
   * where the endpoint is served at standard ports (443 for HTTPS, 80 for HTTP).
   */
  private MinioClient initializeWithUrlEndpoint() {
    log.info(
        "Initializing MinIO client with URL endpoint - endpoint={}, bucket={}, mode=URL",
        endpoint,
        bucketName);

    try {
      MinioClient client =
          MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
      log.debug(
          "MinIO client successfully initialized - endpoint={}, bucket={}, mode=URL, "
              + "note=scheme and port extracted from URL",
          endpoint,
          bucketName);
      return client;
    } catch (Exception e) {
      log.error(
          "MinIO client initialization failed (URL mode) - endpoint={}, bucket={}, "
              + "errorType={}, message={}",
          endpoint,
          bucketName,
          e.getClass().getSimpleName(),
          e.getMessage());
      log.debug("MinIO initialization error details", e);
      throw new RuntimeException("Failed to initialize MinIO client with URL endpoint", e);
    }
  }

  /**
   * Initialize MinIO client using explicit host, port, and TLS flag. This is the fallback mode for
   * direct host-port-tls configurations without a full URL. The host is extracted from the endpoint
   * (stripping any accidental scheme prefix), and explicit port/tls values are used.
   */
  private MinioClient initializeWithHostPortEndpoint() {
    // Extract hostname in case there are any accidental prefixes
    String host = endpoint;
    if (host.startsWith("https://")) {
      host = host.substring("https://".length());
    } else if (host.startsWith("http://")) {
      host = host.substring("http://".length());
    }

    log.info(
        "Initializing MinIO client with host-port endpoint - host={}, port={}, tls={}, "
            + "bucket={}, mode=HOST-PORT",
        host,
        port,
        tls,
        bucketName);

    try {
      MinioClient client =
          MinioClient.builder().endpoint(host, port, tls).credentials(accessKey, secretKey).build();
      log.debug(
          "MinIO client successfully initialized - host={}, port={}, tls={}, bucket={}, "
              + "mode=HOST-PORT, note=explicit host and port used",
          host,
          port,
          tls,
          bucketName);
      return client;
    } catch (Exception e) {
      log.error(
          "MinIO client initialization failed (HOST-PORT mode) - host={}, port={}, tls={}, "
              + "bucket={}, errorType={}, message={}",
          host,
          port,
          tls,
          bucketName,
          e.getClass().getSimpleName(),
          e.getMessage());
      log.debug("MinIO initialization error details", e);
      throw new RuntimeException("Failed to initialize MinIO client with host-port endpoint", e);
    }
  }
}
