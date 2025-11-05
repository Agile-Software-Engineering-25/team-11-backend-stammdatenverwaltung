package com.ase.stammdatenverwaltung.config;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.KeycloakUser;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
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
    // Define predefined user IDs upfront
    String predefinedStudentId = "d03aa006-6f0b-4939-9287-753798b6d403";
    String predefinedLecturerId = "5a9cee80-7f1e-4bfb-8d4a-218128b3550f";
    String predefinedSauAdminId = "8807bc3c-5329-4a7b-8b47-6a828d696465";
    String predefinedAdminStaffId = "9d1c388c-55a5-426e-8dd6-9f037a1bc1ba";

    // Check existence before creating any entities
    boolean studentExists = studentRepository.existsById(predefinedStudentId);
    boolean lecturerExists = lecturerRepository.existsById(predefinedLecturerId);
    boolean sauAdminExists = employeeRepository.existsById(predefinedSauAdminId);
    boolean adminStaffExists = employeeRepository.existsById(predefinedAdminStaffId);

    // Collect entities to save
    List<Student> students = new ArrayList<>();
    List<Lecturer> lecturers = new ArrayList<>();
    List<Employee> employees = new ArrayList<>();

    // Add only entities that don't already exist
    if (!studentExists) {
      Student student =
          Student.builder()
              .id(predefinedStudentId)
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
    }

    if (!lecturerExists) {
      Lecturer lecturer =
          Lecturer.builder()
              .id(predefinedLecturerId)
              .dateOfBirth(LocalDate.of(PREDEF_LECTURER_YEAR, 1, 1))
              .address("Test Address 2")
              .phoneNumber("987654321")
              .employeeNumber("L123")
              .fieldChair("Software Engineering")
              .title("Dr.")
              .employmentStatus(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT)
              .build();
      lecturers.add(lecturer);
    }

    if (!sauAdminExists) {
      Employee sauAdmin =
          Employee.builder()
              .id(predefinedSauAdminId)
              .dateOfBirth(LocalDate.of(PREDEF_SAU_ADMIN_YEAR, 1, 1))
              .address("Test Address 3")
              .phoneNumber("1122334455")
              .employeeNumber("E123")
              .department("SAU")
              .officeNumber("A101")
              .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
              .build();
      employees.add(sauAdmin);
    }

    if (!adminStaffExists) {
      Employee adminStaff =
          Employee.builder()
              .id(predefinedAdminStaffId)
              .dateOfBirth(LocalDate.of(PREDEF_ADMIN_STAFF_YEAR, 1, 1))
              .address("Test Address 4")
              .phoneNumber("5566778899")
              .employeeNumber("E124")
              .department("Administration")
              .officeNumber("B202")
              .workingTimeModel(Employee.WorkingTimeModel.PART_TIME)
              .build();
      employees.add(adminStaff);
    }

    // Save all collected entities in bulk
    if (!students.isEmpty()) {
      studentRepository.saveAll(students);
    }
    if (!lecturers.isEmpty()) {
      lecturerRepository.saveAll(lecturers);
    }
    if (!employees.isEmpty()) {
      employeeRepository.saveAll(employees);
    }
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
