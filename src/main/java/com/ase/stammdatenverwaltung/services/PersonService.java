package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service class for managing Person entities. Provides business logic for person-specific CRUD
 * operations with proper validation and transaction management. This service handles the base
 * Person entity and can be used for operations across all person types due to inheritance.
 */
@Service
@Transactional
@Validated
@RequiredArgsConstructor
@Slf4j
public class PersonService {

  private static final int MAX_AGE_YEARS = 150;

  private final PersonRepository personRepository;

  /**
   * Find a person by their ID.
   *
   * @param id the person ID
   * @return optional containing the person if found
   */
  @Transactional(readOnly = true)
  public Optional<Person> findById(Long id) {
    log.debug("Finding person with ID: {}", id);
    return personRepository.findById(id);
  }

  /**
   * Get a person by their ID, throwing an exception if not found.
   *
   * @param id the person ID
   * @return the person entity
   * @throws EntityNotFoundException if the person is not found
   */
  @Transactional(readOnly = true)
  public Person getById(Long id) {
    log.debug("Getting person with ID: {}", id);
    return personRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Person not found with ID: " + id));
  }

  /**
   * Find all persons in the system.
   *
   * @return list of all persons
   */
  @Transactional(readOnly = true)
  public List<Person> findAll() {
    log.debug("Finding all persons");
    return personRepository.findAll();
  }

  /**
   * Create a new person.
   *
   * @param person the person entity to create
   * @return the created person entity
   */
  public Person create(@Valid Person person) {
    log.debug("Creating new person");
    validatePersonForCreation(person);
    Person savedPerson = personRepository.save(person);
    log.info("Successfully created person with ID: {}", savedPerson.getId());
    return savedPerson;
  }

  /**
   * Update an existing person.
   *
   * @param id the person ID to update
   * @param updatedPerson the updated person data
   * @return the updated person entity
   * @throws EntityNotFoundException if the person is not found
   */
  public Person update(Long id, @Valid Person updatedPerson) {
    log.debug("Updating person with ID: {}", id);
    Person existingPerson = getById(id);

    // Update person fields
    existingPerson.setDateOfBirth(updatedPerson.getDateOfBirth());
    existingPerson.setAddress(updatedPerson.getAddress());
    existingPerson.setPhoneNumber(updatedPerson.getPhoneNumber());
    existingPerson.setPhotoUrl(updatedPerson.getPhotoUrl());

    Person savedPerson = personRepository.save(existingPerson);
    log.info("Successfully updated person with ID: {}", savedPerson.getId());
    return savedPerson;
  }

  /**
   * Delete a person by their ID.
   *
   * @param id the person ID to delete
   * @throws EntityNotFoundException if the person is not found
   */
  public void deleteById(Long id) {
    log.debug("Deleting person with ID: {}", id);
    if (!personRepository.existsById(id)) {
      throw new EntityNotFoundException("Person not found with ID: " + id);
    }
    personRepository.deleteById(id);
    log.info("Successfully deleted person with ID: {}", id);
  }

  /**
   * Find persons by phone number.
   *
   * @param phoneNumber the phone number to search for
   * @return list of persons with the given phone number
   */
  @Transactional(readOnly = true)
  public List<Person> findByPhoneNumber(String phoneNumber) {
    log.debug("Finding persons by phone number: {}", phoneNumber);
    return personRepository.findByPhoneNumber(phoneNumber);
  }

  /**
   * Find persons by address containing the given text.
   *
   * @param address the address text to search for
   * @return list of persons with addresses containing the text
   */
  @Transactional(readOnly = true)
  public List<Person> findByAddressContaining(String address) {
    log.debug("Finding persons by address containing: {}", address);
    return personRepository.findByAddressContainingIgnoreCase(address);
  }

