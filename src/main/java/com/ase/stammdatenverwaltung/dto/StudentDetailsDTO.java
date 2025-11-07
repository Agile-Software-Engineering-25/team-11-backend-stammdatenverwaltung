package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Student;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Data Transfer Object for sending detailed student information to clients. */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentDetailsDTO extends PersonDetailsDTO {
  private String matriculationNumber;
  private String degreeProgram;
  private Integer semester;
  private Student.StudyStatus studyStatus;
  private String cohort;

  /**
   * Constructs a StudentDetailsDTO from a Student entity.
   *
   * @param student The Student entity.
   */
  public StudentDetailsDTO(Student student) {
    // map base Person fields using the new static mapper
    PersonDetailsDTO base = PersonDetailsDTO.fromEntity(student);
    if (base != null) {
      this.setId(base.getId());
      this.setDateOfBirth(base.getDateOfBirth());
      this.setAddress(base.getAddress());
      this.setPhoneNumber(base.getPhoneNumber());
      this.setPhotoUrl(base.getPhotoUrl());
      this.setDrivesCar(base.isDrivesCar());
    }

    this.matriculationNumber = student.getMatriculationNumber();
    this.degreeProgram = student.getDegreeProgram();
    this.semester = student.getSemester();
    this.studyStatus = student.getStudyStatus();
    this.cohort = student.getCohort();
  }
}
