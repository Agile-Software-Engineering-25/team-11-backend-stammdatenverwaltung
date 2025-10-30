package com.ase.stammdatenverwaltung.config;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.KeycloakUser;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Initializes the database with test data on application startup. */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private static final int PREDEF_STUDENT_YEAR = 2000;
  private static final int PREDEF_LECTURER_YEAR = 1980;
  private static final int PREDEF_SAU_ADMIN_YEAR = 1990;
  private static final int PREDEF_ADMIN_STAFF_YEAR = 1985;
  private static final int NUM_STUDENTS_PER_GROUP = 30;
  private static final int STUDENT_G1_YEAR = 2002;
  private static final int STUDENT_G2_YEAR = 2003;
  private static final int LECTURER_YEAR = 1975;
  private static final int EMPLOYEE_YEAR = 1995;
  private static final int PHONE_NUM_G1_START = 10;
  private static final int PHONE_NUM_G2_START = 40;
  private static final int PHONE_NUM_LECTURER_START = 70;
  private static final int PHONE_NUM_EMPLOYEE_START = 80;
  private static final int COHORT_F1_END = 10;
  private static final int COHORT_F2_END = 20;

  private final PersonRepository personRepository;
  private final StudentRepository studentRepository;
  private final LecturerRepository lecturerRepository;
  private final EmployeeRepository employeeRepository;
  private final KeycloakClient keycloakClient;

  @Override
  public void run(String... args) throws Exception {
    createUsers();
  }

  private void createUsers() {
    // Predefined users
    createPredefinedUsers();

    // Generated users
    createGeneratedStudents();
    createGeneratedLecturers();
    createGeneratedEmployees();
  }

  private void createPredefinedUsers() {
    // Collect predefined entities and save in bulk
    List<Student> students = new ArrayList<>();
    List<Lecturer> lecturers = new ArrayList<>();
    List<Employee> employees = new ArrayList<>();

    // test-stud - b7acb825-4e70-49e4-84a1-bf5dc7c8f509 - Student
    Student student =
        Student.builder()
            .id("b7acb825-4e70-49e4-84a1-bf5dc7c8f509")
            .dateOfBirth(LocalDate.of(PREDEF_STUDENT_YEAR, 1, 1))
            .address("Test Address 1")
            .phoneNumber("123456789")
            .matriculationNumber("123456")
            .degreeProgram("Computer Science")
            .semester(1)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("BIN-T23-F4")
            .build();
    students.add(student);

    // test-doz - fc6ac29a-b9dd-4b35-889f-2baff71f3be1 - Lecturer
    Lecturer lecturer =
        Lecturer.builder()
            .id("fc6ac29a-b9dd-4b35-889f-2baff71f3be1")
            .dateOfBirth(LocalDate.of(PREDEF_LECTURER_YEAR, 1, 1))
            .address("Test Address 2")
            .phoneNumber("987654321")
            .employeeNumber("L123")
            .fieldChair("Software Engineering")
            .title("Dr.")
            .employmentStatus(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT)
            .build();
    lecturers.add(lecturer);

    // test-sau-admin - 4f6bf355-6a63-45c5-8839-2f4b571cb478 - SAU Admin
    Employee sauAdmin =
        Employee.builder()
            .id("4f6bf355-6a63-45c5-8839-2f4b571cb478")
            .dateOfBirth(LocalDate.of(PREDEF_SAU_ADMIN_YEAR, 1, 1))
            .address("Test Address 3")
            .phoneNumber("1122334455")
            .employeeNumber("E123")
            .department("SAU")
            .officeNumber("A101")
            .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
            .build();
    employees.add(sauAdmin);

    // test-hochschulverwaltung - 471fb05c-e3c5-4bd1-9c41-dc355885811c - University administrative
    // staff
    Employee adminStaff =
        Employee.builder()
            .id("471fb05c-e3c5-4bd1-9c41-dc355885811c")
            .dateOfBirth(LocalDate.of(PREDEF_ADMIN_STAFF_YEAR, 1, 1))
            .address("Test Address 4")
            .phoneNumber("5566778899")
            .employeeNumber("E124")
            .department("Administration")
            .officeNumber("B202")
            .workingTimeModel(Employee.WorkingTimeModel.PART_TIME)
            .build();
    employees.add(adminStaff);

    // Save predefined entities in bulk (lists contain the predefined entries above)
    studentRepository.saveAll(students);
    lecturerRepository.saveAll(lecturers);
    employeeRepository.saveAll(employees);
  }

  private void createGeneratedStudents() {
    List<Student> studentsToSave = new ArrayList<>();
    for (int i = 1; i <= NUM_STUDENTS_PER_GROUP; i++) {
      Student s = createStudentForGroup1(i);
      if (s != null) {
        studentsToSave.add(s);
      }
    }
    for (int i = 1; i <= NUM_STUDENTS_PER_GROUP; i++) {
      Student s = createStudentForGroup2(i);
      if (s != null) {
        studentsToSave.add(s);
      }
    }
    if (!studentsToSave.isEmpty()) {
      studentRepository.saveAll(studentsToSave);
    }
  }

  private Student createStudentForGroup1(int i) {
    String email = "student_g1_" + i + "@test.com";
    List<KeycloakUser> existingUsers = keycloakClient.findUserByEmail(email).block();
    if (existingUsers == null || existingUsers.isEmpty()) {
      return null;
    }
    KeycloakUser keycloakUser = existingUsers.stream().findFirst().get();
    if (studentRepository.existsById(keycloakUser.getId())) {
      return null;
    }
    return Student.builder()
        .id(keycloakUser.getId())
        .dateOfBirth(LocalDate.of(STUDENT_G1_YEAR, 1, 1))
        .address("Some Address")
        .phoneNumber("+49151000000" + (PHONE_NUM_G1_START + i))
        .matriculationNumber("m_g1_" + i)
        .degreeProgram("Computer Science")
        .semester(2)
        .studyStatus(Student.StudyStatus.ENROLLED)
        .cohort("BIN-T23-F4")
        .build();
  }

  private Student createStudentForGroup2(int i) {
    String email = "student_g2_" + i + "@test.com";
    List<KeycloakUser> existingUsers = keycloakClient.findUserByEmail(email).block();
    if (existingUsers == null || existingUsers.isEmpty()) {
      return null;
    }
    KeycloakUser keycloakUser = existingUsers.stream().findFirst().get();
    if (studentRepository.existsById(keycloakUser.getId())) {
      return null;
    }
    String cohort;
    if (i <= COHORT_F1_END) {
      cohort = "BIN-T23-F1";
    } else if (i <= COHORT_F2_END) {
      cohort = "BIN-T23-F2";
    } else {
      cohort = "BIN-T23-F3";
    }
    return Student.builder()
        .id(keycloakUser.getId())
        .dateOfBirth(LocalDate.of(STUDENT_G2_YEAR, 1, 1))
        .address("Some other Address")
        .phoneNumber("+49151000000" + (PHONE_NUM_G2_START + i))
        .matriculationNumber("m_g2_" + i)
        .degreeProgram("Business Informatics")
        .semester(4)
        .studyStatus(Student.StudyStatus.ENROLLED)
        .cohort(cohort)
        .build();
  }

  private void createGeneratedLecturers() {
    List<Lecturer> lecturersToSave = new ArrayList<>();
    for (int i = 1; i <= 4; i++) {
      String email = "lecturer_" + i + "@test.com";
      List<KeycloakUser> existingUsers = keycloakClient.findUserByEmail(email).block();
      if (existingUsers != null && !existingUsers.isEmpty()) {
        KeycloakUser keycloakUser = existingUsers.stream().findFirst().get();
        if (!lecturerRepository.existsById(keycloakUser.getId())) {
          Lecturer lecturer =
              Lecturer.builder()
                  .id(keycloakUser.getId())
                  .dateOfBirth(LocalDate.of(LECTURER_YEAR, 1, 1))
                  .address("Lecturer Address")
                  .phoneNumber("+49151000000" + (PHONE_NUM_LECTURER_START + i))
                  .employeeNumber("L-00" + i)
                  .department("IT")
                  .officeNumber("C" + i)
                  .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
                  .fieldChair("Field " + i)
                  .title("Prof. Dr.")
                  .employmentStatus(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT)
                  .build();
          lecturersToSave.add(lecturer);
        }
      }
    }
    if (!lecturersToSave.isEmpty()) {
      lecturerRepository.saveAll(lecturersToSave);
    }
  }

  private void createGeneratedEmployees() {
    List<Employee> employeesToSave = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      String email = "admin_staff_" + i + "@test.com";
      List<KeycloakUser> existingUsers = keycloakClient.findUserByEmail(email).block();
      if (existingUsers != null && !existingUsers.isEmpty()) {
        KeycloakUser keycloakUser = existingUsers.stream().findFirst().get();
        if (!employeeRepository.existsById(keycloakUser.getId())) {
          Employee employee =
              Employee.builder()
                  .id(keycloakUser.getId())
                  .dateOfBirth(LocalDate.of(EMPLOYEE_YEAR, 1, 1))
                  .address("Admin Address")
                  .phoneNumber("+49151000000" + (PHONE_NUM_EMPLOYEE_START + i))
                  .employeeNumber("E-00" + i)
                  .department("University Administration")
                  .officeNumber("D" + i)
                  .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
                  .build();
          employeesToSave.add(employee);
        }
      }
    }
    if (!employeesToSave.isEmpty()) {
      employeeRepository.saveAll(employeesToSave);
    }
  }
}
