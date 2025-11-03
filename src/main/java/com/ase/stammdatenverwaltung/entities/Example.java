package com.ase.stammdatenverwaltung.entities;

import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple example entity demonstrating basic JPA entity structure. This serves as a template for
 * creating other entities in the application.
 */
@Entity
@Table(name = "examples")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Example {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "Name cannot be blank")
  @Size(
      min = ValidationConstants.MIN_NAME_LENGTH,
      max = ValidationConstants.MAX_NAME_LENGTH,
      message =
          "Name must be between "
              + ValidationConstants.MIN_NAME_LENGTH
              + " and "
              + ValidationConstants.MAX_NAME_LENGTH
              + " characters")
  @Column(name = "name", nullable = false, length = ValidationConstants.MAX_NAME_LENGTH)
  private String name;

  @NotBlank(message = "Description cannot be blank")
  @Size(
      min = ValidationConstants.MIN_DESCRIPTION_LENGTH,
      max = ValidationConstants.MAX_DESCRIPTION_LENGTH,
      message =
          "Description must be between "
              + ValidationConstants.MIN_DESCRIPTION_LENGTH
              + " and "
              + ValidationConstants.MAX_DESCRIPTION_LENGTH
              + " characters")
  @Column(
      name = "description",
      nullable = false,
      length = ValidationConstants.MAX_DESCRIPTION_LENGTH)
  private String description;
}
