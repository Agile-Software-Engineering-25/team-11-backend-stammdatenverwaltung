package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.CreateStudentRequest;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Tests")
class StudentServiceTest {

  @Mock private StudentRepository studentRepository;
  @Mock private KeycloakClient keycloakClient;

  @InjectMocks private StudentService studentService;

  private Student testStudent;

  @BeforeEach
  void setUp() {
    testStudent =
        Student.builder()
            .id("test-id")
            .dateOfBirth(LocalDate.of(2000, 8, 15))
            .address("Student Address 123")
            .phoneNumber("+49 123 456789")
            .matriculationNumber("S2023001")
            .degreeProgram("Computer Science")
            .semester(3)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("BIN-T-23")
            .build();
  }

  @Test
  @DisplayName("Should find student by ID when student exists")
  void shouldFindStudentByIdWhenStudentExists() {
    // Given
    when(studentRepository.findById("test-id")).thenReturn(Optional.of(testStudent));

    // When
    Optional<Student> result = studentService.findById("test-id");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testStudent);
    verify(studentRepository).findById("test-id");
  }

  @Test
  @DisplayName("Should get student by ID when student exists")
  void shouldGetStudentByIdWhenStudentExists() {
    // Given
    when(studentRepository.findById("test-id")).thenReturn(Optional.of(testStudent));

    // When
    Student result = studentService.getById("test-id");

    // Then
    assertThat(result).isEqualTo(testStudent);
    verify(studentRepository).findById("test-id");
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when getting student by non-existent ID")
  void shouldThrowEntityNotFoundExceptionWhenGettingStudentByNonExistentId() {
    // Given
    when(studentRepository.findById("test-id")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> studentService.getById("test-id"))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Student not found with ID: test-id");
  }

  @Test
  @DisplayName("Should find student by matriculation number")
  void shouldFindStudentByMatriculationNumber() {
    // Given
    when(studentRepository.findByMatriculationNumber("S2023001"))
        .thenReturn(Optional.of(testStudent));

    // When
    Optional<Student> result = studentService.findByMatriculationNumber("S2023001");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testStudent);
    verify(studentRepository).findByMatriculationNumber("S2023001");
  }

  @Test
  @DisplayName("Should find all students")
  void shouldFindAllStudents() {
    // Given
    Student student2 =
        Student.builder()
            .id("test-id-2")
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .matriculationNumber("S2023002")
            .degreeProgram("Business Administration")
            .semester(2)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("BWL-T-23")
            .build();
    List<Student> students = Arrays.asList(testStudent, student2);
    when(studentRepository.findAll()).thenReturn(students);

    // When
    List<Student> result = studentService.findAll();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(testStudent, student2);
    verify(studentRepository).findAll();
  }

  @Test
  @DisplayName("Should create student successfully from DTO")
  void shouldCreateStudentSuccessfullyFromDto() {
    // Given
    CreateStudentRequest request = new CreateStudentRequest();
    request.setUsername("new.student@example.com");
    request.setFirstName("New");
    request.setLastName("Student");
    request.setEmail("new.student@example.com");
    request.setDateOfBirth(LocalDate.of(2001, 4, 20));
    request.setAddress("New Student Address 456");
    request.setPhoneNumber("+49 987 654321");
    request.setMatriculationNumber("S2023003");
    request.setDegreeProgram("Mathematics");
    request.setSemester(1);
    request.setStudyStatus(Student.StudyStatus.ENROLLED);
    request.setCohort("MATH-T-23");

    // Mock Keycloak response
    com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse keycloakResponse =
        new com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse();
    keycloakResponse.setId("keycloak-generated-id");
    keycloakResponse.setUsername("new.student@example.com");

    when(keycloakClient.createUser(
            any(com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest.class)))
        .thenReturn(reactor.core.publisher.Mono.just(keycloakResponse));

    Student savedStudent =
        Student.builder()
            .id("keycloak-generated-id")
            .dateOfBirth(LocalDate.of(2001, 4, 20))
            .address("New Student Address 456")
            .phoneNumber("+49 987 654321")
            .matriculationNumber("S2023003")
            .degreeProgram("Mathematics")
            .semester(1)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("MATH-T-23")
            .build();

    when(studentRepository.existsByMatriculationNumber("S2023003")).thenReturn(false);
    when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

    // When
    Student result = studentService.create(request);

    // Then
    assertThat(result).isEqualTo(savedStudent);
    assertThat(result.getId()).isEqualTo("keycloak-generated-id");
    verify(keycloakClient)
        .createUser(any(com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest.class));
    verify(studentRepository).existsByMatriculationNumber("S2023003");
    verify(studentRepository).save(any(Student.class));
  }

  @Test
  @DisplayName("Should throw exception when creating student with duplicate matriculation number")
  void shouldThrowExceptionWhenCreatingStudentWithDuplicateMatriculationNumber() {
    // Given
    CreateStudentRequest request = new CreateStudentRequest();
    request.setUsername("duplicate@example.com");
    request.setFirstName("Duplicate");
    request.setLastName("Student");
    request.setEmail("duplicate@example.com");
    request.setMatriculationNumber("S2023001");

    // Mock Keycloak response
    com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse keycloakResponse =
        new com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse();
    keycloakResponse.setId("keycloak-id");

    when(keycloakClient.createUser(
            any(com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest.class)))
        .thenReturn(reactor.core.publisher.Mono.just(keycloakResponse));
    when(studentRepository.existsByMatriculationNumber("S2023001")).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> studentService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Student with matriculation number S2023001 already exists");
  }

  @Test
  @DisplayName("Should throw exception when creating student with invalid semester")
  void shouldThrowExceptionWhenCreatingStudentWithInvalidSemester() {
    // Given
    CreateStudentRequest request = new CreateStudentRequest();
    request.setUsername("invalid@example.com");
    request.setFirstName("Invalid");
    request.setLastName("Student");
    request.setEmail("invalid@example.com");
    request.setMatriculationNumber("S2023004");
    request.setSemester(0);

    // Mock Keycloak response
    com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse keycloakResponse =
        new com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse();
    keycloakResponse.setId("keycloak-id");

    when(keycloakClient.createUser(
            any(com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest.class)))
        .thenReturn(reactor.core.publisher.Mono.just(keycloakResponse));
    when(studentRepository.existsByMatriculationNumber("S2023004")).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> studentService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Semester must be positive");
  }

  @Test
  @DisplayName("Should throw exception when creating student with semester exceeding limit")
  void shouldThrowExceptionWhenCreatingStudentWithSemesterExceedingLimit() {
    // Given
    CreateStudentRequest request = new CreateStudentRequest();
    request.setUsername("exceed@example.com");
    request.setFirstName("Exceed");
    request.setLastName("Student");
    request.setEmail("exceed@example.com");
    request.setMatriculationNumber("S2023005");
    request.setSemester(21);

    // Mock Keycloak response
    com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse keycloakResponse =
        new com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse();
    keycloakResponse.setId("keycloak-id");

    when(keycloakClient.createUser(
            any(com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest.class)))
        .thenReturn(reactor.core.publisher.Mono.just(keycloakResponse));
    when(studentRepository.existsByMatriculationNumber("S2023005")).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> studentService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Semester cannot exceed 20");
  }

  @Test
  @DisplayName("Should update student successfully")
  void shouldUpdateStudentSuccessfully() {
    // Given
    Student updatedData =
        Student.builder()
            .dateOfBirth(LocalDate.of(2000, 8, 15))
            .address("Updated Student Address 999")
            .phoneNumber("+49 999 888777")
            .matriculationNumber("S2023001")
            .degreeProgram("Computer Science and AI")
            .semester(4)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("BIN-T-23")
            .build();

    Student savedStudent =
        Student.builder()
            .id("test-id")
            .dateOfBirth(LocalDate.of(2000, 8, 15))
            .address("Updated Student Address 999")
            .phoneNumber("+49 999 888777")
            .matriculationNumber("S2023001")
            .degreeProgram("Computer Science and AI")
            .semester(4)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("BIN-T-23")
            .build();

    when(studentRepository.findById("test-id")).thenReturn(Optional.of(testStudent));
    when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

    // When
    Student result = studentService.update("test-id", updatedData);

    // Then
    assertThat(result.getDegreeProgram()).isEqualTo("Computer Science and AI");
    assertThat(result.getSemester()).isEqualTo(4);
    assertThat(result.getAddress()).isEqualTo("Updated Student Address 999");
    verify(studentRepository).findById("test-id");
    verify(studentRepository).save(any(Student.class));
  }

  @Test
  @DisplayName("Should delete student by ID when student exists")
  void shouldDeleteStudentByIdWhenStudentExists() {
    // Given
    when(studentRepository.existsById("test-id")).thenReturn(true);

    // When
    studentService.deleteById("test-id");

    // Then
    verify(studentRepository).existsById("test-id");
    verify(studentRepository).deleteById("test-id");
  }

  @Test
  @DisplayName("Should find students by cohort")
  void shouldFindStudentsByCohort() {
    // Given
    List<Student> students = Arrays.asList(testStudent);
    when(studentRepository.findByCohort("BIN-T-23")).thenReturn(students);

    // When
    List<Student> result = studentService.findByCohort("BIN-T-23");

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testStudent);
    verify(studentRepository).findByCohort("BIN-T-23");
  }

  @Test
  @DisplayName("Should find students by study status")
  void shouldFindStudentsByStudyStatus() {
    // Given
    List<Student> students = Arrays.asList(testStudent);
    when(studentRepository.findByStudyStatus(Student.StudyStatus.ENROLLED)).thenReturn(students);

    // When
    List<Student> result = studentService.findByStudyStatus(Student.StudyStatus.ENROLLED);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testStudent);
    verify(studentRepository).findByStudyStatus(Student.StudyStatus.ENROLLED);
  }

  @Test
  @DisplayName("Should find all enrolled students")
  void shouldFindAllEnrolledStudents() {
    // Given
    List<Student> students = Arrays.asList(testStudent);
    when(studentRepository.findAllEnrolledStudents()).thenReturn(students);

    // When
    List<Student> result = studentService.findEnrolledStudents();

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testStudent);
    verify(studentRepository).findAllEnrolledStudents();
  }

  @Test
  @DisplayName("Should update student study status")
  void shouldUpdateStudentStudyStatus() {
    // Given
    Student updatedStudent =
        Student.builder()
            .id("test-id")
            .dateOfBirth(LocalDate.of(2000, 8, 15))
            .address("Student Address 123")
            .phoneNumber("+49 123 456789")
            .matriculationNumber("S2023001")
            .degreeProgram("Computer Science")
            .semester(3)
            .studyStatus(Student.StudyStatus.ON_LEAVE)
            .cohort("BIN-T-23")
            .build();

    when(studentRepository.findById("test-id")).thenReturn(Optional.of(testStudent));
    when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);

    // When
    Student result = studentService.updateStudyStatus("test-id", Student.StudyStatus.ON_LEAVE);

    // Then
    assertThat(result.getStudyStatus()).isEqualTo(Student.StudyStatus.ON_LEAVE);
    verify(studentRepository).findById("test-id");
    verify(studentRepository).save(any(Student.class));
  }

  @Test
  @DisplayName("Should advance student to next semester")
  void shouldAdvanceStudentToNextSemester() {
    // Given
    Student advancedStudent =
        Student.builder()
            .id("test-id")
            .dateOfBirth(LocalDate.of(2000, 8, 15))
            .address("Student Address 123")
            .phoneNumber("+49 123 456789")
            .matriculationNumber("S2023001")
            .degreeProgram("Computer Science")
            .semester(4)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("BIN-T-23")
            .build();

    when(studentRepository.findById("test-id")).thenReturn(Optional.of(testStudent));
    when(studentRepository.save(any(Student.class))).thenReturn(advancedStudent);

    // When
    Student result = studentService.advanceToNextSemester("test-id");

    // Then
    assertThat(result.getSemester()).isEqualTo(4);
    verify(studentRepository).findById("test-id");
    verify(studentRepository).save(any(Student.class));
  }

  @Test
  @DisplayName("Should count students by cohort")
  void shouldCountStudentsByCohort() {
    // Given
    when(studentRepository.countByCohort("BIN-T-23")).thenReturn(5L);

    // When
    long result = studentService.countByCohort("BIN-T-23");

    // Then
    assertThat(result).isEqualTo(5L);
    verify(studentRepository).countByCohort("BIN-T-23");
  }
}
