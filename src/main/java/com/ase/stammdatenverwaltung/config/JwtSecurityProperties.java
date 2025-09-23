package com.ase.stammdatenverwaltung.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT security settings. Provides type-safe configuration for Keycloak
 * JWT authentication parameters.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtSecurityProperties {

  /** Audience in JWT tokens for API access validation. */
  private String audience = "stammdatenverwaltung-api";

  /** Client ID used for extracting client-specific roles from JWT tokens. */
  private String clientId = "stammdatenverwaltung-api";

  /** Whether to validate the audience claim in JWT tokens. */
  private boolean validateAudience = true;

  /** Whether to extract realm roles from JWT tokens. */
  private boolean extractRealmRoles = true;

  /** Whether to extract client roles from JWT tokens. */
  private boolean extractClientRoles = true;

  public String getAudience() {
    return audience;
  }

  public void setAudience(String audience) {
    this.audience = audience;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public boolean isValidateAudience() {
    return validateAudience;
  }

  public void setValidateAudience(boolean validateAudience) {
    this.validateAudience = validateAudience;
  }

  public boolean isExtractRealmRoles() {
    return extractRealmRoles;
  }

  public void setExtractRealmRoles(boolean extractRealmRoles) {
    this.extractRealmRoles = extractRealmRoles;
  }

  public boolean isExtractClientRoles() {
    return extractClientRoles;
  }

  public void setExtractClientRoles(boolean extractClientRoles) {
    this.extractClientRoles = extractClientRoles;
  }
}
