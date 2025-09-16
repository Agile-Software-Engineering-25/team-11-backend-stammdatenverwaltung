package com.ase.stammdatenverwaltung.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a Student, which extends the Person base class. Uses joined table inheritance
 * to store student-specific attributes in a separate table while maintaining a reference to the
 * parent Person table.
 */
@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "person_id")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Student extends Person {

  /** Unique matriculation number identifying the student within the institution. */
  @NotBlank(message = "Matriculation number is required")
  @Size(max = 20, message = "Matriculation number cannot exceed 20 characters")
  @Column(name = "matriculation_number", nullable = false, unique = true, length = 20)
  private String matriculationNumber;

  /** The degree program the student is enrolled in (e.g., Computer Science, Business, etc.). */
  @Size(max = 200, message = "Degree program cannot exceed 200 characters")
  @Column(name = "degree_program", length = 200)
  private String degreeProgram;

  /** Current semester/term number. Must be positive. */
  @Positive(message = "Semester must be positive") @Column(name = "semester")
  private Integer semester;

  /** Current study status of the student. */
  @NotNull(message = "Study status is required") @Enumerated(EnumType.STRING)
  @Column(name = "study_status", nullable = false)
  private StudyStatus studyStatus;

  /**
   * Cohort or class identifier (e.g., BIN-T23). Used to group students by their entering class or
   * program variant.
   */
  @Size(max = 50, message = "Cohort identifier cannot exceed 50 characters")
  @Column(name = "cohort", length = 50)
  private String cohort;

  /** Enumeration defining possible study statuses for a student. */
  public enum StudyStatus {
    /** Student is actively enrolled and attending classes. */
    ENROLLED,

    /** Student is registered but not actively attending (e.g., on leave). */
    REGISTERED,

    /** Student is temporarily on leave of absence. */
    ON_LEAVE,

    /** Student has been exmatriculated (withdrawn/expelled). */
    EXMATRICULATED,

    /** Student has successfully graduated. */
    GRADUATED
  }
}
