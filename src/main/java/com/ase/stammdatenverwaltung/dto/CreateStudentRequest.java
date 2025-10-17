package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Data transfer object for creating a new student. */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateStudentRequest extends PersonRequest {

  @NotBlank(message = "Matriculation number is required")
  @Size(max = 20, message = "Matriculation number cannot exceed 20 characters")
  private String matriculationNumber;

  @Size(max = 200, message = "Degree program cannot exceed 200 characters")
  private String degreeProgram;

  @Positive(message = "Semester must be positive") private Integer semester;

  @NotNull(message = "Study status is required") private Student.StudyStatus studyStatus;

  @Size(max = 50, message = "Cohort identifier cannot exceed 50 characters")
  private String cohort;
}
