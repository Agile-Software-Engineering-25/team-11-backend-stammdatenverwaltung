package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Person;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data Transfer Object for sending detailed person information to clients. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonDetailsDTO {
  private String id;
  private LocalDate dateOfBirth;
  private String address;
  private String phoneNumber;
  private String photoUrl;
  private boolean drivesCar;

  // Additional fields coming from Keycloak
  private String username;
  private String firstName;
  private String lastName;
  private String email;

  /**
   * Creates a DTO from a Person entity. Does not populate authentication-related fields.
   *
   * @param person source Person entity
   * @return mapped PersonDetailsDTO
   */
  public static PersonDetailsDTO fromEntity(Person person) {
    if (person == null) {
      return null;
    }
    return PersonDetailsDTO.builder()
        .id(person.getId())
        .dateOfBirth(person.getDateOfBirth())
        .address(person.getAddress())
        .phoneNumber(person.getPhoneNumber())
        .photoUrl(person.getPhotoUrl())
        .drivesCar(person.isDrivesCar())
        .build();
  }
}
