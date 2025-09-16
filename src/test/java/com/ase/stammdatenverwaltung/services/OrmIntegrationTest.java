package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the ORM setup and inheritance relationships. These tests verify that the
 * joined table inheritance works correctly with the actual database.
 */
@SpringBootTest
@ActiveProfiles("dev")
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
    // Clear all tables before each test
    lecturerRepository.deleteAll();
    employeeRepository.deleteAll();
    studentRepository.deleteAll();
    personRepository.deleteAll();
  }

  @Test
  @DisplayName("Should create and persist Person entity")
  void shouldCreateAndPersistPersonEntity() {
    // Given
    Person person =
        Person.builder()
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .address("Test Address 123")
            .phoneNumber("+49 123 456789")
            .photoUrl("http://example.com/photo.jpg")
            .build();

    // When
    Person savedPerson = personService.create(person);

    // Then
    assertThat(savedPerson.getId()).isNotNull();
    assertThat(savedPerson.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
    assertThat(savedPerson.getAddress()).isEqualTo("Test Address 123");

    // Verify persistence
    Optional<Person> foundPerson = personRepository.findById(savedPerson.getId());
    assertThat(foundPerson).isPresent();
    assertThat(foundPerson.get().getAddress()).isEqualTo("Test Address 123");
  }

  @Test
  @DisplayName("Should create and persist Student entity with inheritance")
  void shouldCreateAndPersistStudentEntityWithInheritance() {
    // Given
    Student student =
        Student.builder()
            .dateOfBirth(LocalDate.of(2000, 8, 20))
            .address("Student Address 456")
            .phoneNumber("+49 987 654321")
            .matriculationNumber("S2023001")
            .degreeProgram("Computer Science")
            .semester(3)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("BIN-T-23")
            .build();

    // When
    Student savedStudent = studentService.create(student);

    // Then
    assertThat(savedStudent.getId()).isNotNull();
    assertThat(savedStudent.getMatriculationNumber()).isEqualTo("S2023001");
    assertThat(savedStudent.getDegreeProgram()).isEqualTo("Computer Science");
    assertThat(savedStudent.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 8, 20));

    // Verify inheritance - should be found in both Person and Student repositories
    Optional<Person> foundAsPerson = personRepository.findById(savedStudent.getId());
    Optional<Student> foundAsStudent = studentRepository.findById(savedStudent.getId());

    assertThat(foundAsPerson).isPresent();
    assertThat(foundAsStudent).isPresent();
    assertThat(foundAsPerson.get().getId()).isEqualTo(foundAsStudent.get().getId());
  }

  @Test
  @DisplayName("Should create and persist Employee entity with inheritance")
  void shouldCreateAndPersistEmployeeEntityWithInheritance() {
    // Given
    Employee employee =
        Employee.builder()
            .dateOfBirth(LocalDate.of(1985, 3, 10))
            .address("Employee Address 789")
            .phoneNumber("+49 555 123456")
            .employeeNumber("E001")
            .department("IT Support")
            .officeNumber("A-101")
            .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
            .build();

    // When
    Employee savedEmployee = employeeService.create(employee);

    // Then
    assertThat(savedEmployee.getId()).isNotNull();
    assertThat(savedEmployee.getEmployeeNumber()).isEqualTo("E001");
    assertThat(savedEmployee.getDepartment()).isEqualTo("IT Support");
    assertThat(savedEmployee.getOfficeNumber()).isEqualTo("A-101");

    // Verify inheritance
    Optional<Person> foundAsPerson = personRepository.findById(savedEmployee.getId());
    Optional<Employee> foundAsEmployee = employeeRepository.findById(savedEmployee.getId());

    assertThat(foundAsPerson).isPresent();
    assertThat(foundAsEmployee).isPresent();
    assertThat(foundAsPerson.get().getAddress()).isEqualTo("Employee Address 789");
  }

  @Test
  @DisplayName("Should create and persist Lecturer entity with multi-level inheritance")
  void shouldCreateAndPersistLecturerEntityWithMultiLevelInheritance() {
    // Given
    Lecturer lecturer =
        Lecturer.builder()
            .dateOfBirth(LocalDate.of(1975, 11, 5))
            .address("Professor Address 999")
            .phoneNumber("+49 333 777888")
            .employeeNumber("L001")
            .department("Computer Science Department")
            .officeNumber("B-201")
            .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
            .fieldChair("Software Engineering")
            .title("Prof. Dr.")
            .employmentStatus(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT)
            .build();

    // When
    Lecturer savedLecturer = lecturerService.create(lecturer);

    // Then
    assertThat(savedLecturer.getId()).isNotNull();
    assertThat(savedLecturer.getFieldChair()).isEqualTo("Software Engineering");
    assertThat(savedLecturer.getTitle()).isEqualTo("Prof. Dr.");
    assertThat(savedLecturer.getEmploymentStatus())
        .isEqualTo(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT);

    // Verify multi-level inheritance
    Optional<Person> foundAsPerson = personRepository.findById(savedLecturer.getId());
    Optional<Employee> foundAsEmployee = employeeRepository.findById(savedLecturer.getId());
    Optional<Lecturer> foundAsLecturer = lecturerRepository.findById(savedLecturer.getId());

    assertThat(foundAsPerson).isPresent();
    assertThat(foundAsEmployee).isPresent();
    assertThat(foundAsLecturer).isPresent();

    // All should have the same ID
    assertThat(foundAsPerson.get().getId()).isEqualTo(savedLecturer.getId());
    assertThat(foundAsEmployee.get().getId()).isEqualTo(savedLecturer.getId());
    assertThat(foundAsLecturer.get().getId()).isEqualTo(savedLecturer.getId());

    // Verify inherited fields are accessible
    assertThat(foundAsPerson.get().getAddress()).isEqualTo("Professor Address 999");
    assertThat(foundAsEmployee.get().getDepartment()).isEqualTo("Computer Science Department");
  }

  @Test
  @DisplayName("Should handle cascading deletes correctly")
  void shouldHandleCascadingDeletesCorrectly() {
    // Given - Create a lecturer (which includes person and employee data)
    Lecturer lecturer =
        Lecturer.builder()
            .dateOfBirth(LocalDate.of(1980, 6, 15))
            .address("Test Address for Deletion")
            .phoneNumber("+49 111 222333")
            .employeeNumber("DELETE001")
            .department("Test Department")
            .officeNumber("DELETE-101")
            .workingTimeModel(Employee.WorkingTimeModel.PART_TIME)
            .fieldChair("Test Field")
            .title("Dr.")
            .employmentStatus(Lecturer.EmploymentStatus.EXTERNAL)
            .build();

    Lecturer savedLecturer = lecturerService.create(lecturer);
    Long lecturerId = savedLecturer.getId();

    // Verify all records exist
    assertThat(personRepository.findById(lecturerId)).isPresent();
    assertThat(employeeRepository.findById(lecturerId)).isPresent();
    assertThat(lecturerRepository.findById(lecturerId)).isPresent();

    // When - Delete the lecturer
    lecturerService.deleteById(lecturerId);

    // Then - All related records should be deleted due to CASCADE
    assertThat(lecturerRepository.findById(lecturerId)).isEmpty();
    assertThat(employeeRepository.findById(lecturerId)).isEmpty();
    assertThat(personRepository.findById(lecturerId)).isEmpty();
  }

  @Test
  @DisplayName("Should query across inheritance hierarchy")
  void shouldQueryAcrossInheritanceHierarchy() {
    // Given - Create different types of persons
    Person person =
        Person.builder()
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .address("Person Only Address")
            .phoneNumber("+49 111 111111")
            .build();

    Student student =
        Student.builder()
            .dateOfBirth(LocalDate.of(2000, 2, 2))
            .address("Student Address")
            .phoneNumber("+49 222 222222")
            .matriculationNumber("QUERY001")
            .degreeProgram("Test Program")
            .semester(2)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("TEST-COHORT")
            .build();

    Employee employee =
        Employee.builder()
            .dateOfBirth(LocalDate.of(1985, 3, 3))
            .address("Employee Address")
            .phoneNumber("+49 333 333333")
            .employeeNumber("QUERY002")
            .department("Test Department")
            .officeNumber("QUERY-101")
            .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
            .build();

    // Save all entities
    Person savedPerson = personService.create(person);
    Student savedStudent = studentService.create(student);
    Employee savedEmployee = employeeService.create(employee);

    // When - Query all persons (should include all types)
    List<Person> allPersons = personService.findAll();

    // Then - Should find all entities as persons
    assertThat(allPersons).hasSize(3);
    assertThat(allPersons)
        .extracting(Person::getId)
        .containsExactlyInAnyOrder(
            savedPerson.getId(), savedStudent.getId(), savedEmployee.getId());

    // Verify type-specific queries work correctly
    List<Student> students = studentService.findAll();
    List<Employee> employees = employeeService.findAll();

    assertThat(students).hasSize(1);
    assertThat(employees).hasSize(1);
    assertThat(students.get(0).getId()).isEqualTo(savedStudent.getId());
    assertThat(employees.get(0).getId()).isEqualTo(savedEmployee.getId());
  }

  @Test
  @DisplayName("Should handle unique constraints correctly")
  void shouldHandleUniqueConstraintsCorrectly() {
    // Given - Create a student with a specific matriculation number
    Student student1 =
        Student.builder()
            .dateOfBirth(LocalDate.of(2001, 5, 10))
            .address("First Student Address")
            .phoneNumber("+49 444 444444")
            .matriculationNumber("UNIQUE001")
            .degreeProgram("First Program")
            .semester(1)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("UNIQUE-COHORT")
            .build();

    studentService.create(student1);

    // When & Then - Try to create another student with the same matriculation number
    Student student2 =
        Student.builder()
            .dateOfBirth(LocalDate.of(2002, 6, 15))
            .address("Second Student Address")
            .phoneNumber("+49 555 555555")
            .matriculationNumber("UNIQUE001") // Same matriculation number
            .degreeProgram("Second Program")
            .semester(1)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("UNIQUE-COHORT-2")
            .build();

    assertThatThrownBy(() -> studentService.create(student2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Student with matriculation number UNIQUE001 already exists");
  }

  @Test
  @DisplayName("Should support complex queries with inheritance relationships")
  void shouldSupportComplexQueriesWithInheritanceRelationships() {
    // Given - Create test data
    createComplexTestData();

    // When & Then - Test complex queries
    verifyComplexQueries();
  }

  private void createComplexTestData() {
    Student enrolledStudent = createEnrolledStudent();
    Student graduatedStudent = createGraduatedStudent();
    Lecturer permanentLecturer = createPermanentLecturer();
    Lecturer externalLecturer = createExternalLecturer();

    // Save all entities
    studentService.create(enrolledStudent);
    studentService.create(graduatedStudent);
    lecturerService.create(permanentLecturer);
    lecturerService.create(externalLecturer);
  }

  private Student createEnrolledStudent() {
    return Student.builder()
        .dateOfBirth(LocalDate.of(2000, 1, 1))
        .address("Enrolled Student Address")
        .phoneNumber("+49 101 101101")
        .matriculationNumber("COMPLEX001")
        .degreeProgram("Computer Science")
        .semester(3)
        .studyStatus(Student.StudyStatus.ENROLLED)
        .cohort("COMPLEX-COHORT")
        .build();
  }

  private Student createGraduatedStudent() {
    return Student.builder()
        .dateOfBirth(LocalDate.of(1999, 12, 31))
        .address("Graduated Student Address")
        .phoneNumber("+49 202 202202")
        .matriculationNumber("COMPLEX002")
        .degreeProgram("Computer Science")
        .semester(6)
        .studyStatus(Student.StudyStatus.GRADUATED)
        .cohort("COMPLEX-COHORT")
        .build();
  }

  private Lecturer createPermanentLecturer() {
    return Lecturer.builder()
        .dateOfBirth(LocalDate.of(1975, 6, 15))
        .address("Permanent Lecturer Address")
        .phoneNumber("+49 303 303303")
        .employeeNumber("COMPLEX003")
        .department("Computer Science Department")
        .officeNumber("COMPLEX-201")
        .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
        .fieldChair("Software Engineering")
        .title("Prof. Dr.")
        .employmentStatus(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT)
        .build();
  }

  private Lecturer createExternalLecturer() {
    return Lecturer.builder()
        .dateOfBirth(LocalDate.of(1980, 8, 20))
        .address("External Lecturer Address")
        .phoneNumber("+49 404 404404")
        .employeeNumber("COMPLEX004")
        .department("Computer Science Department")
        .officeNumber("COMPLEX-202")
        .workingTimeModel(Employee.WorkingTimeModel.PART_TIME)
        .fieldChair("Data Science")
        .title("Dr.")
        .employmentStatus(Lecturer.EmploymentStatus.EXTERNAL)
        .build();
  }

  private void verifyComplexQueries() {
    List<Student> enrolledStudents = studentService.findByStudyStatus(Student.StudyStatus.ENROLLED);
    assertThat(enrolledStudents).hasSize(1);

    List<Student> cohortStudents = studentService.findByCohort("COMPLEX-COHORT");
    assertThat(cohortStudents).hasSize(2);

    List<Lecturer> permanentLecturers = lecturerService.findAllPermanentLecturers();
    assertThat(permanentLecturers).hasSize(1);

    List<Lecturer> externalLecturers = lecturerService.findAllExternalLecturers();
    assertThat(externalLecturers).hasSize(1);

    List<Employee> csEmployees = employeeService.findByDepartment("Computer Science");
    assertThat(csEmployees).hasSize(2); // Both lecturers are employees

    List<Person> allPersons = personService.findAll();
    assertThat(allPersons).hasSize(4); // All entities are persons
  }
}
