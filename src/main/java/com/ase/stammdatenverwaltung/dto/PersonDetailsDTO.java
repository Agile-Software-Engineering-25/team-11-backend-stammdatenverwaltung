package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Person;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/** Data Transfer Object for sending detailed person information to clients. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonDetailsDTO extends Person {
  private String username;
  private String firstName;
  private String lastName;
  private String email;

  /**
   * Constructs a PersonDetailsDTO from a Person entity.
   *
   * @param person The Person entity.
   */
  public PersonDetailsDTO(Person person) {
    super(
        person.getId(),
        person.getDateOfBirth(),
        person.getAddress(),
        person.getPhoneNumber(),
        person.getPhotoUrl());
  }
}