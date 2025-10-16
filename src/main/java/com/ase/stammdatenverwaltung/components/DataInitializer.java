package com.ase.stammdatenverwaltung.components;

import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.services.EmployeeService;
import com.ase.stammdatenverwaltung.services.LecturerService;
import com.ase.stammdatenverwaltung.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/** Component that initializes sample data for development purposes. */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final StudentService studentService;
  private final LecturerService lecturerService;
  private final EmployeeService employeeService;

  @Override
  public void run(String... args) throws Exception {
    // Create Students
    Student student1 =
        Student.builder()
          .matriculationNumber("D161")
          .dateOfBirth(LocalDate.of(2000, 5, 15))
          .degreeProgram("Computer Science")
          .cohort("BIN-T23-F3")
          .build();
    studentService.create(student1);

    Student student2 =
        Student.builder()
          .matriculationNumber("D456")
          .dateOfBirth(LocalDate.of(1999, 8, 22))
          .degreeProgram("Computer Science")
          .semester(2)
          .studyStatus(Student.StudyStatus.ENROLLED)
          .cohort("BIN-T23-F3")
          .build();
    studentService.create(student2);

    Student student3 =
        Student.builder()
          .matriculationNumber("D678")
          .dateOfBirth(LocalDate.of(2001, 3, 10))
          .degreeProgram("Business Informatics")
          .semester(1)
          .studyStatus(Student.StudyStatus.ENROLLED)
          .cohort("BIN-T24-F1")
          .build();
    studentService.create(student3);

    // Create Lecturers
    Lecturer lecturer1 =
        Lecturer.builder()
          .dateOfBirth(LocalDate.of(1975, 11, 30))
          .fieldChair("Software Engineering")
          .title("Prof. Dr.")
          .employmentStatus(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT)
          .build();
    lecturerService.create(lecturer1);

    // Create Employees
    Employee employee1 =
        Employee.builder()
          .dateOfBirth(LocalDate.of(1985, 2, 20))
          .employeeNumber("E001")
          .department("IT Services")
          .officeNumber("A-101")
          .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
          .build();
    employeeService.create(employee1);
  }
}
