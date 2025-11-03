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
 * <p>Supports updating both Person base fields (address, phone, etc.) and Student-specific fields
 * (study status, semester, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

  // Person base fields
  @JsonProperty("date_of_birth")
  private LocalDate dateOfBirth;

  private String address;

  @JsonProperty("phone_number")
  @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Phone number must be a valid format")
  private String phoneNumber;

  @JsonProperty("photo_url")
  @Size(max = 1000, message = "Photo URL cannot exceed 1000 characters")
  private String photoUrl;

  // Student-specific fields
  @JsonProperty("degree_program")
  @Size(max = 200, message = "Degree program cannot exceed 200 characters")
  private String degreeProgram;

  @Positive(message = "Semester must be positive") private Integer semester;

  @JsonProperty("study_status")
  private String studyStatus;

  private String cohort;

  // Employee-specific fields
  @JsonProperty("employment_status")
  private String employmentStatus;

  @JsonProperty("department")
  @Size(max = 100, message = "Department cannot exceed 100 characters")
  private String department;

  @JsonProperty("job_title")
  @Size(max = 150, message = "Job title cannot exceed 150 characters")
  private String jobTitle;

  // Lecturer-specific fields
  @JsonProperty("office_location")
  @Size(max = 200, message = "Office location cannot exceed 200 characters")
  private String officeLocation;

  @JsonProperty("specialization")
  @Size(max = 300, message = "Specialization cannot exceed 300 characters")
  private String specialization;
}
