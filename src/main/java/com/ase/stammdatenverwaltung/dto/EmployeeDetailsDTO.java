package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Employee;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Data Transfer Object for sending detailed employee information to clients. */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDetailsDTO extends PersonDetailsDTO {
  private String employeeNumber;
  private String department;
  private String officeNumber;
  private Employee.WorkingTimeModel workingTimeModel;

  /**
   * Constructs a EmployeeDetailsDTO from a Employee entity.
   *
   * @param employee The Employee entity.
   */
  public EmployeeDetailsDTO(Employee employee) {
    super(employee);
    this.employeeNumber = employee.getEmployeeNumber();
    this.department = employee.getDepartment();
    this.officeNumber = employee.getOfficeNumber();
    this.workingTimeModel = employee.getWorkingTimeModel();
  }
}
