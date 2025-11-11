package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.entities.Example;
import com.ase.stammdatenverwaltung.repositories.ExampleRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service component for managing Example entities. Demonstrates business logic layer with
 * transaction management and error handling. Only available in development profile.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Profile("dev")
public class ExampleService {

  private final ExampleRepository exampleRepository;

  /**
   * Retrieve all examples.
   *
   * @return list of all examples
   */
  public List<Example> findAll() {
    log.debug("Finding all examples");
    return exampleRepository.findAll();
  }

  /**
   * Find example by ID.
   *
   * @param id the example ID
   * @return optional containing the example if found
   */
  public Optional<Example> findById(Long id) {
    log.debug("Finding example with ID: {}", id);
    return exampleRepository.findById(id);
  }

  /**
   * Search examples by name (case-insensitive).
   *
   * @param name the name to search for
   * @return list of examples with matching names
   */
  public List<Example> searchByName(String name) {
    log.debug("Searching examples by name: {}", name);
    return exampleRepository.findByNameContainingIgnoreCase(name);
  }

  /**
   * Search examples by description content.
   *
   * @param searchText the text to search for in descriptions
   * @return list of examples with matching descriptions
   */
  public List<Example> searchByDescription(String searchText) {
    log.debug("Searching examples by description: {}", searchText);
    return exampleRepository.findByDescriptionContaining(searchText);
  }

  /**
   * Create a new example.
   *
   * @param example the example to create
   * @return the created example with generated ID
   * @throws IllegalArgumentException if an example with the same name already exists
   */
  @Transactional
  public Example create(Example example) {
    log.debug("Creating new example: {}", example.getName());

    if (exampleRepository.existsByName(example.getName())) {
      throw new IllegalArgumentException(
          "Example with name '" + example.getName() + "' already exists");
    }

    try {
      Example savedExample = exampleRepository.save(example);
      log.info("Created example with ID: {}", savedExample.getId());
      return savedExample;
    } catch (DataIntegrityViolationException e) {
      log.error(
          "Data integrity violation while creating example ({})", e.getClass().getSimpleName());
      log.debug("Data integrity violation while creating example", e);
      throw new IllegalArgumentException("Invalid example data", e);
    }
  }

  /**
   * Update an existing example.
   *
   * @param id the ID of the example to update
   * @param updatedExample the updated example data
   * @return the updated example
   * @throws IllegalArgumentException if the example doesn't exist or name conflicts
   */
  @Transactional
  public Example update(Long id, Example updatedExample) {
    log.debug("Updating example with ID: {}", id);

    Example existingExample =
        exampleRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Example not found with ID: " + id));

    // Check for name conflicts (excluding the current example)
    if (!existingExample.getName().equals(updatedExample.getName())
        && exampleRepository.existsByName(updatedExample.getName())) {
      throw new IllegalArgumentException(
          "Example with name '" + updatedExample.getName() + "' already exists");
    }

    existingExample.setName(updatedExample.getName());
    existingExample.setDescription(updatedExample.getDescription());

    try {
      Example savedExample = exampleRepository.save(existingExample);
      log.info("Updated example with ID: {}", savedExample.getId());
      return savedExample;
    } catch (DataIntegrityViolationException e) {
      log.error(
          "Data integrity violation while updating example ({})", e.getClass().getSimpleName());
      log.debug("Data integrity violation while updating example", e);
      throw new IllegalArgumentException("Invalid example data", e);
    }
  }

  /**
   * Delete an example by ID.
   *
   * @param id the ID of the example to delete
   * @throws IllegalArgumentException if the example doesn't exist
   */
  @Transactional
  public void delete(Long id) {
    log.debug("Deleting example with ID: {}", id);

    if (!exampleRepository.existsById(id)) {
      throw new IllegalArgumentException("Example not found with ID: " + id);
    }

    exampleRepository.deleteById(id);
    log.info("Deleted example with ID: {}", id);
  }

  /**
   * Check if an example exists with the given name.
   *
   * @param name the name to check
   * @return true if an example with this name exists
   */
  public boolean existsByName(String name) {
    log.debug("Checking if example exists with name: {}", name);
    return exampleRepository.existsByName(name);
  }
}
