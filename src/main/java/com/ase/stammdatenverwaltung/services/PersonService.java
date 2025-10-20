package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.EmployeeDetailsDTO;
import com.ase.stammdatenverwaltung.dto.KeycloakUser;
import com.ase.stammdatenverwaltung.dto.LecturerDetailsDTO;
import com.ase.stammdatenverwaltung.dto.PersonDetailsDTO;
import com.ase.stammdatenverwaltung.dto.StudentDetailsDTO;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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

  private static final int MAX_AGE_YEARS = 150;
  private final PersonRepository personRepository;
  private final StudentRepository studentRepository;
  private final LecturerRepository lecturerRepository;
  private final EmployeeRepository employeeRepository;
  private final KeycloakClient keycloakClient;

  /**
   * Finds all persons and optionally enriches them with data from Keycloak.
   *
   * @param withDetails If true, enriches the person data with details from Keycloak.
   * @param userType Optional filter by user type (student, lecturer, employee).
   * @return A list of persons as DTOs.
   */
  @Transactional(readOnly = true)
  public List<PersonDetailsDTO> findAll(boolean withDetails, String userType) {
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
      return persons.stream().map(this::enrichPersonWithKeycloakData).collect(Collectors.toList());
    }
    return persons.stream()
        .map(
            p -> {
              if (p instanceof Lecturer) {
                return new LecturerDetailsDTO((Lecturer) p);
              } else if (p instanceof Employee) {
                return new EmployeeDetailsDTO((Employee) p);
              } else if (p instanceof Student) {
                return new StudentDetailsDTO((Student) p);
              } else {
                return new PersonDetailsDTO(p);
              }
            })
        .collect(Collectors.toList());
  }

  /**
   * Finds a person by their ID and optionally enriches them with data from Keycloak.
   *
   * @param id The ID of the person to find.
   * @param withDetails If true, enriches the person data with details from Keycloak.
   * @return The person as a DTO.
   */
  @Transactional(readOnly = true)
  public PersonDetailsDTO findById(String id, boolean withDetails) {
    log.debug("Getting person with ID: {}", id);
    Person person =
        personRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Person not found with ID: " + id));

    if (withDetails) {
      return enrichPersonWithKeycloakData(person);
    }

    if (person instanceof Lecturer) {
      return new LecturerDetailsDTO((Lecturer) person);
    } else if (person instanceof Employee) {
      return new EmployeeDetailsDTO((Employee) person);
    } else if (person instanceof Student) {
      return new StudentDetailsDTO((Student) person);
    } else {
      return new PersonDetailsDTO(person);
    }
  }

  private PersonDetailsDTO enrichPersonWithKeycloakData(Person person) {
    PersonDetailsDTO dto;
    if (person instanceof Lecturer) {
      dto = new LecturerDetailsDTO((Lecturer) person);
    } else if (person instanceof Employee) {
      dto = new EmployeeDetailsDTO((Employee) person);
    } else if (person instanceof Student) {
      dto = new StudentDetailsDTO((Student) person);
    } else {
      dto = new PersonDetailsDTO(person);
    }

    try {
      List<KeycloakUser> keycloakUsers = keycloakClient.findUserById(person.getId()).block();
      if (keycloakUsers != null && !keycloakUsers.isEmpty()) {
        KeycloakUser keycloakUser = keycloakUsers.get(0);
        dto.setUsername(keycloakUser.getUsername());
        dto.setFirstName(keycloakUser.getFirstName());
        dto.setLastName(keycloakUser.getLastName());
        dto.setEmail(keycloakUser.getEmail());
      }
    } catch (Exception e) {
      log.error("Failed to fetch user details from Keycloak for person ID: {}", person.getId(), e);
    }
    return dto;
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
        && person.getDateOfBirth().isBefore(LocalDate.now().minusYears(MAX_AGE_YEARS))) {
      throw new IllegalArgumentException(
          "Date of birth cannot be more than " + MAX_AGE_YEARS + " years ago");
    }
  }
}
