package com.ase.stammdatenverwaltung.config;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for HttpClient. Provides a reusable singleton HttpClient bean for making HTTP
 * requests with efficient connection pooling.
 *
 * <p>This bean is shared across the application to avoid the overhead of creating new HttpClient
 * instances for each request, which would recreate connection pools repeatedly.
 */
@Configuration
public class HttpClientConfig {

  /**
   * Creates and configures a singleton HttpClient bean with default settings.
   *
   * <p>The returned instance is thread-safe and can be shared across multiple services. It
   * automatically manages connection pooling and keeps connections alive for reuse.
   *
   * @return A configured HttpClient instance
   */
  @Bean
  HttpClient httpClient() {
    return HttpClient.newHttpClient();
  }
}
