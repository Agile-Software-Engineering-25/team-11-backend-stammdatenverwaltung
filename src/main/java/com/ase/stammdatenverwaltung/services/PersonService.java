package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import com.ase.stammdatenverwaltung.dto.KeycloakUser;
import com.ase.stammdatenverwaltung.dto.PersonDetailsDTO;
import com.ase.stammdatenverwaltung.dto.UpdateUserRequest;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.mapper.PersonDtoMapper;
import com.ase.stammdatenverwaltung.mapper.UpdateUserMapper;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import com.ase.stammdatenverwaltung.security.UserInformationJWT;
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

  /**
   * Enriches a Person entity with additional user details from Keycloak.
   *
   * <p>This method retrieves user information from Keycloak using the person's ID and maps it to a
   * PersonDetailsDTO. If Keycloak data is available, the DTO is populated with username, first
   * name, last name, and email from the Keycloak user record.
   *
   * @param person the Person entity to be enriched with Keycloak data
   * @return a Mono containing a PersonDetailsDTO with enriched user details from Keycloak, or a
   *     basic PersonDetailsDTO if Keycloak data is unavailable or an error occurs
   * @throws No checked exceptions are thrown; errors are logged and handled gracefully
   */
  private Mono<PersonDetailsDTO> enrichPersonWithKeycloakData(Person person) {
    // WHY: Wrap enrichment in error handling to prevent cascading failures. If Keycloak is
    // unavailable or returns a user not found error, we still return basic person data without
    // enrichment instead of failing the entire request.
    return keycloakClient
        .findUserById(person.getId())
        .map(
            keycloakUsers -> {
              PersonDetailsDTO dto = personDtoMapper.map(person);
              if (keycloakUsers != null && !keycloakUsers.isEmpty()) {
                KeycloakUser keycloakUser = keycloakUsers.getFirst();
                dto.setUsername(keycloakUser.getUsername());
                dto.setFirstName(keycloakUser.getFirstName());
                dto.setLastName(keycloakUser.getLastName());
                dto.setEmail(keycloakUser.getEmail());
              }
              return dto;
            })
        .onErrorResume(
            error -> {
              log.warn(
                  "Keycloak unavailable for person ID {}, returning basic person data without enrichment",
                  person.getId());
              return Mono.just(personDtoMapper.map(person));
            });
  }

  /**
   * Creates a new person.
   *
   * @param person The person to create.
   * @return The created person.
   */
  public Person create(@Valid Person person) {
    log.debug("Creating new person");
    validatePersonData(person);
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

  /**
   * Checks if the current user has the specified permission on a person based on the person's type.
   * Used for authorization in @PreAuthorize expressions.
   *
   * @param userId The ID of the person to check access for.
   * @param permission The permission type (e.g., "Read", "Write", "Delete")
   * @return true if the user has the permission for the person's type, false if the person is not
   *     found or has an unsupported type
   */
  public boolean canAccessUser(String userId, String permission) {
    log.debug("canAccessUser() check - userId: {}, permission: {}", userId, permission);

    Person person = personRepository.findById(userId).orElse(null);

    if (person == null) {
      log.debug("User not found during permission check: {}", userId);
      return false;
    }

    String personType = person.getClass().getSimpleName();
    log.debug("Person found - ID: {}, Type: {}", userId, personType);

    String role;
    if (person instanceof Student) {
      role = "Area-3.Team-11." + permission + ".Student";
    } else if (person instanceof Employee) {
      role = "Area-3.Team-11." + permission + ".Employee";
    } else if (person instanceof Lecturer) {
      role = "Area-3.Team-11." + permission + ".Lecturer";
    } else {
      log.debug("Unknown person type during permission check for user: {}", userId);
      return false;
    }

    log.debug("Required role for permission check: {}", role);
    boolean hasAccess = UserInformationJWT.hasRole(role);
    log.debug(
        "Permission check result: {} (role '{}' required)", hasAccess ? "GRANTED" : "DENIED", role);

    return hasAccess;
  }

  /**
   * Validates the personal data of a person, specifically the date of birth.
   *
   * @param person the Person object whose data is to be validated
   * @throws IllegalArgumentException if the date of birth is in the future
   * @throws IllegalArgumentException if the date of birth is more than MAX_AGE_YEARS in the past
   */
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
