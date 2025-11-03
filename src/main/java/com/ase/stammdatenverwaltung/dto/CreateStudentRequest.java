package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import com.ase.stammdatenverwaltung.entities.Student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Data transfer object for creating a new student. */
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateStudentRequest extends PersonRequest {

  @NotBlank(message = "Matriculation number is required")
  @Size(
      max = ValidationConstants.MAX_MATRICULATION_NUMBER_LENGTH,
      message =
          "Matriculation number cannot exceed "
              + ValidationConstants.MAX_MATRICULATION_NUMBER_LENGTH
              + " characters")
  private String matriculationNumber;

  @Size(
      max = ValidationConstants.MAX_DEGREE_PROGRAM_LENGTH,
      message =
          "Degree program cannot exceed "
              + ValidationConstants.MAX_DEGREE_PROGRAM_LENGTH
              + " characters")
  private String degreeProgram;

  @Positive(message = "Semester must be positive") private Integer semester;

  @NotNull(message = "Study status is required") private Student.StudyStatus studyStatus;

  @Size(
      max = ValidationConstants.MAX_COHORT_LENGTH,
      message =
          "Cohort identifier cannot exceed "
              + ValidationConstants.MAX_COHORT_LENGTH
              + " characters")
  private String cohort;
}
