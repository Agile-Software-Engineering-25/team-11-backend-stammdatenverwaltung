package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Abstract base class for DTOs representing requests to create or update a Person. Contains fields
 * for Keycloak user creation (username, firstName, lastName, email) which are sent to Keycloak but
 * NOT stored locally in this service's database.
 */
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public abstract class PersonRequest {

  // Fields for Keycloak user creation - NOT stored locally
  @NotBlank(message = "Username is required")
  @Email(message = "Username must be a valid email format")
  private String username;

  @NotBlank(message = "First name is required")
  @Size(
      max = ValidationConstants.MAX_FIRST_NAME_LENGTH,
      message =
          "First name cannot exceed " + ValidationConstants.MAX_FIRST_NAME_LENGTH + " characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(
      max = ValidationConstants.MAX_LAST_NAME_LENGTH,
      message =
          "Last name cannot exceed " + ValidationConstants.MAX_LAST_NAME_LENGTH + " characters")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  // Fields stored locally in this service's database
  @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past")
  private LocalDate dateOfBirth;

  @Size(
      max = ValidationConstants.MAX_ADDRESS_LENGTH,
      message = "Address cannot exceed " + ValidationConstants.MAX_ADDRESS_LENGTH + " characters")
  private String address;

  @Pattern(
      regexp = ValidationConstants.PHONE_NUMBER_PATTERN,
      message = "Phone number must be a valid format")
  private String phoneNumber;

  @Size(
      max = ValidationConstants.MAX_PHOTO_URL_LENGTH,
      message =
          "Photo URL cannot exceed " + ValidationConstants.MAX_PHOTO_URL_LENGTH + " characters")
  private String photoUrl;
}
