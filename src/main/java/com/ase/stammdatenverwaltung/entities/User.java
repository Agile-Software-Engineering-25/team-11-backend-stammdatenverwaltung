package com.ase.stammdatenverwaltung.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.Data;

/**
 * Entity representing user information extracted from JWT token. This class maps to the structure
 * of the JWT token claims from Keycloak. Field names use camelCase for Java conventions, with
 * JsonProperty annotations to map to the JWT claim names with underscores.
 */
@Data
public class User {
  public int exp;
  public int iat;

  @JsonProperty("auth_time")
  public int authTime;

  public String jti;
  public String iss;
  public String aud;
  public String sub;
  public String typ;
  public String azp;
  public String sid;

  @JsonProperty("at_hash")
  public String atHash;

  public String acr;
  public String upn;

  @JsonProperty("email_verified")
  public boolean emailVerified;

  public String name;
  public ArrayList<String> groups;

  @JsonProperty("preferred_username")
  public String preferredUsername;

  @JsonProperty("given_name")
  public String givenName;

  @JsonProperty("family_name")
  public String familyName;

  public String email;
}
