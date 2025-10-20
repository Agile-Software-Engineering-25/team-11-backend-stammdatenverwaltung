package com.ase.stammdatenverwaltung.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Represents a user object retrieved from Keycloak. */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakUser {
  private String id;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private List<String> groups;
  private List<String> roles;
  private boolean enabled;
}
