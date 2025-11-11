package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.CreateEmployeeRequest;
import com.ase.stammdatenverwaltung.dto.CreateLecturerRequest;
import com.ase.stammdatenverwaltung.dto.CreateStudentRequest;
import com.ase.stammdatenverwaltung.dto.DeleteUserRequest;
import com.ase.stammdatenverwaltung.dto.PersonDetailsDTO;
import com.ase.stammdatenverwaltung.dto.UpdateUserRequest;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
            }),
        @ApiResponse(responseCode = "400", description = "Invalid request data or conflict"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping("/students")
  public ResponseEntity<Student> createStudent(@Valid @RequestBody CreateStudentRequest request) {
    log.debug(
        "POST /api/v1/users/students - Creating student with username: {}", request.getUsername());
    try {
      Student createdStudent = studentService.create(request);
      return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to create student: invalid request data - {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (IllegalStateException e) {
      log.error("Failed to create student: external service error - {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      log.error("Failed to create student: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
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
    try {
      Flux<PersonDetailsDTO> users = personService.findAll(withDetails, userType);
      List<PersonDetailsDTO> userList = users.collectList().block();
      return ResponseEntity.ok(userList);
    } catch (Exception e) {
      log.error(
          "Error retrieving users with withDetails={} and userType={}: {}",
          withDetails,
          userType,
          e.getMessage(),
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
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
            }),
        @ApiResponse(responseCode = "400", description = "Invalid request data or conflict"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping("/employees")
  public ResponseEntity<Employee> createEmployee(
      @Valid @RequestBody CreateEmployeeRequest request) {
    log.debug(
        "POST /api/v1/users/employees - Creating employee with username: {}",
        request.getUsername());
    try {
      Employee createdEmployee = employeeService.create(request);
      return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to create employee: invalid request data - {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (IllegalStateException e) {
      log.error("Failed to create employee: external service error - {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      log.error("Failed to create employee: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
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

    try {
      Mono<PersonDetailsDTO> user = personService.findById(userId, withDetails);
      PersonDetailsDTO userDetails = user.block();
      return ResponseEntity.ok(userDetails);
    } catch (EntityNotFoundException e) {
      log.debug("User not found with ID: {}", userId);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error retrieving user with ID {}: {}", userId, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Updates an existing user with partial data. Only provided fields are updated; others retain
   * their existing values. Supports updating all user types (students, employees, lecturers).
   *
   * @param userId The ID of the user to update.
   * @param updateRequest The request containing fields to update.
   * @return The updated user data.
   */
  @Operation(
      summary = "Update an existing user",
      description =
          "Partially updates a user. Only provided fields are updated; omitted fields retain their"
              + " existing values.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated user",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PersonDetailsDTO.class))
            }),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{userId}")
  public ResponseEntity<PersonDetailsDTO> updateUser(
      @Parameter(description = "ID of the user to update", required = true) @PathVariable
          String userId,
      @Valid @RequestBody UpdateUserRequest updateRequest) {

    log.debug("PUT /api/v1/users/{} - Updating user with data: {}", userId, updateRequest);
    try {
      personService.updatePartial(userId, updateRequest);
      log.debug("User partial update completed for ID: {}", userId);

      PersonDetailsDTO result = personService.findById(userId, true).block();
      log.info("Successfully updated user with ID: {}", userId);
      return ResponseEntity.ok(result);

    } catch (EntityNotFoundException e) {
      log.warn("Failed to update user with ID {}: user not found - {}", userId, e.getMessage());
      return ResponseEntity.notFound().build();

    } catch (IllegalArgumentException e) {
      log.warn("Failed to update user with ID {}: invalid data - {}", userId, e.getMessage());
      return ResponseEntity.badRequest().build();

    } catch (Exception e) {
      log.error("Unexpected error while updating user with ID {}: {}", userId, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
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
            }),
        @ApiResponse(responseCode = "400", description = "Invalid request data or conflict"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping("/lecturers")
  public ResponseEntity<Lecturer> createLecturer(
      @Valid @RequestBody CreateLecturerRequest request) {
    log.debug(
        "POST /api/v1/users/lecturers - Creating lecturer with username: {}",
        request.getUsername());
    try {
      Lecturer createdLecturer = lecturerService.create(request);
      return new ResponseEntity<>(createdLecturer, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to create lecturer: invalid request data - {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (IllegalStateException e) {
      log.error("Failed to create lecturer: external service error - {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      log.error("Failed to create lecturer: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Deletes a user
   *
   * @param request the request body containing the user-id
   * @return an empty response
   */
  @PostMapping("/delete")
  @Operation(summary = "Delete a user", description = "Delete a user by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid request body"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  public ResponseEntity<Void> deleteUserById(@Valid @RequestBody DeleteUserRequest request) {
    String id = request.getUserId();
    log.debug("POST /api/v1/users/delete - Deleting user with ID {}", id);
    try {
      personService.deleteById(id);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      log.warn("Failed to delete user with ID {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
