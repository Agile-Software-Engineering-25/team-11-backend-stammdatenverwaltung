package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import com.ase.stammdatenverwaltung.dto.KeycloakUser;
import com.ase.stammdatenverwaltung.dto.PersonDetailsDTO;
import com.ase.stammdatenverwaltung.dto.UpdateUserRequest;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.mapper.PersonDtoMapper;
import com.ase.stammdatenverwaltung.mapper.UpdateUserMapper;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Person entities. Provides business logic for person-specific CRUD
 * operations and for enriching person data with information from Keycloak.
 */
@Service
@Transactional
@Validated
@RequiredArgsConstructor
@Slf4j
public class PersonService {

  private final PersonRepository personRepository;
  private final StudentRepository studentRepository;
  private final LecturerRepository lecturerRepository;
  private final EmployeeRepository employeeRepository;
  private final KeycloakClient keycloakClient;
  private final PersonDtoMapper personDtoMapper;
  private final UpdateUserMapper updateUserMapper;

  /**
   * Finds all persons and optionally enriches them with data from Keycloak.
   *
   * @param withDetails If true, enriches the person data with details from Keycloak.
   * @param userType Optional filter by user type (student, lecturer, employee).
   * @return A list of persons as DTOs.
   */
  @Transactional(readOnly = true)
  public Flux<PersonDetailsDTO> findAll(boolean withDetails, String userType) {
    log.debug("Finding all persons with userType: {}", userType);
    List<? extends Person> persons;
    if (userType != null) {
      if ("student".equalsIgnoreCase(userType)) {
        persons = studentRepository.findAll();
      } else if ("lecturer".equalsIgnoreCase(userType)) {
        persons = lecturerRepository.findAll();
      } else if ("employee".equalsIgnoreCase(userType)) {
        persons = employeeRepository.findAll();
      } else {
        persons = Collections.emptyList();
      }
    } else {
      persons = personRepository.findAll();
    }

    if (withDetails) {
      return Flux.fromIterable(persons)
          .flatMap(this::enrichPersonWithKeycloakData); // run enrichments concurrently
    }
    return Flux.fromIterable(persons).map(personDtoMapper::map);
  }

  /**
   * Finds a person by their ID and optionally enriches them with data from Keycloak.
   *
   * @param id The ID of the person to find.
   * @param withDetails If true, enriches the person data with details from Keycloak.
   * @return The person as a DTO.
   */
  @Transactional(readOnly = true)
  public Mono<PersonDetailsDTO> findById(String id, boolean withDetails) {
    log.debug("Getting person with ID: {}", id);
    Person person =
        personRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Person not found with ID: " + id));

    if (withDetails) {
      return enrichPersonWithKeycloakData(person);
    }

    return Mono.just(personDtoMapper.map(person));
  }

  private Mono<PersonDetailsDTO> enrichPersonWithKeycloakData(Person person) {
    try {
      Mono<PersonDetailsDTO> keycloakUsers =
          keycloakClient
              .findUserById(person.getId())
              .map(
                  keycloakUsers1 -> {
                    PersonDetailsDTO dto = personDtoMapper.map(person);
                    if (keycloakUsers1 != null && !keycloakUsers1.isEmpty()) {
                      KeycloakUser keycloakUser = keycloakUsers1.getFirst();
                      dto.setUsername(keycloakUser.getUsername());
                      dto.setFirstName(keycloakUser.getFirstName());
                      dto.setLastName(keycloakUser.getLastName());
                      dto.setEmail(keycloakUser.getEmail());
                    }
                    return dto;
                  });
      return keycloakUsers;
    } catch (Exception e) {
      log.error("Failed to fetch user details from Keycloak for person ID: {}", person.getId(), e);
      return Mono.just(personDtoMapper.map(person));
    }
  }

  /**
   * Creates a new person.
   *
   * @param person The person to create.
   * @return The created person.
   */
  public Person create(@Valid Person person) {
    log.debug("Creating new person");
    validatePersonForCreation(person);
    Person savedPerson = personRepository.save(person);
    log.info("Successfully created person with ID: {}", savedPerson.getId());
    return savedPerson;
  }

  /**
   * Updates an existing person.
   *
   * @param id The ID of the person to update.
   * @param updatedPerson The updated person data.
   * @return The updated person.
   */
  public Person update(String id, @Valid Person updatedPerson) {
    log.debug("Updating person with ID: {}", id);
    Person existingPerson =
        personRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Person not found with ID: " + id));

    existingPerson.setDateOfBirth(updatedPerson.getDateOfBirth());
    existingPerson.setAddress(updatedPerson.getAddress());
    existingPerson.setPhoneNumber(updatedPerson.getPhoneNumber());
    existingPerson.setPhotoUrl(updatedPerson.getPhotoUrl());

    Person savedPerson = personRepository.save(existingPerson);
    log.info("Successfully updated person with ID: {}", savedPerson.getId());
    return savedPerson;
  }

  /**
   * Applies partial updates to an existing person. Only fields provided in the request are updated;
   * remaining fields retain their existing values. Supports updating all person subtypes (Student,
   * Employee, Lecturer).
   *
   * <p>Validation is applied to ensure business logic constraints are maintained, maintaining
   * consistency with create() and update() operations.
   *
   * @param id The ID of the person to update.
   * @param updateRequest The request containing fields to update.
   * @return The updated person.
   * @throws EntityNotFoundException if the person is not found.
   * @throws IllegalArgumentException if the update violates business constraints (e.g., invalid
   *     date of birth).
   */
  public Person updatePartial(String id, @Valid UpdateUserRequest updateRequest) {
    log.debug("Applying partial update to person with ID: {}", id);
    Person existingPerson =
        personRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Person not found with ID: " + id));

    updateUserMapper.applyUpdates(existingPerson, updateRequest);

    // WHY: Apply validation after updates to maintain consistency with create() and update()
    // methods. Validation in mapper catches individual field violations; service-level validation
    // ensures complete entity consistency.
    validatePersonData(existingPerson);

    Person savedPerson = personRepository.save(existingPerson);
    log.info("Successfully applied partial updates to person with ID: {}", savedPerson.getId());
    return savedPerson;
  }

  /**
   * Deletes a person by their ID.
   *
   * @param id The ID of the person to delete.
   */
  public void deleteById(String id) {
    log.debug("Deleting person with ID: {}", id);
    if (!personRepository.existsById(id)) {
      throw new EntityNotFoundException("Person not found with ID: " + id);
    }
    personRepository.deleteById(id);
    log.info("Successfully deleted person with ID: {}", id);
  }

  private void validatePersonForCreation(Person person) {
    validatePersonData(person);
  }

  private void validatePersonData(Person person) {
    if (person.getDateOfBirth() != null && person.getDateOfBirth().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Date of birth cannot be in the future");
    }

    if (person.getDateOfBirth() != null
        && person
            .getDateOfBirth()
            .isBefore(LocalDate.now().minusYears(ValidationConstants.MAX_AGE_YEARS))) {
      throw new IllegalArgumentException(
          "Date of birth cannot be more than " + ValidationConstants.MAX_AGE_YEARS + " years ago");
    }
  }
}
