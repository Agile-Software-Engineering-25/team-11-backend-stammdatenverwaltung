package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.CreateLecturerRequest;
import com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest;
import com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.model.KeycloakGroup;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
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
 * Service class for managing Lecturer entities. Provides business logic for lecturer-specific CRUD
 * operations with proper validation and transaction management.
 */
@Service
@Transactional
@Validated
@RequiredArgsConstructor
@Slf4j
public class LecturerService {

  private final LecturerRepository lecturerRepository;
  private final KeycloakClient keycloakClient;

  /**
   * Creates a new lecturer from a request DTO. First checks if the user already exists in Keycloak,
   * then creates the user in Keycloak with the "lecturer" group, and finally stores the lecturer
   * data locally using the Keycloak user ID.
   *
   * @param request The request body containing the lecturer data.
   * @return The created lecturer.
   * @throws com.ase.stammdatenverwaltung.exceptions.KeycloakUserAlreadyExistsException if the user
   *     already exists in Keycloak
   */
  public Lecturer create(CreateLecturerRequest request) {
    log.debug("Creating new lecturer in field/chair: {}", request.getFieldChair());

    // Check if user already exists in Keycloak (prevents 409 conflicts)
    if (keycloakClient.userExists(request.getUsername())) {
      throw new com.ase.stammdatenverwaltung.exceptions.KeycloakUserAlreadyExistsException(
          request.getUsername());
    }

    // Create user in Keycloak with lecturer group
    CreateUserRequest keycloakRequest =
        CreateUserRequest.builder()
            .username(request.getUsername())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .groups(java.util.List.of(KeycloakGroup.LECTURER.getGroupName()))
            .build();

    CreateUserResponse keycloakResponse = keycloakClient.createUser(keycloakRequest).block();

    if (keycloakResponse == null || keycloakResponse.getId() == null) {
      throw new IllegalStateException(
          "Failed to create user in Keycloak for username: " + request.getUsername());
    }

    // Create lecturer entity with Keycloak user ID
    Lecturer lecturer =
        Lecturer.builder()
            .id(keycloakResponse.getId())
            .dateOfBirth(request.getDateOfBirth())
            .address(request.getAddress())
            .phoneNumber(request.getPhoneNumber())
            .photoUrl(request.getPhotoUrl())
            .employeeNumber(request.getEmployeeNumber())
            .department(request.getDepartment())
            .officeNumber(request.getOfficeNumber())
            .workingTimeModel(request.getWorkingTimeModel())
            .fieldChair(request.getFieldChair())
            .title(request.getTitle())
            .employmentStatus(request.getEmploymentStatus())
            .build();

    validateLecturerForCreation(lecturer);
    Lecturer savedLecturer = lecturerRepository.save(lecturer);
    log.info(
        "Successfully created lecturer with ID: {} in field/chair: {}",
        savedLecturer.getId(),
        savedLecturer.getFieldChair());
    return savedLecturer;
  }

  /**
   * Find a lecturer by their ID.
   *
   * @param id the lecturer ID
   * @return optional containing the lecturer if found
   */
  @Transactional(readOnly = true)
  public Optional<Lecturer> findById(String id) {
    log.debug("Finding lecturer with ID: {}", id);
    return lecturerRepository.findById(id);
  }

  /**
   * Get a lecturer by their ID, throwing an exception if not found.
   *
   * @param id the lecturer ID
   * @return the lecturer entity
   * @throws EntityNotFoundException if the lecturer is not found
   */
  @Transactional(readOnly = true)
  public Lecturer getById(String id) {
    log.debug("Getting lecturer with ID: {}", id);
    return lecturerRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Lecturer not found with ID: " + id));
  }

  /**
   * Find all lecturers in the system.
   *
   * @return list of all lecturers
   */
  @Transactional(readOnly = true)
  public List<Lecturer> findAll() {
    log.debug("Finding all lecturers");
    return lecturerRepository.findAll();
  }

  /**
   * Update an existing lecturer.
   *
   * @param id the lecturer ID to update
   * @param updatedLecturer the updated lecturer data
   * @return the updated lecturer entity
   * @throws EntityNotFoundException if the lecturer is not found
   */
  public Lecturer update(String id, @Valid Lecturer updatedLecturer) {
    log.debug("Updating lecturer with ID: {}", id);
    Lecturer existingLecturer = getById(id);

    existingLecturer.setFieldChair(updatedLecturer.getFieldChair());
    existingLecturer.setTitle(updatedLecturer.getTitle());
    existingLecturer.setEmploymentStatus(updatedLecturer.getEmploymentStatus());

    existingLecturer.setEmployeeNumber(updatedLecturer.getEmployeeNumber());
    existingLecturer.setDepartment(updatedLecturer.getDepartment());
    existingLecturer.setOfficeNumber(updatedLecturer.getOfficeNumber());
    existingLecturer.setWorkingTimeModel(updatedLecturer.getWorkingTimeModel());

    existingLecturer.setDateOfBirth(updatedLecturer.getDateOfBirth());
    existingLecturer.setAddress(updatedLecturer.getAddress());
    existingLecturer.setPhoneNumber(updatedLecturer.getPhoneNumber());
    existingLecturer.setPhotoUrl(updatedLecturer.getPhotoUrl());

    Lecturer savedLecturer = lecturerRepository.save(existingLecturer);
    log.info("Successfully updated lecturer with ID: {}", savedLecturer.getId());
    return savedLecturer;
  }

