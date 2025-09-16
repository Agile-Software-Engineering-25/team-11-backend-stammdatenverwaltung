package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.dto.ExampleDto;
import com.ase.stammdatenverwaltung.entities.Example;
import com.ase.stammdatenverwaltung.repositories.ExampleRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Simple service for managing Example entities. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleService {

  private final ExampleRepository exampleRepository;

  /**
   * Get all examples.
   *
   * @return List of all examples
   */
  public List<ExampleDto> getAllExamples() {
    log.info("Fetching all examples");
    return exampleRepository.findAll().stream().map(this::convertToDto).toList();
  }

  /**
   * Get example by ID.
   *
   * @param id the example ID
   * @return Optional containing the example if found
   */
  public Optional<ExampleDto> getExampleById(Long id) {
    log.info("Fetching example with id: {}", id);
    return exampleRepository.findById(id).map(this::convertToDto);
  }

  /**
   * Create a new example.
   *
   * @param exampleDto the example data
   * @return created example
   */
  public ExampleDto createExample(ExampleDto exampleDto) {
    log.info("Creating new example: {}", exampleDto.getName());
    Example example =
        Example.builder()
            .name(exampleDto.getName())
            .description(exampleDto.getDescription())
            .build();
    Example saved = exampleRepository.save(example);
    return convertToDto(saved);
  }

  private ExampleDto convertToDto(Example example) {
    return ExampleDto.builder()
        .id(example.getId())
        .name(example.getName())
        .description(example.getDescription())
        .build();
  }
}
