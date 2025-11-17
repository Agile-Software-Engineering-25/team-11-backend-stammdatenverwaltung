package com.ase.stammdatenverwaltung.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Keycloak integration. These properties are loaded from
 * application.yml under the "keycloak" prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Data
@Validated
public class KeycloakConfigProperties {
  private String serverUrl;
  private String realm;
  private String clientId;
  private String clientSecret;
  private String grantType;
  private String userApiUrl;
  private boolean enabled = true;
}
