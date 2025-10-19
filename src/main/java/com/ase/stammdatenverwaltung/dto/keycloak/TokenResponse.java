package com.ase.stammdatenverwaltung.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Data Transfer Object for Keycloak token response. Used to deserialize the access token received
 * from the Keycloak API.
 */
@Data
public class TokenResponse {
  @JsonProperty("access_token")
  private String accessToken;
}