  /**
   * Find persons born between two dates.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @return list of persons born between the dates
   */
  @Transactional(readOnly = true)
  public List<Person> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate) {
    log.debug("Finding persons born between {} and {}", startDate, endDate);
    return personRepository.findByDateOfBirthBetween(startDate, endDate);
  }

  /**
   * Find the oldest person in the system.
   *
   * @return optional containing the oldest person
   */
  @Transactional(readOnly = true)
  public Optional<Person> findOldestPerson() {
    log.debug("Finding oldest person");
    return personRepository.findOldestPerson();
  }

  /**
   * Find the youngest person in the system.
   *
   * @return optional containing the youngest person
   */
  @Transactional(readOnly = true)
  public Optional<Person> findYoungestPerson() {
    log.debug("Finding youngest person");
    return personRepository.findYoungestPerson();
  }

  /**
   * Find persons within a specific age range.
   *
   * @param minAge the minimum age (inclusive)
   * @param maxAge the maximum age (inclusive)
   * @return list of persons within the age range
   * @throws IllegalArgumentException if age parameters are invalid
   */
  @Transactional(readOnly = true)
  public List<Person> findByAgeRange(int minAge, int maxAge) {
    log.debug("Finding persons with age between {} and {}", minAge, maxAge);
    validateAgeRange(minAge, maxAge);

    LocalDate currentDate = LocalDate.now();
    LocalDate maxBirthDate = currentDate.minusYears(minAge); // Earliest birth date for minAge
    LocalDate minBirthDate = currentDate.minusYears(maxAge + 1); // Latest birth date for maxAge

    return personRepository.findByAgeRange(minBirthDate, maxBirthDate);
  }

  /**
   * Count persons within a specific age range.
   *
   * @param minAge the minimum age (inclusive)
   * @param maxAge the maximum age (inclusive)
   * @return count of persons within the age range
   * @throws IllegalArgumentException if age parameters are invalid
   */
  @Transactional(readOnly = true)
  public long countByAgeRange(int minAge, int maxAge) {
    log.debug("Counting persons with age between {} and {}", minAge, maxAge);
    validateAgeRange(minAge, maxAge);

    LocalDate currentDate = LocalDate.now();
    LocalDate maxBirthDate = currentDate.minusYears(minAge); // Earliest birth date for minAge
    LocalDate minBirthDate = currentDate.minusYears(maxAge + 1); // Latest birth date for maxAge

    return personRepository.countByAgeRange(minBirthDate, maxBirthDate);
  }

  /**
   * Find persons with an exact age.
   *
   * @param age the exact age to search for
   * @return list of persons with the exact age
   * @throws IllegalArgumentException if age parameter is invalid
   */
  @Transactional(readOnly = true)
  public List<Person> findByAge(int age) {
    log.debug("Finding persons with exact age: {}", age);
    validateAge(age);

    LocalDate currentDate = LocalDate.now();
    LocalDate maxBirthDate = currentDate.minusYears(age); // Earliest birth date for this age
    LocalDate minBirthDate = currentDate.minusYears(age + 1); // Latest birth date for this age

    return personRepository.findByAge(minBirthDate, maxBirthDate);
  }

  // Note: Age-based counting removed due to complexity with date arithmetic in JPQL
  // Can be implemented later with native queries if needed

  /**
   * Check if a person exists with the given ID.
   *
   * @param id the person ID to check
   * @return true if the person exists
   */
  @Transactional(readOnly = true)
  public boolean existsById(Long id) {
    return personRepository.existsById(id);
  }

  /**
   * Validate person data for creation.
   *
   * @param person the person to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validatePersonForCreation(Person person) {
    validatePersonData(person);
  }

  /**
   * Validate person data for business rules.
   *
   * @param person the person to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validatePersonData(Person person) {
    if (person.getDateOfBirth() != null && person.getDateOfBirth().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Date of birth cannot be in the future");
    }

    // Additional business validation can be added here
    if (person.getDateOfBirth() != null
        && person.getDateOfBirth().isBefore(LocalDate.now().minusYears(MAX_AGE_YEARS))) {
      throw new IllegalArgumentException(
          "Date of birth cannot be more than " + MAX_AGE_YEARS + " years ago");
    }
  }

  /**
   * Validate age range parameters.
   *
   * @param minAge the minimum age
   * @param maxAge the maximum age
   * @throws IllegalArgumentException if age range is invalid
   */
  private void validateAgeRange(int minAge, int maxAge) {
    if (minAge < 0) {
      throw new IllegalArgumentException("Minimum age cannot be negative");
    }
    if (maxAge < 0) {
      throw new IllegalArgumentException("Maximum age cannot be negative");
    }
    if (minAge > maxAge) {
      throw new IllegalArgumentException("Minimum age cannot be greater than maximum age");
    }
    if (maxAge > MAX_AGE_YEARS) {
      throw new IllegalArgumentException("Maximum age cannot exceed " + MAX_AGE_YEARS + " years");
    }
  }

  /**
   * Validate a single age parameter.
   *
   * @param age the age to validate
   * @throws IllegalArgumentException if age is invalid
   */
  private void validateAge(int age) {
    if (age < 0) {
      throw new IllegalArgumentException("Age cannot be negative");
    }
    if (age > MAX_AGE_YEARS) {
      throw new IllegalArgumentException("Age cannot exceed " + MAX_AGE_YEARS + " years");
    }
  }
}
