package com.ase.stammdatenverwaltung.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Represents the Keycloak groups as an enum. */
@Getter
@RequiredArgsConstructor
public enum KeycloakGroup {
  STUDENT("Student"),
  LECTURER("Lecturer"),
  SAU_ADMIN("SAU Admin"),
  UNIVERSITY_ADMINISTRATIVE_STAFF("University administrative staff");

  private final String groupName;
}
