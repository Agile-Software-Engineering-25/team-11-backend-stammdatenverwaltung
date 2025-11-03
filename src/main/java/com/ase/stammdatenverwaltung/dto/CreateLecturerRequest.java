package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Data transfer object for creating a new lecturer. */
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateLecturerRequest extends CreateEmployeeRequest {

  @Size(
      max = ValidationConstants.MAX_FIELD_CHAIR_LENGTH,
      message =
          "Field/chair cannot exceed " + ValidationConstants.MAX_FIELD_CHAIR_LENGTH + " characters")
  private String fieldChair;

  @Size(
      max = ValidationConstants.MAX_TITLE_LENGTH,
      message = "Title cannot exceed " + ValidationConstants.MAX_TITLE_LENGTH + " characters")
  private String title;

  private Lecturer.EmploymentStatus employmentStatus;
}
