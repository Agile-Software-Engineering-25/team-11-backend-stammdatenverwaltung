package com.ase.stammdatenverwaltung.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user data. Supports partial updates where only provided fields are modified. All
 * fields are optional to allow flexible updates. Fields not provided remain unchanged.
 *
 * <p>Supports updating fields from Person base class and all subtypes (Student, Employee, Lecturer)
 * according to the inheritance hierarchy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

  // ========== PERSON BASE FIELDS ==========

  @JsonProperty("date_of_birth")
  private LocalDate dateOfBirth;

  @JsonProperty("address")
  @Size(max = 500, message = "Address cannot exceed 500 characters")
  private String address;

  @JsonProperty("phone_number")
  @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Phone number must be a valid format")
  private String phoneNumber;

  @JsonProperty("photo_url")
  @Size(max = 1000, message = "Photo URL cannot exceed 1000 characters")
  private String photoUrl;

  // ========== STUDENT-SPECIFIC FIELDS ==========

  @JsonProperty("matriculation_number")
  @Size(max = 20, message = "Matriculation number cannot exceed 20 characters")
  private String matriculationNumber;

  @JsonProperty("degree_program")
  @Size(max = 200, message = "Degree program cannot exceed 200 characters")
  private String degreeProgram;

  @JsonProperty("semester")
  @Positive(message = "Semester must be positive")
  private Integer semester;

  @JsonProperty("study_status")
  private String studyStatus;

  @JsonProperty("cohort")
  @Pattern(regexp = "\\S+", message = "Cohort cannot contain whitespaces")
  private String cohort;

  // ========== EMPLOYEE-SPECIFIC FIELDS ==========

  @JsonProperty("employee_number")
  @Size(max = 20, message = "Employee number cannot exceed 20 characters")
  private String employeeNumber;

  @JsonProperty("department")
  @Size(max = 200, message = "Department cannot exceed 200 characters")
  private String department;

  @JsonProperty("office_number")
  @Size(max = 50, message = "Office number cannot exceed 50 characters")
  private String officeNumber;

  @JsonProperty("working_time_model")
  private String workingTimeModel;

  // ========== LECTURER-SPECIFIC FIELDS ==========

  @JsonProperty("field_chair")
  @Size(max = 300, message = "Field/chair cannot exceed 300 characters")
  private String fieldChair;

  @JsonProperty("title")
  @Size(max = 50, message = "Title cannot exceed 50 characters")
  private String title;

  @JsonProperty("employment_status")
  private String employmentStatus;
}
