package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Student;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for transferring student data between layers. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
  private String firstName;
  private String lastName;
  private String matriculationNumber;
  private String degreeProgram;
  private Integer semester;
  private Student.StudyStatus studyStatus;
  private String cohort;
  private String address;
  private String phoneNumber;
  private LocalDate dateOfBirth;
}
