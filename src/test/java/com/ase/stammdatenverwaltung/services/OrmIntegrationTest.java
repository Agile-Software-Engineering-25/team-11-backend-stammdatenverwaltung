package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.ase.stammdatenverwaltung.dto.CreateEmployeeRequest;
import com.ase.stammdatenverwaltung.dto.CreateLecturerRequest;
import com.ase.stammdatenverwaltung.dto.CreateStudentRequest;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ORM Integration Tests")
class OrmIntegrationTest {

  @Autowired private PersonRepository personRepository;

  @Autowired private StudentRepository studentRepository;

  @Autowired private EmployeeRepository employeeRepository;

  @Autowired private LecturerRepository lecturerRepository;

  @Autowired private PersonService personService;

  @Autowired private StudentService studentService;

  @Autowired private EmployeeService employeeService;

  @Autowired private LecturerService lecturerService;

  @BeforeEach
  void setUp() {
    lecturerRepository.deleteAll();
    employeeRepository.deleteAll();
    studentRepository.deleteAll();
    personRepository.deleteAll();
  }

  @Test
  @DisplayName("Should create and persist Person entity")
  void shouldCreateAndPersistPersonEntity() {
    Person person =
        Person.builder()
            .id(UUID.randomUUID().toString())
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .address("Test Address 123")
            .phoneNumber("+49 123 456789")
            .photoUrl("http://example.com/photo.jpg")
            .build();

    Person savedPerson = personService.create(person);

    assertThat(savedPerson.getId()).isNotNull();
    assertThat(savedPerson.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
    assertThat(savedPerson.getAddress()).isEqualTo("Test Address 123");

    Optional<Person> foundPerson = personRepository.findById(savedPerson.getId());
    assertThat(foundPerson).isPresent();
    assertThat(foundPerson.get().getAddress()).isEqualTo("Test Address 123");
  }

  @Test
  @DisplayName("Should create and persist Student entity with inheritance")
  void shouldCreateAndPersistStudentEntityWithInheritance() {
    CreateStudentRequest request = new CreateStudentRequest();
    request.setDateOfBirth(LocalDate.of(2000, 8, 20));
    request.setAddress("Student Address 456");
    request.setPhoneNumber("+49 987 654321");
    request.setMatriculationNumber("S2023001");
    request.setDegreeProgram("Computer Science");
    request.setSemester(3);
    request.setStudyStatus(Student.StudyStatus.ENROLLED);
    request.setCohort("BIN-T-23");

    Student savedStudent = studentService.create(request);

    assertThat(savedStudent.getId()).isNotNull();
    assertThat(savedStudent.getMatriculationNumber()).isEqualTo("S2023001");
    assertThat(savedStudent.getDegreeProgram()).isEqualTo("Computer Science");
    assertThat(savedStudent.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 8, 20));

    Optional<Person> foundAsPerson = personRepository.findById(savedStudent.getId());
    Optional<Student> foundAsStudent = studentRepository.findById(savedStudent.getId());

    assertThat(foundAsPerson).isPresent();
    assertThat(foundAsStudent).isPresent();
    assertThat(foundAsPerson.get().getId()).isEqualTo(foundAsStudent.get().getId());
  }

  @Test
  @DisplayName("Should create and persist Employee entity with inheritance")
  void shouldCreateAndPersistEmployeeEntityWithInheritance() {
    CreateEmployeeRequest request = new CreateEmployeeRequest();
    request.setDateOfBirth(LocalDate.of(1985, 3, 10));
    request.setAddress("Employee Address 789");
    request.setPhoneNumber("+49 555 123456");
    request.setEmployeeNumber("E001");
    request.setDepartment("IT Support");
    request.setOfficeNumber("A-101");
    request.setWorkingTimeModel(Employee.WorkingTimeModel.FULL_TIME);

    Employee savedEmployee = employeeService.create(request);

    assertThat(savedEmployee.getId()).isNotNull();
    assertThat(savedEmployee.getEmployeeNumber()).isEqualTo("E001");
    assertThat(savedEmployee.getDepartment()).isEqualTo("IT Support");
    assertThat(savedEmployee.getOfficeNumber()).isEqualTo("A-101");

    Optional<Person> foundAsPerson = personRepository.findById(savedEmployee.getId());
    Optional<Employee> foundAsEmployee = employeeRepository.findById(savedEmployee.getId());

    assertThat(foundAsPerson).isPresent();
    assertThat(foundAsEmployee).isPresent();
    assertThat(foundAsPerson.get().getAddress()).isEqualTo("Employee Address 789");
  }

