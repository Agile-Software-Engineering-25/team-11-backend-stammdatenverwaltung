package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.KeycloakUser;
import com.ase.stammdatenverwaltung.dto.PersonDetailsDTO;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
@Slf4j
public class PersonService {

  private static final int MAX_AGE_YEARS = 150;
  private final PersonRepository personRepository;
  private final KeycloakClient keycloakClient;

  @Transactional(readOnly = true)
  public List<PersonDetailsDTO> findAll(boolean withDetails) {
    log.debug("Finding all persons");
    List<Person> persons = personRepository.findAll();
    if (withDetails) {
      return persons.stream().map(this::enrichPersonWithKeycloakData).collect(Collectors.toList());
    }
    return persons.stream().map(PersonDetailsDTO::new).collect(Collectors.toList());
  }

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
    return new PersonDetailsDTO(person);
  }

  private PersonDetailsDTO enrichPersonWithKeycloakData(Person person) {
    PersonDetailsDTO dto = new PersonDetailsDTO(person);
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

  public Person create(@Valid Person person) {
    log.debug("Creating new person");
    validatePersonForCreation(person);
    Person savedPerson = personRepository.save(person);
    log.info("Successfully created person with ID: {}", savedPerson.getId());
    return savedPerson;
  }

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
