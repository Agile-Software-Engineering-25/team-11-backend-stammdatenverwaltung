package com.ase.stammdatenverwaltung.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Represents the Keycloak groups as an enum. */
@Getter
@RequiredArgsConstructor
public enum KeycloakGroup {
  STUDENT("student"),
  LECTURER("lecturer"),
  UNIVERSITY_ADMINISTRATIVE_STAFF("university-administrative-staff");

  private final String groupName;
}
