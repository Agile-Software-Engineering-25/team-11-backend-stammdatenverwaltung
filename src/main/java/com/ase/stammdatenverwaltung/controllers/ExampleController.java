package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.entities.Example;
import com.ase.stammdatenverwaltung.services.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Example entities. Demonstrates @RestController, validation, OpenAPI
 * documentation, and proper HTTP responses. Only available in development profile.
 */
@RestController
@RequestMapping("/api/v1/examples")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Examples", description = "API for managing example entities")
@Profile("dev")
public class ExampleController {

  private final ExampleService exampleService;

  /**
   * Get all examples.
   *
   * @return list of all examples
   */
  @GetMapping
  @Operation(
      summary = "Get all examples",
      description = "Retrieve a list of all examples in the system")
  @ApiResponse(
      responseCode = "200",
      description = "Successfully retrieved all examples",
      content = @Content(schema = @Schema(implementation = Example.class)))
  public ResponseEntity<List<Example>> getAllExamples() {
    log.debug("GET /api/v1/examples - Getting all examples");
    List<Example> examples = exampleService.findAll();
    return ResponseEntity.ok(examples);
  }

  /**
   * Get example by ID.
   *
   * @param id the example ID
   * @return the example if found
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get example by ID", description = "Retrieve a specific example by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Example found",
            content = @Content(schema = @Schema(implementation = Example.class))),
        @ApiResponse(responseCode = "404", description = "Example not found")
      })
  public ResponseEntity<Example> getExampleById(
      @Parameter(description = "ID of the example to retrieve", required = true) @PathVariable
          Long id) {
    log.debug("GET /api/v1/examples/{} - Getting example by ID", id);
    return exampleService
        .findById(id)
        .map(example -> ResponseEntity.ok(example))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Search examples by name.
   *
   * @param name the name to search for
   * @return list of examples matching the name
   */
  @GetMapping("/search")
  @Operation(
      summary = "Search examples by name",
      description = "Search for examples containing the specified name (case-insensitive)")
  @ApiResponse(
      responseCode = "200",
      description = "Search completed successfully",
      content = @Content(schema = @Schema(implementation = Example.class)))
  public ResponseEntity<List<Example>> searchExamples(
      @Parameter(description = "Name to search for", required = false)
          @RequestParam(required = false)
          String name,
      @Parameter(description = "Description text to search for", required = false)
          @RequestParam(required = false)
          String description) {
    log.debug(
        "GET /api/v1/examples/search - Searching examples with name: {} and description: {}",
        name,
        description);

    List<Example> examples;
    if (name != null && !name.trim().isEmpty()) {
      examples = exampleService.searchByName(name);
    } else if (description != null && !description.trim().isEmpty()) {
      examples = exampleService.searchByDescription(description);
    } else {
      examples = exampleService.findAll();
    }

    return ResponseEntity.ok(examples);
  }

  /**
   * Create a new example.
   *
   * @param example the example to create
   * @return the created example
   */
  @PostMapping
  @Operation(summary = "Create new example", description = "Create a new example entity")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Example created successfully",
            content = @Content(schema = @Schema(implementation = Example.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Example with this name already exists")
      })
  public ResponseEntity<Example> createExample(
      @Parameter(description = "Example data to create", required = true) @Valid @RequestBody
          Example example) {
    log.debug("POST /api/v1/examples - Creating new example: {}", example.getName());

    try {
      Example createdExample = exampleService.create(example);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdExample);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to create example: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Update an existing example.
   *
   * @param id the ID of the example to update
   * @param example the updated example data
   * @return the updated example
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update example", description = "Update an existing example by ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Example updated successfully",
            content = @Content(schema = @Schema(implementation = Example.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Example not found"),
        @ApiResponse(responseCode = "409", description = "Example with this name already exists")
      })
  public ResponseEntity<Example> updateExample(
      @Parameter(description = "ID of the example to update", required = true) @PathVariable
          Long id,
      @Parameter(description = "Updated example data", required = true) @Valid @RequestBody
          Example example) {
    log.debug("PUT /api/v1/examples/{} - Updating example: {}", id, example.getName());

    try {
      Example updatedExample = exampleService.update(id, example);
      return ResponseEntity.ok(updatedExample);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to update example with ID {}: {}", id, e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Delete an example by ID.
   *
   * @param id the ID of the example to delete
   * @return empty response
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete example", description = "Delete an example by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Example deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Example not found")
      })
  public ResponseEntity<Void> deleteExample(
      @Parameter(description = "ID of the example to delete", required = true) @PathVariable
          Long id) {
    log.debug("DELETE /api/v1/examples/{} - Deleting example", id);

    try {
      exampleService.delete(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      log.warn("Failed to delete example with ID {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Check if an example exists with the given name.
   *
   * @param name the name to check
   * @return boolean indicating if the example exists
   */
  @GetMapping("/exists")
  @Operation(
      summary = "Check if example exists",
      description = "Check if an example with the given name exists")
  @ApiResponse(
      responseCode = "200",
      description = "Check completed successfully",
      content = @Content(schema = @Schema(implementation = Boolean.class)))
  public ResponseEntity<Boolean> checkExampleExists(
      @Parameter(description = "Name to check for existence", required = true) @RequestParam
          String name) {
    log.debug("GET /api/v1/examples/exists?name={} - Checking if example exists", name);
    boolean exists = exampleService.existsByName(name);
    return ResponseEntity.ok(exists);
  }
}
