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
    // map base Person fields using the new static mapper
    PersonDetailsDTO base = PersonDetailsDTO.fromEntity(employee);
    if (base != null) {
      this.setId(base.getId());
      this.setDateOfBirth(base.getDateOfBirth());
      this.setAddress(base.getAddress());
      this.setPhoneNumber(base.getPhoneNumber());
      this.setPhotoUrl(base.getPhotoUrl());
      this.setDrivesCar(base.isDrivesCar());
    }

    this.employeeNumber = employee.getEmployeeNumber();
    this.department = employee.getDepartment();
    this.officeNumber = employee.getOfficeNumber();
    this.workingTimeModel = employee.getWorkingTimeModel();
  }
}
