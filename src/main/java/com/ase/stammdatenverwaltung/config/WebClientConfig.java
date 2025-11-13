package com.ase.stammdatenverwaltung.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** Configuration class for WebClient. Provides a WebClient bean for making HTTP requests. */
@Configuration
public class WebClientConfig {

  /**
   * Creates and configures a WebClient bean.
   *
   * @return A configured WebClient instance.
   */
  @Bean
  WebClient webClient() {
    return WebClient.builder().build();
  }
}
