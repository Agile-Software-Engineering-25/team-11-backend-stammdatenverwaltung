package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the ORM setup and inheritance relationships. These tests verify that the
 * joined table inheritance works correctly with the actual database.
 */
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

  @MockBean private KeycloakClient keycloakClient;

  @BeforeEach
  void setUp() {
    lecturerRepository.deleteAll();
    employeeRepository.deleteAll();
    studentRepository.deleteAll();
    personRepository.deleteAll();

    // Mock KeycloakClient to return unique IDs without calling the actual API
    when(keycloakClient.createUser(
            any(com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest.class)))
        .thenAnswer(
            invocation -> {
              com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse response =
                  new com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse();
              response.setId(UUID.randomUUID().toString());
              com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest request =
                  invocation.getArgument(0);
              response.setUsername(request.getUsername());
              response.setFirstName(request.getFirstName());
              response.setLastName(request.getLastName());
              response.setEmail(request.getEmail());
              return Mono.just(response);
            });
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
    CreateStudentRequest request = CreateStudentRequest.builder()
        .dateOfBirth(LocalDate.of(2000, 8, 20))
        .address("Student Address 456")
        .phoneNumber("+49 987 654321")
        .matriculationNumber("S2023001")
        .degreeProgram("Computer Science")
        .semester(3)
        .studyStatus(Student.StudyStatus.ENROLLED)
        .cohort("BIN-T-23")
        .build();

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
    CreateEmployeeRequest request = CreateEmployeeRequest.builder()
        .dateOfBirth(LocalDate.of(1985, 3, 10))
        .address("Employee Address 789")
        .phoneNumber("+49 555 123456")
        .employeeNumber("E001")
        .department("IT Support")
        .officeNumber("A-101")
        .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
        .build();

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
    CreateLecturerRequest request = CreateLecturerRequest.builder()
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
    CreateLecturerRequest request =
        CreateLecturerRequest.builder()
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

  @Disabled("As long PersonService only usage are these test i will disable them.")
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

    CreateStudentRequest student =
        CreateStudentRequest.builder()
            .dateOfBirth(LocalDate.of(2000, 2, 2))
            .address("Student Address")
            .phoneNumber("+49 222 222222")
            .matriculationNumber("QUERY001")
            .degreeProgram("Test Program")
            .semester(2)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("TEST-COHORT")
            .build();

    CreateEmployeeRequest employee =
        CreateEmployeeRequest.builder()
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
    CreateStudentRequest student1 =
        CreateStudentRequest.builder()
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
    CreateStudentRequest student2 =
        CreateStudentRequest.builder()
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



  @Disabled("As long PersonService only usage are these test i will disable them.")
  @Test
  @DisplayName("Should find persons by exact age correctly with real database")
  void shouldFindPersonsByExactAgeCorrectlyWithRealDatabase() {
    // Given - Create persons with specific birth dates
    LocalDate currentDate = LocalDate.now();
    Person person30 =
        Person.builder()
            .dateOfBirth(currentDate.minusYears(30)) // Exactly 30 years old
            .address("Person 30 Address")
            .phoneNumber("+49 400 400400")
            .build();

    Person anotherPerson30 =
        Person.builder()
            .dateOfBirth(
                currentDate.minusYears(30).minusDays(100)) // Also 30 years old but different day
            .address("Another Person 30 Address")
            .phoneNumber("+49 500 500500")
            .build();

    Person person40 =
        Person.builder()
            .dateOfBirth(currentDate.minusYears(40)) // 40 years old
            .address("Person 40 Address")
            .phoneNumber("+49 600 600600")
            .build();

    // Save persons
    Person savedYoungFirst = personService.create(person30);
    Person savedYoungSecond = personService.create(anotherPerson30);
    Person savedForty = personService.create(person40);

    // When & Then - Test exact age queries
    List<Person> personsAge30 = personService.findByAge(30);
    assertThat(personsAge30).hasSize(2);
    assertThat(personsAge30)
        .extracting(Person::getId)
        .containsExactlyInAnyOrder(savedYoungFirst.getId(), savedYoungSecond.getId());

    List<Person> personsAge40 = personService.findByAge(40);
    assertThat(personsAge40).hasSize(1);
    assertThat(personsAge40.get(0).getId()).isEqualTo(savedForty.getId());

    List<Person> personsAge50 = personService.findByAge(50);
    assertThat(personsAge50).isEmpty();
  }

  @Disabled("As long PersonService only usage are these test i will disable them.")
  @Test
  @DisplayName("Should handle edge cases for age calculations with real database")
  void shouldHandleEdgeCasesForAgeCalculationsWithRealDatabase() {
    // Given - Create persons with edge case birth dates
    LocalDate currentDate = LocalDate.now();

    // Person born exactly one year ago today
    Person personExactlyOneYear =
        Person.builder()
            .dateOfBirth(currentDate.minusYears(1))
            .address("One Year Old Address")
            .phoneNumber("+49 700 700700")
            .build();

    // Person born one year ago but tomorrow (not yet had birthday this year)
    Person personAlmostOneYear =
        Person.builder()
            .dateOfBirth(currentDate.minusYears(1).plusDays(1))
            .address("Almost One Year Old Address")
            .phoneNumber("+49 800 800800")
            .build();

    // Person born yesterday (0 years old)
    Person personYesterday =
        Person.builder()
            .dateOfBirth(currentDate.minusDays(1))
            .address("Born Yesterday Address")
            .phoneNumber("+49 900 900900")
            .build();

    // Save persons
    Person savedExactlyOne = personService.create(personExactlyOneYear);
    Person savedAlmostOne = personService.create(personAlmostOneYear);
    Person savedYesterday = personService.create(personYesterday);

    // When & Then - Test edge cases
    List<Person> age0 = personService.findByAge(0);
    assertThat(age0).hasSize(2); // Both yesterday and almost-one-year should be 0
    assertThat(age0)
        .extracting(Person::getId)
        .containsExactlyInAnyOrder(savedAlmostOne.getId(), savedYesterday.getId());

    List<Person> age1 = personService.findByAge(1);
    assertThat(age1).hasSize(1); // Only exactly-one-year should be 1
    assertThat(age1.get(0).getId()).isEqualTo(savedExactlyOne.getId());

    // Test count for very specific range
    long countInfants = personService.countByAgeRange(0, 1);
    assertThat(countInfants).isEqualTo(3); // All three persons are 0 or 1 years old
  }

  @Test
  @DisplayName("Should work correctly with inheritance hierarchy for age queries")
  void shouldWorkCorrectlyWithInheritanceHierarchyForAgeQueries() {
    // Given - Create different types of persons with known ages
    LocalDate currentDate = LocalDate.now();

    CreateStudentRequest youngStudent =
        CreateStudentRequest.builder()
            .dateOfBirth(currentDate.minusYears(20))
            .address("Young Student Address")
            .phoneNumber("+49 111 111111")
            .matriculationNumber("AGE001")
            .degreeProgram("Computer Science")
            .semester(2)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("AGE-COHORT")
            .build();

    CreateEmployeeRequest middleEmployee =
        CreateEmployeeRequest.builder()
            .dateOfBirth(currentDate.minusYears(35))
            .address("Middle Employee Address")
            .phoneNumber("+49 222 222222")
            .employeeNumber("AGE002")
            .department("IT")
            .officeNumber("AGE-101")
            .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
            .build();

    CreateLecturerRequest seniorLecturer =
        CreateLecturerRequest.builder()
            .dateOfBirth(currentDate.minusYears(50))
            .address("Senior Lecturer Address")
            .phoneNumber("+49 333 333333")
            .employeeNumber("AGE003")
            .department("Computer Science")
            .officeNumber("AGE-201")
            .workingTimeModel(Employee.WorkingTimeModel.FULL_TIME)
            .fieldChair("Software Engineering")
            .title("Prof. Dr.")
            .employmentStatus(Lecturer.EmploymentStatus.FULL_TIME_PERMANENT)
            .build();

    // Save entities
    Student savedStudent = studentService.create(youngStudent);
    Employee savedEmployee = employeeService.create(middleEmployee);
    Lecturer savedLecturer = lecturerService.create(seniorLecturer);

    // When & Then - Age queries should work across all person types
    List<Person> youngAdults = personService.findByAgeRange(18, 25);
    assertThat(youngAdults).hasSize(1);
    assertThat(youngAdults.get(0).getId()).isEqualTo(savedStudent.getId());

    List<Person> middleAged = personService.findByAgeRange(30, 40);
    assertThat(middleAged).hasSize(1);
    assertThat(middleAged.get(0).getId()).isEqualTo(savedEmployee.getId());

    List<Person> seniors = personService.findByAgeRange(45, 55);
    assertThat(seniors).hasSize(1);
    assertThat(seniors.get(0).getId()).isEqualTo(savedLecturer.getId());

    // Count across all inheritance types
    long totalCount = personService.countByAgeRange(0, 150);
    assertThat(totalCount).isEqualTo(3);

    // Exact age queries
    List<Person> exactly20 = personService.findByAge(20);
    assertThat(exactly20).hasSize(1);
    assertThat(exactly20.get(0).getId()).isEqualTo(savedStudent.getId());
  }

  private void createComplexTestData() {
    CreateStudentRequest enrolledStudent = createEnrolledStudent();
    CreateStudentRequest graduatedStudent = createGraduatedStudent();
    CreateLecturerRequest permanentLecturer = createPermanentLecturer();
    CreateLecturerRequest externalLecturer = createExternalLecturer();

    // Save all entities
    studentService.create(enrolledStudent);
    studentService.create(graduatedStudent);
    lecturerService.create(permanentLecturer);
    lecturerService.create(externalLecturer);
  }

  private CreateStudentRequest createEnrolledStudent() {
    return CreateStudentRequest.builder()
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

  private CreateStudentRequest createGraduatedStudent() {
    return CreateStudentRequest.builder()
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

  private CreateLecturerRequest createPermanentLecturer() {
    return CreateLecturerRequest.builder()
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

  private CreateLecturerRequest createExternalLecturer() {
    return CreateLecturerRequest.builder()
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