  @Test
  @DisplayName("Should create and persist Lecturer entity with multi-level inheritance")
  void shouldCreateAndPersistLecturerEntityWithMultiLevelInheritance() {
    CreateLecturerRequest request = new CreateLecturerRequest();
    request.setDateOfBirth(LocalDate.of(1975, 11, 5));
    request.setAddress("Professor Address 999");
    request.setPhoneNumber("+49 333 777888");
    request.setEmployeeNumber("L001");
    request.setDepartment("Computer Science Department");
    request.setOfficeNumber("B-201");
    request.setWorkingTimeModel(Employee.WorkingTimeModel.FULL_TIME);
    request.setFieldChair("Software Engineering");
    request.setTitle("Prof. Dr.");
    request.setEmploymentStatus(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT);

    Lecturer savedLecturer = lecturerService.create(request);

    assertThat(savedLecturer.getId()).isNotNull();
    assertThat(savedLecturer.getFieldChair()).isEqualTo("Software Engineering");
    assertThat(savedLecturer.getTitle()).isEqualTo("Prof. Dr.");
    assertThat(savedLecturer.getEmploymentStatus())
        .isEqualTo(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT);

    Optional<Person> foundAsPerson = personRepository.findById(savedLecturer.getId());
    Optional<Employee> foundAsEmployee = employeeRepository.findById(savedLecturer.getId());
    Optional<Lecturer> foundAsLecturer = lecturerRepository.findById(savedLecturer.getId());

    assertThat(foundAsPerson).isPresent();
    assertThat(foundAsEmployee).isPresent();
    assertThat(foundAsLecturer).isPresent();

    assertThat(foundAsPerson.get().getId()).isEqualTo(savedLecturer.getId());
    assertThat(foundAsEmployee.get().getId()).isEqualTo(savedLecturer.getId());
    assertThat(foundAsLecturer.get().getId()).isEqualTo(savedLecturer.getId());

    assertThat(foundAsPerson.get().getAddress()).isEqualTo("Professor Address 999");
    assertThat(foundAsEmployee.get().getDepartment()).isEqualTo("Computer Science Department");
  }

  @Test
  @DisplayName("Should handle cascading deletes correctly")
  void shouldHandleCascadingDeletesCorrectly() {
    CreateLecturerRequest request = new CreateLecturerRequest();
    request.setDateOfBirth(LocalDate.of(1980, 6, 15));
    request.setAddress("Test Address for Deletion");
    request.setPhoneNumber("+49 111 222333");
    request.setEmployeeNumber("DELETE001");
    request.setDepartment("Test Department");
    request.setOfficeNumber("DELETE-101");
    request.setWorkingTimeModel(Employee.WorkingTimeModel.PART_TIME);
    request.setFieldChair("Test Field");
    request.setTitle("Dr.");
    request.setEmploymentStatus(Lecturer.EmploymentStatus.EXTERNAL);

    Lecturer savedLecturer = lecturerService.create(request);
    String lecturerId = savedLecturer.getId();

    assertThat(personRepository.findById(lecturerId)).isPresent();
    assertThat(employeeRepository.findById(lecturerId)).isPresent();
    assertThat(lecturerRepository.findById(lecturerId)).isPresent();

    lecturerService.deleteById(lecturerId);

    assertThat(lecturerRepository.findById(lecturerId)).isEmpty();
    assertThat(employeeRepository.findById(lecturerId)).isEmpty();
    assertThat(personRepository.findById(lecturerId)).isEmpty();
  }
}
