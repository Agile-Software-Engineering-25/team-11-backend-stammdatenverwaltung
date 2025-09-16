package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @InjectMocks private StudentService studentService;

  private Student testStudent;

  @BeforeEach
  void setUp() {
    testStudent =
        Student.builder()
            .id(1L)
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
    when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

    // When
    Optional<Student> result = studentService.findById(1L);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testStudent);
    verify(studentRepository).findById(1L);
  }

  @Test
  @DisplayName("Should get student by ID when student exists")
  void shouldGetStudentByIdWhenStudentExists() {
    // Given
    when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

    // When
    Student result = studentService.getById(1L);

    // Then
    assertThat(result).isEqualTo(testStudent);
    verify(studentRepository).findById(1L);
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when getting student by non-existent ID")
  void shouldThrowEntityNotFoundExceptionWhenGettingStudentByNonExistentId() {
    // Given
    when(studentRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> studentService.getById(1L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Student not found with ID: 1");
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
            .id(2L)
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
  @DisplayName("Should create student successfully")
  void shouldCreateStudentSuccessfully() {
    // Given
    Student newStudent =
        Student.builder()
            .dateOfBirth(LocalDate.of(2001, 4, 20))
            .address("New Student Address 456")
            .phoneNumber("+49 987 654321")
            .matriculationNumber("S2023003")
            .degreeProgram("Mathematics")
            .semester(1)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("MATH-T-23")
            .build();

    Student savedStudent =
        Student.builder()
            .id(3L)
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
    when(studentRepository.save(newStudent)).thenReturn(savedStudent);

    // When
    Student result = studentService.create(newStudent);

    // Then
    assertThat(result).isEqualTo(savedStudent);
    assertThat(result.getId()).isEqualTo(3L);
    verify(studentRepository).existsByMatriculationNumber("S2023003");
    verify(studentRepository).save(newStudent);
  }

  @Test
  @DisplayName("Should throw exception when creating student with duplicate matriculation number")
  void shouldThrowExceptionWhenCreatingStudentWithDuplicateMatriculationNumber() {
    // Given
    Student newStudent =
        Student.builder()
            .matriculationNumber("S2023001")
            .degreeProgram("Physics")
            .semester(1)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("PHY-T-23")
            .build();

    when(studentRepository.existsByMatriculationNumber("S2023001")).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> studentService.create(newStudent))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Student with matriculation number S2023001 already exists");
  }

  @Test
  @DisplayName("Should throw exception when creating student with invalid semester")
  void shouldThrowExceptionWhenCreatingStudentWithInvalidSemester() {
    // Given
    Student invalidStudent =
        Student.builder()
            .matriculationNumber("S2023004")
            .degreeProgram("Physics")
            .semester(0) // Invalid semester
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("PHY-T-23")
            .build();

    when(studentRepository.existsByMatriculationNumber("S2023004")).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> studentService.create(invalidStudent))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Semester must be positive");
  }

  @Test
  @DisplayName("Should throw exception when creating student with semester exceeding limit")
  void shouldThrowExceptionWhenCreatingStudentWithSemesterExceedingLimit() {
    // Given
    Student invalidStudent =
        Student.builder()
            .matriculationNumber("S2023005")
            .degreeProgram("Physics")
            .semester(21) // Exceeds limit
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("PHY-T-23")
            .build();

    when(studentRepository.existsByMatriculationNumber("S2023005")).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> studentService.create(invalidStudent))
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
            .id(1L)
            .dateOfBirth(LocalDate.of(2000, 8, 15))
            .address("Updated Student Address 999")
            .phoneNumber("+49 999 888777")
            .matriculationNumber("S2023001")
            .degreeProgram("Computer Science and AI")
            .semester(4)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("BIN-T-23")
            .build();

    when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
    when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

    // When
    Student result = studentService.update(1L, updatedData);

    // Then
    assertThat(result.getDegreeProgram()).isEqualTo("Computer Science and AI");
    assertThat(result.getSemester()).isEqualTo(4);
    assertThat(result.getAddress()).isEqualTo("Updated Student Address 999");
    verify(studentRepository).findById(1L);
    verify(studentRepository).save(any(Student.class));
  }

  @Test
  @DisplayName("Should delete student by ID when student exists")
  void shouldDeleteStudentByIdWhenStudentExists() {
    // Given
    when(studentRepository.existsById(1L)).thenReturn(true);

    // When
    studentService.deleteById(1L);

    // Then
    verify(studentRepository).existsById(1L);
    verify(studentRepository).deleteById(1L);
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
            .id(1L)
            .dateOfBirth(LocalDate.of(2000, 8, 15))
            .address("Student Address 123")
            .phoneNumber("+49 123 456789")
            .matriculationNumber("S2023001")
            .degreeProgram("Computer Science")
            .semester(3)
            .studyStatus(Student.StudyStatus.ON_LEAVE)
            .cohort("BIN-T-23")
            .build();

    when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
    when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);

    // When
    Student result = studentService.updateStudyStatus(1L, Student.StudyStatus.ON_LEAVE);

    // Then
    assertThat(result.getStudyStatus()).isEqualTo(Student.StudyStatus.ON_LEAVE);
    verify(studentRepository).findById(1L);
    verify(studentRepository).save(any(Student.class));
  }

  @Test
  @DisplayName("Should advance student to next semester")
  void shouldAdvanceStudentToNextSemester() {
    // Given
    Student advancedStudent =
        Student.builder()
            .id(1L)
            .dateOfBirth(LocalDate.of(2000, 8, 15))
            .address("Student Address 123")
            .phoneNumber("+49 123 456789")
            .matriculationNumber("S2023001")
            .degreeProgram("Computer Science")
            .semester(4)
            .studyStatus(Student.StudyStatus.ENROLLED)
            .cohort("BIN-T-23")
            .build();

    when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
    when(studentRepository.save(any(Student.class))).thenReturn(advancedStudent);

    // When
    Student result = studentService.advanceToNextSemester(1L);

    // Then
    assertThat(result.getSemester()).isEqualTo(4);
    verify(studentRepository).findById(1L);
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
