package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import com.ase.stammdatenverwaltung.entities.Employee;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Data transfer object for creating a new employee. */
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateEmployeeRequest extends PersonRequest {

  @Size(
      max = ValidationConstants.MAX_EMPLOYEE_NUMBER_LENGTH,
      message =
          "Employee number cannot exceed "
              + ValidationConstants.MAX_EMPLOYEE_NUMBER_LENGTH
              + " characters")
  private String employeeNumber;

  @Size(
      max = ValidationConstants.MAX_DEPARTMENT_LENGTH,
      message =
          "Department cannot exceed " + ValidationConstants.MAX_DEPARTMENT_LENGTH + " characters")
  private String department;

  @Size(
      max = ValidationConstants.MAX_OFFICE_NUMBER_LENGTH,
      message =
          "Office number cannot exceed "
              + ValidationConstants.MAX_OFFICE_NUMBER_LENGTH
              + " characters")
  private String officeNumber;

  private Employee.WorkingTimeModel workingTimeModel;
}
