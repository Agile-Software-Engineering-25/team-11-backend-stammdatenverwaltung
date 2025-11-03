package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.constants.ValidationConstants;
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
  @Size(
      max = ValidationConstants.MAX_ADDRESS_LENGTH,
      message = "Address cannot exceed " + ValidationConstants.MAX_ADDRESS_LENGTH + " characters")
  private String address;

  @JsonProperty("phone_number")
  @Pattern(
      regexp = ValidationConstants.PHONE_NUMBER_PATTERN,
      message = "Phone number must be a valid format")
  private String phoneNumber;

  @JsonProperty("photo_url")
  @Size(
      max = ValidationConstants.MAX_PHOTO_URL_LENGTH,
      message =
          "Photo URL cannot exceed " + ValidationConstants.MAX_PHOTO_URL_LENGTH + " characters")
  private String photoUrl;

  // ========== STUDENT-SPECIFIC FIELDS ==========

  @JsonProperty("matriculation_number")
  @Size(
      max = ValidationConstants.MAX_MATRICULATION_NUMBER_LENGTH,
      message =
          "Matriculation number cannot exceed "
              + ValidationConstants.MAX_MATRICULATION_NUMBER_LENGTH
              + " characters")
  private String matriculationNumber;

  @JsonProperty("degree_program")
  @Size(
      max = ValidationConstants.MAX_DEGREE_PROGRAM_LENGTH,
      message =
          "Degree program cannot exceed "
              + ValidationConstants.MAX_DEGREE_PROGRAM_LENGTH
              + " characters")
  private String degreeProgram;

  @JsonProperty("semester")
  @Positive(message = "Semester must be positive") private Integer semester;

  @JsonProperty("study_status")
  private String studyStatus;

  @JsonProperty("cohort")
  @Pattern(
      regexp = ValidationConstants.COHORT_NO_WHITESPACE_PATTERN,
      message = "Cohort cannot contain whitespaces")
  private String cohort;

  // ========== EMPLOYEE-SPECIFIC FIELDS ==========

  @JsonProperty("employee_number")
  @Size(
      max = ValidationConstants.MAX_EMPLOYEE_NUMBER_LENGTH,
      message =
          "Employee number cannot exceed "
              + ValidationConstants.MAX_EMPLOYEE_NUMBER_LENGTH
              + " characters")
  private String employeeNumber;

  @JsonProperty("department")
  @Size(
      max = ValidationConstants.MAX_DEPARTMENT_LENGTH,
      message =
          "Department cannot exceed " + ValidationConstants.MAX_DEPARTMENT_LENGTH + " characters")
  private String department;

  @JsonProperty("office_number")
  @Size(
      max = ValidationConstants.MAX_OFFICE_NUMBER_LENGTH,
      message =
          "Office number cannot exceed "
              + ValidationConstants.MAX_OFFICE_NUMBER_LENGTH
              + " characters")
  private String officeNumber;

  @JsonProperty("working_time_model")
  private String workingTimeModel;

  // ========== LECTURER-SPECIFIC FIELDS ==========

  @JsonProperty("field_chair")
  @Size(
      max = ValidationConstants.MAX_FIELD_CHAIR_LENGTH,
      message =
          "Field/chair cannot exceed " + ValidationConstants.MAX_FIELD_CHAIR_LENGTH + " characters")
  private String fieldChair;

  @JsonProperty("title")
  @Size(
      max = ValidationConstants.MAX_TITLE_LENGTH,
      message = "Title cannot exceed " + ValidationConstants.MAX_TITLE_LENGTH + " characters")
  private String title;

  @JsonProperty("employment_status")
  private String employmentStatus;
}
