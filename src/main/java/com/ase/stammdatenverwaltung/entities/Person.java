package com.ase.stammdatenverwaltung.entities;

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
   * Primary key that must match the ID from Team 10's database for collaboration and consistency
   * across systems.
   */
  @Id
  @Column(name = "id")
  private String id;



  /** Date of birth of the person. Must be in the past. */
  @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past")
  @Column(name = "date_of_birth", nullable = false)
  private LocalDate dateOfBirth;

  /** Physical address of the person. */
  @Size(max = 500, message = "Address cannot exceed 500 characters")
  @Column(name = "address", length = 500)
  private String address;

  /** Phone number of the person. */
  @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Phone number must be a valid format")
  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  /** Optional URL or path to the person's photo. */
  @Size(max = 1000, message = "Photo URL cannot exceed 1000 characters")
  @Column(name = "photo_url", length = 1000)
  private String photoUrl;
}
