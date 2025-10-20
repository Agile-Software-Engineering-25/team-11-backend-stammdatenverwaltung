package com.ase.stammdatenverwaltung.config;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.CreateEmployeeRequest;
import com.ase.stammdatenverwaltung.dto.CreateLecturerRequest;
import com.ase.stammdatenverwaltung.dto.CreateStudentRequest;
import com.ase.stammdatenverwaltung.dto.KeycloakUser;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import com.ase.stammdatenverwaltung.services.EmployeeService;
import com.ase.stammdatenverwaltung.services.LecturerService;
import com.ase.stammdatenverwaltung.services.StudentService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Initializes the database with test data on application startup if the database is empty. */
@Component
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

  private final PersonRepository personRepository;
  private final StudentRepository studentRepository;
  private final LecturerRepository lecturerRepository;
  private final EmployeeRepository employeeRepository;
  private final StudentService studentService;
  private final LecturerService lecturerService;
  private final EmployeeService employeeService;
  private final KeycloakClient keycloakClient;

  @Override
  public void run(String... args) throws Exception {
    if (personRepository.count() == 0) {
      // Predefined users
      createPredefinedUsers();

      // Generated users
      createGeneratedStudents();
      createGeneratedLecturers();
      createGeneratedEmployees();
    }
  }

  private void createPredefinedUsers() {
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
            .cohort("2023")
            .build();
    studentRepository.save(student);

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
    lecturerRepository.save(lecturer);

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
    employeeRepository.save(sauAdmin);

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
    employeeRepository.save(adminStaff);
  }

  private void createGeneratedStudents() {
    for (int i = 1; i <= NUM_STUDENTS_PER_GROUP; i++) {
      String email = "student_g1_" + i + "@test.com";
      List<KeycloakUser> existingUsers = keycloakClient.findUserByEmail(email).block();
      if (existingUsers == null || existingUsers.isEmpty()) {
        CreateStudentRequest request = new CreateStudentRequest();
        request.setUsername(email);
        request.setFirstName("FirstNameG1");
        request.setLastName("LastName" + i);
        request.setEmail(email);
        request.setDateOfBirth(LocalDate.of(STUDENT_G1_YEAR, 1, 1));
        request.setAddress("Some Address");
        request.setPhoneNumber("+49151000000" + (PHONE_NUM_G1_START + i));
        request.setMatriculationNumber("m_g1_" + i);
        request.setDegreeProgram("Computer Science");
        request.setSemester(2);
        request.setStudyStatus(Student.StudyStatus.ENROLLED);
        request.setCohort("G1");
        studentService.create(request);
      }
    }

    for (int i = 1; i <= NUM_STUDENTS_PER_GROUP; i++) {
      String email = "student_g2_" + i + "@test.com";
      List<KeycloakUser> existingUsers = keycloakClient.findUserByEmail(email).block();
      if (existingUsers == null || existingUsers.isEmpty()) {
        CreateStudentRequest request = new CreateStudentRequest();
        request.setUsername(email);
        request.setFirstName("FirstNameG2");
        request.setLastName("LastName" + i);
        request.setEmail(email);
        request.setDateOfBirth(LocalDate.of(STUDENT_G2_YEAR, 1, 1));
        request.setAddress("Some other Address");
        request.setPhoneNumber("+49151000000" + (PHONE_NUM_G2_START + i));
        request.setMatriculationNumber("m_g2_" + i);
        request.setDegreeProgram("Business Informatics");
        request.setSemester(4);
        request.setStudyStatus(Student.StudyStatus.ENROLLED);
        request.setCohort("G2");
        studentService.create(request);
      }
    }
  }

  private void createGeneratedLecturers() {
    for (int i = 1; i <= 4; i++) {
      String email = "lecturer_" + i + "@test.com";
      List<KeycloakUser> existingUsers = keycloakClient.findUserByEmail(email).block();
      if (existingUsers == null || existingUsers.isEmpty()) {
        CreateLecturerRequest request = new CreateLecturerRequest();
        request.setUsername(email);
        request.setFirstName("LecturerFirstName");
        request.setLastName("LastName" + i);
        request.setEmail(email);
        request.setDateOfBirth(LocalDate.of(LECTURER_YEAR, 1, 1));
        request.setAddress("Lecturer Address");
        request.setPhoneNumber("+49151000000" + (PHONE_NUM_LECTURER_START + i));
        request.setEmployeeNumber("L-00" + i);
        request.setDepartment("IT");
        request.setOfficeNumber("C" + i);
        request.setWorkingTimeModel(Employee.WorkingTimeModel.FULL_TIME);
        request.setFieldChair("Field " + i);
        request.setTitle("Prof. Dr.");
        request.setEmploymentStatus(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT);
        lecturerService.create(request);
      }
    }
  }

  private void createGeneratedEmployees() {
    for (int i = 1; i <= 2; i++) {
      String email = "admin_staff_" + i + "@test.com";
      List<KeycloakUser> existingUsers = keycloakClient.findUserByEmail(email).block();
      if (existingUsers == null || existingUsers.isEmpty()) {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setUsername(email);
        request.setFirstName("AdminFirstName");
        request.setLastName("LastName" + i);
        request.setEmail(email);
        request.setDateOfBirth(LocalDate.of(EMPLOYEE_YEAR, 1, 1));
        request.setAddress("Admin Address");
        request.setPhoneNumber("+49151000000" + (PHONE_NUM_EMPLOYEE_START + i));
        request.setEmployeeNumber("E-00" + i);
        request.setDepartment("University Administration");
        request.setOfficeNumber("D" + i);
        request.setWorkingTimeModel(Employee.WorkingTimeModel.FULL_TIME);
        employeeService.create(request);
      }
    }
  }
}
