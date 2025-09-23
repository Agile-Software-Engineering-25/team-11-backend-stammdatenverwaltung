package com.ase.stammdatenverwaltung.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Validates required JWT configuration properties at startup. Ensures that essential Keycloak
 * configuration is present in production environments to prevent runtime authentication failures.
 */
@Component
@ConditionalOnProperty(
    name = "spring.profiles.active",
    havingValue = "prod",
    matchIfMissing = false)
public class JwtConfigurationValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtConfigurationValidator.class);

  private final Environment environment;

  /**
   * Creates a new JWT configuration validator.
   *
   * @param environment Spring environment for accessing configuration properties
   */
  public JwtConfigurationValidator(Environment environment) {
    this.environment = environment;
  }

  /**
   * Validates required JWT configuration properties at application startup. Logs warnings for
   * missing optional properties and throws exceptions for missing required properties.
   */
  @PostConstruct
  public void validateConfiguration() {
    LOGGER.info("Validating JWT configuration for production environment...");

    validateRequiredProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
    validateOptionalProperty("spring.security.oauth2.resourceserver.jwt.audiences[0]");

    LOGGER.info("JWT configuration validation completed successfully");
  }

  private void validateRequiredProperty(String propertyName) {
    String value = environment.getProperty(propertyName);
    if (value == null || value.trim().isEmpty()) {
      String errorMessage =
          String.format(
              "Required JWT configuration property '%s' is missing or empty. "
                  + "Please set this property via environment variable or configuration file.",
              propertyName);
      LOGGER.error(errorMessage);
      throw new IllegalStateException(errorMessage);
    }
    LOGGER.debug("Required property '{}' is configured", propertyName);
  }

  private void validateOptionalProperty(String propertyName) {
    String value = environment.getProperty(propertyName);
    if (value == null || value.trim().isEmpty()) {
      LOGGER.warn(
          "Optional JWT configuration property '{}' is not configured. "
              + "Default value will be used.",
          propertyName);
    } else {
      LOGGER.debug("Optional property '{}' is configured: {}", propertyName, value);
    }
  }
}
