package com.ase.stammdatenverwaltung.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Data Transfer Object for Keycloak user information. Used to deserialize user details fetched from
 * the Keycloak API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakUser {
  private String firstName;
  private String lastName;
  private String email;
}
