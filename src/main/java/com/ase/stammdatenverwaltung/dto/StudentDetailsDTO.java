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
    super(student);
    this.matriculationNumber = student.getMatriculationNumber();
    this.degreeProgram = student.getDegreeProgram();
    this.semester = student.getSemester();
    this.studyStatus = student.getStudyStatus();
    this.cohort = student.getCohort();
  }
}
