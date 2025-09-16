package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.ExampleDto;
import com.ase.stammdatenverwaltung.services.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Simple REST controller for managing examples with a service layer and entity persistence. */
@Slf4j
@RestController
@RequestMapping("/api/v1/examples")
@RequiredArgsConstructor
@Tag(name = "Examples", description = "Simple API for managing examples")
public class ExampleController {

  private final ExampleService exampleService;

  /**
   * Get all examples.
   *
   * @return ResponseEntity containing list of all examples
   */
  @GetMapping
  @Operation(summary = "Get all examples", description = "Returns all examples from the database")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved examples")
  public ResponseEntity<List<ExampleDto>> getAllExamples() {
    log.info("Getting all examples");
    List<ExampleDto> examples = exampleService.getAllExamples();
    return ResponseEntity.ok(examples);
  }

  /**
   * Get example by ID.
   *
   * @param id the example ID
   * @return ResponseEntity containing the example if found
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get example by ID", description = "Returns a specific example by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved example"),
        @ApiResponse(responseCode = "404", description = "Example not found")
      })
  public ResponseEntity<ExampleDto> getExampleById(@PathVariable Long id) {
    log.info("Getting example with id: {}", id);
    return exampleService
        .getExampleById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Create a new example.
   *
   * @param exampleDto the example data to create
   * @return ResponseEntity containing the created example
   */
  @PostMapping
  @Operation(summary = "Create example", description = "Creates a new example in the database")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully created example"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  public ResponseEntity<ExampleDto> createExample(@Valid @RequestBody ExampleDto exampleDto) {
    log.info("Creating example: {}", exampleDto.getName());
    ExampleDto created = exampleService.createExample(exampleDto);
    return ResponseEntity.ok(created);
  }
}
