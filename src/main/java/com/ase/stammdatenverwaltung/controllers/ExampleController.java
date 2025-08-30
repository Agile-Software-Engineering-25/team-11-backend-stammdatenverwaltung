package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.CreateExampleRequest;
import com.ase.stammdatenverwaltung.dto.ExampleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example REST controller demonstrating simple endpoints.
 * This controller showcases basic Spring Boot features with separate DTOs.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/example")
@Tag(name = "Example", description = "Example API for demonstration purposes")
public class ExampleController {

  /**
   * Returns a simple greeting message.
   *
   * @return ResponseEntity containing a greeting message
   */
  @GetMapping
  @Operation(summary = "Get greeting message",
             description = "Returns a simple greeting message")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved greeting")
  public ResponseEntity<String> getExample() {
    log.info("Greeting endpoint called");
    return ResponseEntity.ok("Hello from Spring Boot! This is a simple example.");
  }

  /**
   * Returns example data in JSON format.
   *
   * @return ResponseEntity containing example data
   */
  @GetMapping("/data")
  @Operation(summary = "Get example data",
             description = "Returns structured example data in JSON format")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved data")
  public ResponseEntity<ExampleDto> getExampleData() {
    log.info("Data endpoint called");
    ExampleDto data = ExampleDto.builder()
        .name("example")
        .description("This is example data")
        .timestamp(System.currentTimeMillis())
        .version("1.0")
        .build();
    return ResponseEntity.ok(data);
  }

  /**
   * Creates new example data.
   *
   * @param request the data to create
   * @return ResponseEntity containing created data
   */
  @PostMapping
  @Operation(summary = "Create example data",
             description = "Creates new example data with validation")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully created data"),
      @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  public ResponseEntity<ExampleDto> createExampleData(
      @Valid @RequestBody CreateExampleRequest request) {

    log.info("Creating example data: {}", request);

    ExampleDto data = ExampleDto.builder()
        .name(request.getName())
        .description(request.getDescription())
        .timestamp(System.currentTimeMillis())
        .version("1.0")
        .build();

    return ResponseEntity.ok(data);
  }
}
