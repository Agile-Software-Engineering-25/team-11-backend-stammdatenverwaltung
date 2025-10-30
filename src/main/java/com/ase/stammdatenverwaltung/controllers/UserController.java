package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.CreateEmployeeRequest;
import com.ase.stammdatenverwaltung.dto.CreateLecturerRequest;
import com.ase.stammdatenverwaltung.dto.CreateStudentRequest;
import com.ase.stammdatenverwaltung.dto.PersonDetailsDTO;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.services.EmployeeService;
import com.ase.stammdatenverwaltung.services.LecturerService;
import com.ase.stammdatenverwaltung.services.PersonService;
import com.ase.stammdatenverwaltung.services.StudentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller for handling user-related requests. Provides endpoints for creating and retrieving
 *
 * <p>different types of users (students, employees, lecturers).
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Data", description = "API for user data management")
public class UserController {

  private final PersonService personService;

  private final StudentService studentService;

  private final EmployeeService employeeService;

  private final LecturerService lecturerService;

  /**
   * Creates a new student.
   *
   * @param request The request body containing the student data.
   * @return The created student.
   */
  @Operation(
      summary = "Creates a new student.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Successfully created student",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Student.class))
            })
      })
  @PostMapping("/students")
  public ResponseEntity<Student> createStudent(@Valid @RequestBody CreateStudentRequest request) {

    Student createdStudent = studentService.create(request);

    return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
  }

  @Operation(
      summary = "Get master data for multiple users",
      description = "Returns master data for multiple users, with optional filters.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved user data",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PersonDetailsDTO.class))
            })
      })
  @GetMapping
  public ResponseEntity<List<PersonDetailsDTO>> getUsers(
      @Parameter(description = "Flag to include details from Keycloak", required = false)
          @RequestParam(defaultValue = "true")
          boolean withDetails,
      @Parameter(
              description = "Filter by user type (student, lecturer, employee)",
              required = false)
          @RequestParam(required = false)
          String userType) {
    Flux<PersonDetailsDTO> users = personService.findAll(withDetails, userType);
    return ResponseEntity.ok(users.collectList().block());
  }

  /**
   * Creates a new employee.
   *
   * @param request The request body containing the employee data.
   * @return The created employee.
   */
  @Operation(
      summary = "Creates a new employee.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Successfully created employee",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Employee.class))
            })
      })
  @PostMapping("/employees")
  public ResponseEntity<Employee> createEmployee(
      @Valid @RequestBody CreateEmployeeRequest request) {

    Employee createdEmployee = employeeService.create(request);

    return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
  }

  @Operation(
      summary = "Get master data for a single user",
      description = "Returns master data for a single user by their ID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved user data",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PersonDetailsDTO.class))
            }),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @GetMapping("/{userId}")
  public ResponseEntity<PersonDetailsDTO> getUserById(
      @Parameter(description = "ID of the user to retrieve", required = true) @PathVariable
          String userId,
      @Parameter(description = "Flag to include details from Keycloak", required = false)
          @RequestParam(defaultValue = "true")
          boolean withDetails) {

    Mono<PersonDetailsDTO> user = personService.findById(userId, withDetails);

    return ResponseEntity.ok(user.block());
  }

  /**
   * Creates a new lecturer.
   *
   * @param request The request body containing the lecturer data.
   * @return The created lecturer.
   */
  @Operation(
      summary = "Creates a new lecturer.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Successfully created lecturer",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Lecturer.class))
            })
      })
  @PostMapping("/lecturers")
  public ResponseEntity<Lecturer> createLecturer(
      @Valid @RequestBody CreateLecturerRequest request) {

    Lecturer createdLecturer = lecturerService.create(request);

    return new ResponseEntity<>(createdLecturer, HttpStatus.CREATED);
  }

  /**
   * Deletes a user.
   *
   * @param id the ID of the user to delete
   * @return empty response
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete example", description = "Delete an example by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Example deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Example not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  public ResponseEntity<Void> deleteUserById(
      @Parameter(description = "ID of the example to delete", required = true) @PathVariable
          String id) {
    log.debug("DELETE /api/v1/User/{} - Deleting User", id);
    try {
      personService.deleteById(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      log.warn("Failed to delete example with ID {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
