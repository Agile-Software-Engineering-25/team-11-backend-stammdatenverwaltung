package com.ase.stammdatenverwaltung.dto;

import com.ase.stammdatenverwaltung.entities.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
