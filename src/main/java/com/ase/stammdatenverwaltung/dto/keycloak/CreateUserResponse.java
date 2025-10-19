package com.ase.stammdatenverwaltung.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Response DTO from Keycloak user creation endpoint. Contains the created user's UUID and other
 * details.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserResponse {
  private String id;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
}
