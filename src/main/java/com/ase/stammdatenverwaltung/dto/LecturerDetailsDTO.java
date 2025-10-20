package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Data Transfer Object for sending detailed lecturer information to clients. */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LecturerDetailsDTO extends EmployeeDetailsDTO {
  private String fieldChair;
  private String title;
  private Lecturer.EmploymentStatus employmentStatus;

  /**
   * Constructs a LecturerDetailsDTO from a Lecturer entity.
   *
   * @param lecturer The Lecturer entity.
   */
  public LecturerDetailsDTO(Lecturer lecturer) {
    super(lecturer);
    this.fieldChair = lecturer.getFieldChair();
    this.title = lecturer.getTitle();
    this.employmentStatus = lecturer.getEmploymentStatus();
  }
}
