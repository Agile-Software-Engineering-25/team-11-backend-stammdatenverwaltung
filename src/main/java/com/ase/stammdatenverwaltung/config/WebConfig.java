package com.ase.stammdatenverwaltung.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures web-related settings for the application, including CORS (Cross-Origin Resource
 * Sharing). This class allows defining global CORS rules from a central location.
 */
@Configuration
public class WebConfig {

  /**
   * Injects the allowed origins for CORS from the application properties. The value is sourced from
   * the {@code cors.allowed-origins} property.
   */
  @Value("${cors.allowed-origins}")
  private String[] allowedOrigins;

  /**
   * Creates a {@link WebMvcConfigurer} bean to customize global CORS settings. This bean applies
   * CORS rules to all endpoints (/**).
   *
   * @return A {@link WebMvcConfigurer} instance with the defined CORS mappings.
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
      }
    };
  }
}
