package com.ase.stammdatenverwaltung.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Abstract base class for DTOs representing requests to create or update a Person. Contains fields
 * for Keycloak user creation (username, firstName, lastName, email) which are sent to Keycloak but
 * NOT stored locally in this service's database.
 */
@SuperBuilder
@Data
public abstract class PersonRequest {

  // Fields for Keycloak user creation - NOT stored locally
  @NotBlank(message = "Username is required")
  @Email(message = "Username must be a valid email format")
  private String username;

  @NotBlank(message = "First name is required")
  @Size(max = 100, message = "First name cannot exceed 100 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 100, message = "Last name cannot exceed 100 characters")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  // Fields stored locally in this service's database
  @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past")
  private LocalDate dateOfBirth;

  @Size(max = 500, message = "Address cannot exceed 500 characters")
  private String address;

  @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Phone number must be a valid format")
  private String phoneNumber;

  @Size(max = 1000, message = "Photo URL cannot exceed 1000 characters")
  private String photoUrl;
}
