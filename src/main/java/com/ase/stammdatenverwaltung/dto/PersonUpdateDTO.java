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
public class PersonUpdateDTO {
  private String id;
  private LocalDate dateOfBirth;
  private String address;
  private String phoneNumber;
  private String photoUrl;

  // Additional fields coming from Keycloak
  private String username;
  private String firstName;
  private String lastName;
  private String email;

  /**
   * Apply non-null fields from this DTO to the provided Person entity. IMPORTANT: Do NOT change the
   * person's id here — the controller supplies the id from the path.
   *
   * @param person the Person entity to update
   */
  public Person applyTo(Person person) {
    if (person == null) {
      return null;
    }
    if (dateOfBirth != null) {
      person.setDateOfBirth(dateOfBirth);
    }
    if (address != null) {
      person.setAddress(address);
    }
    if (phoneNumber != null) {
      person.setPhoneNumber(phoneNumber);
    }
    if (photoUrl != null) {
      person.setPhotoUrl(photoUrl);
    }

    return person;
  }
}
