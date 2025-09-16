package com.ase.stammdatenverwaltung.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a Lecturer, which extends the Employee class. Uses joined table inheritance
 * to store lecturer-specific attributes in a separate table while maintaining references to both
 * the Employee and Person tables through the inheritance hierarchy.
 */
@Entity
@Table(name = "lecturers")
@PrimaryKeyJoinColumn(name = "person_id")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Lecturer extends Employee {

  /**
   * Field of expertise or chair the lecturer belongs to. Alternatively, this could represent the
   * modules they teach.
   */
  @Size(max = 300, message = "Field/chair cannot exceed 300 characters")
  @Column(name = "field_chair", length = 300)
  private String fieldChair;

  /** Academic title of the lecturer (e.g., Prof., Dr., etc.). */
  @Size(max = 50, message = "Title cannot exceed 50 characters")
  @Column(name = "title", length = 50)
  private String title;

  /** Employment status indicating the type of lecturer employment. */
  @Enumerated(EnumType.STRING)
  @Column(name = "employment_status")
  private EmploymentStatus employmentStatus;

  /** Enumeration defining possible employment statuses for lecturers. */
  public enum EmploymentStatus {
    /** Full-time permanent lecturer (e.g., Daubert position). */
    FULL_TIME_PERMANENT,

    /** Part-time permanent lecturer. */
    PART_TIME_PERMANENT,

    /** Externally employed lecturer (e.g., industry professionals like Kay Schulz). */
    EXTERNAL,

    /** Visiting lecturer or guest professor. */
    VISITING,

    /** Contract-based lecturer for specific courses or periods. */
    CONTRACT,

    /** Emeritus professor (retired but still active). */
    EMERITUS,

    /** Assistant professor or junior lecturer. */
    ASSISTANT,

    /** Associate professor. */
    ASSOCIATE,

    /** Full professor with tenure. */
    PROFESSOR
  }
}
