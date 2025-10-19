package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.CreateStudentRequest;
import com.ase.stammdatenverwaltung.dto.keycloak.KeycloakUser;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service class for managing Student entities. Provides business logic for student-specific CRUD
 * operations with proper validation and transaction management.
 */
@Service
@Transactional
@Validated
@RequiredArgsConstructor
@Slf4j
public class StudentService {

  private static final int MAX_SEMESTER_COUNT = 20;

  private final StudentRepository studentRepository;
  private final KeycloakClient keycloakClient;

  /**
   * Creates a new student from a request DTO.
   *
   * @param request The request body containing the student data.
   * @return The created student.
   */
  public Student create(CreateStudentRequest request, String userId) {
    log.debug(
        "Creating new student with matriculation number: {}", request.getMatriculationNumber());

    KeycloakUser keycloakUser = keycloakClient.getUserInfo(userId).block();

    Student student =
        Student.builder()
            .id(userId)

            .dateOfBirth(request.getDateOfBirth())
            .address(request.getAddress())
            .phoneNumber(request.getPhoneNumber())
            .photoUrl(request.getPhotoUrl())
            .matriculationNumber(request.getMatriculationNumber())
            .degreeProgram(request.getDegreeProgram())
            .semester(request.getSemester())
            .studyStatus(request.getStudyStatus())
            .cohort(request.getCohort())
            .build();

    validateStudentForCreation(student);
    Student savedStudent = studentRepository.save(student);
    log.info(
        "Successfully created student with ID: {} and matriculation number: {}",
        savedStudent.getId(),
        savedStudent.getMatriculationNumber());
    return savedStudent;
  }

  /**
   * Find a student by their ID.
   *
   * @param id the student ID
   * @return optional containing the student if found
   */
  @Transactional(readOnly = true)
  public Optional<Student> findById(String id) {
    log.debug("Finding student with ID: {}", id);
    return studentRepository.findById(id);
  }

  /**
   * Get a student by their ID, throwing an exception if not found.
   *
   * @param id the student ID
   * @return the student entity
   * @throws EntityNotFoundException if the student is not found
   */
  @Transactional(readOnly = true)
  public Student getById(String id) {
    log.debug("Getting student with ID: {}", id);
    return studentRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + id));
  }

  /**
   * Find a student by their matriculation number.
   *
   * @param matriculationNumber the unique matriculation number
   * @return optional containing the student if found
   */
  @Transactional(readOnly = true)
  public Optional<Student> findByMatriculationNumber(String matriculationNumber) {
    log.debug("Finding student by matriculation number: {}", matriculationNumber);
    return studentRepository.findByMatriculationNumber(matriculationNumber);
  }

  /**
   * Find all students in the system.
   *
   * @return list of all students
   */
  @Transactional(readOnly = true)
  public List<Student> findAll() {
    log.debug("Finding all students");
    return studentRepository.findAll();
  }

  /**
   * Update an existing student.
   *
   * @param id the student ID to update
   * @param updatedStudent the updated student data
   * @return the updated student entity
   * @throws EntityNotFoundException if the student is not found
   */
  public Student update(String id, @Valid Student updatedStudent) {
    log.debug("Updating student with ID: {}", id);
    Student existingStudent = getById(id);

    existingStudent.setMatriculationNumber(updatedStudent.getMatriculationNumber());
    existingStudent.setDegreeProgram(updatedStudent.getDegreeProgram());
    existingStudent.setSemester(updatedStudent.getSemester());
    existingStudent.setStudyStatus(updatedStudent.getStudyStatus());
    existingStudent.setCohort(updatedStudent.getCohort());

    existingStudent.setDateOfBirth(updatedStudent.getDateOfBirth());
    existingStudent.setAddress(updatedStudent.getAddress());
    existingStudent.setPhoneNumber(updatedStudent.getPhoneNumber());
    existingStudent.setPhotoUrl(updatedStudent.getPhotoUrl());

    Student savedStudent = studentRepository.save(existingStudent);
    log.info("Successfully updated student with ID: {}", savedStudent.getId());
    return savedStudent;
  }

  /**
   * Delete a student by their ID.
   *
   * @param id the student ID to delete
   * @throws EntityNotFoundException if the student is not found
   */
  public void deleteById(String id) {
    log.debug("Deleting student with ID: {}", id);
    if (!studentRepository.existsById(id)) {
      throw new EntityNotFoundException("Student not found with ID: " + id);
    }
    studentRepository.deleteById(id);
    log.info("Successfully deleted student with ID: {}", id);
  }

  /**
   * Find students by cohort.
   *
   * @param cohort the cohort identifier
   * @return list of students in the given cohort
   */
  @Transactional(readOnly = true)
  public List<Student> findByCohort(String cohort) {
    log.debug("Finding students by cohort: {}", cohort);
    return studentRepository.findByCohort(cohort);
  }

  /**
   * Find students by study status.
   *
   * @param studyStatus the study status
   * @return list of students with the given status
   */
  @Transactional(readOnly = true)
  public List<Student> findByStudyStatus(Student.StudyStatus studyStatus) {
    log.debug("Finding students by study status: {}", studyStatus);
    return studentRepository.findByStudyStatus(studyStatus);
  }

  /**
   * Find all currently enrolled students.
   *
   * @return list of enrolled students
   */
  @Transactional(readOnly = true)
  public List<Student> findEnrolledStudents() {
    log.debug("Finding all enrolled students");
    return studentRepository.findAllEnrolledStudents();
  }

  /**
   * Update a student's study status.
   *
   * @param id the student ID
   * @param newStatus the new study status
   * @return the updated student
   * @throws EntityNotFoundException if the student is not found
   */
  public Student updateStudyStatus(String id, Student.StudyStatus newStatus) {
    log.debug("Updating study status for student ID: {} to {}", id, newStatus);
    Student student = getById(id);
    student.setStudyStatus(newStatus);
    Student savedStudent = studentRepository.save(student);
    log.info("Successfully updated study status for student ID: {} to {}", id, newStatus);
    return savedStudent;
  }

  /**
   * Advance a student to the next semester.
   *
   * @param id the student ID
   * @return the updated student
   * @throws EntityNotFoundException if the student is not found
   */
  public Student advanceToNextSemester(String id) {
    log.debug("Advancing student ID: {} to next semester", id);
    Student student = getById(id);
    student.setSemester(student.getSemester() + 1);
    Student savedStudent = studentRepository.save(student);
    log.info("Successfully advanced student ID: {} to semester {}", id, savedStudent.getSemester());
    return savedStudent;
  }

  /**
   * Count students by cohort.
   *
   * @param cohort the cohort to count
   * @return number of students in the cohort
   */
  @Transactional(readOnly = true)
  public long countByCohort(String cohort) {
    return studentRepository.countByCohort(cohort);
  }

  /**
   * Validate student data for creation.
   *
   * @param student the student to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateStudentForCreation(Student student) {
    if (studentRepository.existsByMatriculationNumber(student.getMatriculationNumber())) {
      throw new IllegalArgumentException(
          "Student with matriculation number "
              + student.getMatriculationNumber()
              + " already exists");
    }
    validateStudentData(student);
  }

  /**
   * Validate student data for business rules.
   *
   * @param student the student to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateStudentData(Student student) {
    if (student.getSemester() != null && student.getSemester() < 1) {
      throw new IllegalArgumentException("Semester must be positive");
    }
    if (student.getSemester() != null && student.getSemester() > MAX_SEMESTER_COUNT) {
      throw new IllegalArgumentException("Semester cannot exceed " + MAX_SEMESTER_COUNT);
    }
  }
}