  /**
   * Delete a lecturer by their ID.
   *
   * @param id the lecturer ID to delete
   * @throws EntityNotFoundException if the lecturer is not found
   */
  public void deleteById(String id) {
    log.debug("Deleting lecturer with ID: {}", id);
    if (!lecturerRepository.existsById(id)) {
      throw new EntityNotFoundException("Lecturer not found with ID: " + id);
    }
    lecturerRepository.deleteById(id);
    log.info("Successfully deleted lecturer with ID: {}", id);
  }

  /**
   * Find lecturers by field or chair.
   *
   * @param fieldChair the field or chair name
   * @return list of lecturers in the given field/chair
   */
  @Transactional(readOnly = true)
  public List<Lecturer> findByFieldChair(String fieldChair) {
    log.debug("Finding lecturers by field/chair: {}", fieldChair);
    return lecturerRepository.findByFieldChairContainingIgnoreCase(fieldChair);
  }

  /**
   * Find lecturers by employment status.
   *
   * @param employmentStatus the employment status
   * @return list of lecturers with the given employment status
   */
  @Transactional(readOnly = true)
  public List<Lecturer> findByEmploymentStatus(Lecturer.EmploymentStatus employmentStatus) {
    log.debug("Finding lecturers by employment status: {}", employmentStatus);
    return lecturerRepository.findByEmploymentStatus(employmentStatus);
  }

  /**
   * Find lecturers by title.
   *
   * @param title the academic title
   * @return list of lecturers with the given title
   */
  @Transactional(readOnly = true)
  public List<Lecturer> findByTitle(String title) {
    log.debug("Finding lecturers by title: {}", title);
    return lecturerRepository.findByTitleContainingIgnoreCase(title);
  }

  /**
   * Find all permanent lecturers.
   *
   * @return list of permanent lecturers
   */
  @Transactional(readOnly = true)
  public List<Lecturer> findAllPermanentLecturers() {
    log.debug("Finding all permanent lecturers");
    return lecturerRepository.findAllPermanentLecturers();
  }

  /**
   * Find all external lecturers.
   *
   * @return list of external lecturers
   */
  @Transactional(readOnly = true)
  public List<Lecturer> findAllExternalLecturers() {
    log.debug("Finding all external lecturers");
    return lecturerRepository.findAllExternalLecturers();
  }

  /**
   * Find all professors.
   *
   * @return list of lecturers with professor titles
   */
  @Transactional(readOnly = true)
  public List<Lecturer> findAllProfessors() {
    log.debug("Finding all professors");
    return lecturerRepository.findAllProfessors();
  }

  /**
   * Update a lecturer's employment status.
   *
   * @param id the lecturer ID
   * @param newStatus the new employment status
   * @return the updated lecturer
   * @throws EntityNotFoundException if the lecturer is not found
   */
  public Lecturer updateEmploymentStatus(String id, Lecturer.EmploymentStatus newStatus) {
    log.debug("Updating employment status for lecturer ID: {} to {}", id, newStatus);
    Lecturer lecturer = getById(id);
    lecturer.setEmploymentStatus(newStatus);
    Lecturer savedLecturer = lecturerRepository.save(lecturer);
    log.info("Successfully updated employment status for lecturer ID: {} to {}", id, newStatus);
    return savedLecturer;
  }

  /**
   * Update a lecturer's title.
   *
   * @param id the lecturer ID
   * @param newTitle the new academic title
   * @return the updated lecturer
   * @throws EntityNotFoundException if the lecturer is not found
   */
  public Lecturer updateTitle(String id, String newTitle) {
    log.debug("Updating title for lecturer ID: {} to {}", id, newTitle);
    Lecturer lecturer = getById(id);
    lecturer.setTitle(newTitle);
    Lecturer savedLecturer = lecturerRepository.save(lecturer);
    log.info("Successfully updated title for lecturer ID: {} to {}", id, newTitle);
    return savedLecturer;
  }

  /**
   * Update a lecturer's field or chair.
   *
   * @param id the lecturer ID
   * @param newFieldChair the new field or chair
   * @return the updated lecturer
   * @throws EntityNotFoundException if the lecturer is not found
   */
  public Lecturer updateFieldChair(String id, String newFieldChair) {
    log.debug("Updating field/chair for lecturer ID: {} to {}", id, newFieldChair);
    Lecturer lecturer = getById(id);
    lecturer.setFieldChair(newFieldChair);
    Lecturer savedLecturer = lecturerRepository.save(lecturer);
    log.info("Successfully updated field/chair for lecturer ID: {} to {}", id, newFieldChair);
    return savedLecturer;
  }

  /**
   * Count lecturers by employment status.
   *
   * @param employmentStatus the employment status to count
   * @return number of lecturers with the given employment status
   */
  @Transactional(readOnly = true)
  public long countByEmploymentStatus(Lecturer.EmploymentStatus employmentStatus) {
    return lecturerRepository.countByEmploymentStatus(employmentStatus);
  }

  /**
   * Count lecturers by field or chair.
   *
   * @param fieldChair the field or chair to count
   * @return number of lecturers in the given field/chair
   */
  @Transactional(readOnly = true)
  public long countByFieldChair(String fieldChair) {
    return lecturerRepository.countByFieldChairContainingIgnoreCase(fieldChair);
  }

  private void validateLecturerForCreation(Lecturer lecturer) {
    validateLecturerData(lecturer);
  }

  private void validateLecturerData(Lecturer lecturer) {
    if (lecturer.getTitle() != null
        && lecturer.getTitle().contains("Prof")
        && lecturer.getEmploymentStatus() == Lecturer.EmploymentStatus.EXTERNAL) {
      log.warn(
          "External lecturer {} has professor title - this may require additional verification",
          lecturer.getId());
    }
  }
}
