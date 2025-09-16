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
 * Entity representing an Employee, which extends the Person base class. Uses joined table
 * inheritance to store employee-specific attributes in a separate table while maintaining a
 * reference to the parent Person table.
 */
@Entity
@Table(name = "employees")
@PrimaryKeyJoinColumn(name = "person_id")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends Person {

  /** Employee number/ID if available. May be null for external employees or contractors. */
  @Size(max = 20, message = "Employee number cannot exceed 20 characters")
  @Column(name = "employee_number", length = 20)
  private String employeeNumber;

  /**
   * Department or area where the employee works (e.g., Examination Office, IT Support, Academic
   * Department).
   */
  @Size(max = 200, message = "Department cannot exceed 200 characters")
  @Column(name = "department", length = 200)
  private String department;

  /** Office or room number where the employee is located. */
  @Size(max = 50, message = "Office number cannot exceed 50 characters")
  @Column(name = "office_number", length = 50)
  private String officeNumber;

  /** Working time model indicating the employment type and hours. */
  @Enumerated(EnumType.STRING)
  @Column(name = "working_time_model")
  private WorkingTimeModel workingTimeModel;

  /** Enumeration defining possible working time models for employees. */
  public enum WorkingTimeModel {
    /** Full-time employment (typically 40 hours per week). */
    FULL_TIME,

    /** Part-time employment (less than full-time hours). */
    PART_TIME,

    /** Mini-job or marginal employment (limited hours and earnings). */
    MINI_JOB,

    /** Contract-based or freelance work. */
    CONTRACT,

    /** Temporary or seasonal employment. */
    TEMPORARY,

    /** Intern or trainee position. */
    INTERNSHIP
  }
}
