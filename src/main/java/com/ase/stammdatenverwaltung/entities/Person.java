package com.ase.stammdatenverwaltung.entities;

import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
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
 * Base entity representing a Person in the system. This is the parent class for all user types
 * (Student, Employee, etc.) using joined table inheritance strategy. Contains shared attributes
 * that are common to all person types.
 *
 * <p>The ID must match the database from Team 10 for collaboration purposes.
 */
@Entity
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

  /**
   * Primary key that must match the ID from Team 10's keycloak database for collaboration and
   * consistency across systems.
   */
  @Id
  @Column(name = "id")
  private String id;

  /** Date of birth of the person. Must be in the past. */
  @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past")
  @Column(name = "date_of_birth", nullable = false)
  private LocalDate dateOfBirth;

  /** Physical address of the person. */
  @Size(
      max = ValidationConstants.MAX_ADDRESS_LENGTH,
      message = "Address cannot exceed " + ValidationConstants.MAX_ADDRESS_LENGTH + " characters")
  @Column(name = "address", length = ValidationConstants.MAX_ADDRESS_LENGTH)
  private String address;

  /** Phone number of the person. */
  @Pattern(
      regexp = ValidationConstants.PHONE_NUMBER_PATTERN,
      message = "Phone number must be a valid format")
  @Column(name = "phone_number", length = ValidationConstants.MAX_PHONE_NUMBER_LENGTH)
  private String phoneNumber;

  /** Optional URL or path to the person's photo. */
  @Size(
      max = ValidationConstants.MAX_PHOTO_URL_LENGTH,
      message =
          "Photo URL cannot exceed " + ValidationConstants.MAX_PHOTO_URL_LENGTH + " characters")
  @Column(name = "photo_url", length = ValidationConstants.MAX_PHOTO_URL_LENGTH)
  private String photoUrl;

  /**
   * Indicates whether the person drives a car to the institution. Used for parking service feature.
   * Default is false.
   */
  @Column(name = "drives_car", nullable = false)
  private boolean drivesCar;
}
