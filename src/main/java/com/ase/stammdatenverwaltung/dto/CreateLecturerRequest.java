package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Lecturer;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** Data transfer object for creating a new lecturer. */
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateLecturerRequest extends CreateEmployeeRequest {

  @Size(max = 300, message = "Field/chair cannot exceed 300 characters")
  private String fieldChair;

  @Size(max = 50, message = "Title cannot exceed 50 characters")
  private String title;

  private Lecturer.EmploymentStatus employmentStatus;
}
