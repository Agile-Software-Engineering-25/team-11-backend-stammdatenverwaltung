package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Employee;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Data transfer object for creating a new employee. */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateEmployeeRequest extends PersonRequest {

  @Size(max = 20, message = "Employee number cannot exceed 20 characters")
  private String employeeNumber;

  @Size(max = 200, message = "Department cannot exceed 200 characters")
  private String department;

  @Size(max = 50, message = "Office number cannot exceed 50 characters")
  private String officeNumber;

  private Employee.WorkingTimeModel workingTimeModel;
}
