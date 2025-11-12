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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
   * Creates a new student. Requires write access to student master data.
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
  @PreAuthorize(
      "hasRole('Area-3.Team-11.Write.Student') or hasRole('sau-admin') or hasRole('university-administrative-staff')")
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
      log.error(
          "Failed to create student: external service error - {} ({})",
          e.getMessage(),
          e.getClass().getSimpleName());
      log.debug("Failed to create student: external service error", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      log.error("Failed to create student: {} ({})", e.getMessage(), e.getClass().getSimpleName());
      log.debug("Failed to create student", e);
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
  @PreAuthorize("hasRole('Area-3.Team-11.Read.User') or hasRole('sau-admin') or hasRole('university-administrative-staff')")
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
          "Error retrieving users with withDetails={} and userType={}: {} ({})",
          withDetails,
          userType,
          e.getMessage(),
          e.getClass().getSimpleName());
      log.debug("Error retrieving users", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Creates a new employee. Requires write access to employee master data.
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
  @PreAuthorize(
      "hasRole('Area-3.Team-11.Write.Employee') or hasRole('sau-admin') or hasRole('university-administrative-staff')")
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
      log.error(
          "Failed to create employee: external service error - {} ({})",
          e.getMessage(),
          e.getClass().getSimpleName());
      log.debug("Failed to create employee: external service error", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      log.error("Failed to create employee: {} ({})", e.getMessage(), e.getClass().getSimpleName());
      log.debug("Failed to create employee", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Operation(
      summary = "Get master data for a single user",
      description =
          "Returns master data for a single user by their ID. Permission is enforced based on user type.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved user data",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PersonDetailsDTO.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions for user type"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @GetMapping("/{userId}")
  @PreAuthorize(
      "@personService.canAccessUser(#userId, 'Read') or hasRole('sau-admin') or hasRole('university-administrative-staff')")
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
      log.error(
          "Error retrieving user with ID {}: {} ({})",
          userId,
          e.getMessage(),
          e.getClass().getSimpleName());
      log.debug("Error retrieving user with ID {}", userId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Updates an existing user with partial data. Only provided fields are updated; others retain
   * their existing values. Supports updating all user types (students, employees, lecturers).
   * Requires write access to the corresponding user type master data.
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
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions for user type"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{userId}")
  @PreAuthorize(
      "@personService.canAccessUser(#userId, 'Write') or hasRole('sau-admin') or hasRole('university-administrative-staff')")
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
      log.error(
          "Unexpected error while updating user with ID {}: {} ({})",
          userId,
          e.getMessage(),
          e.getClass().getSimpleName());
      log.debug("Unexpected error while updating user with ID {}", userId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Creates a new lecturer. Requires write access to lecturer master data.
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
  @PreAuthorize(
      "hasRole('Area-3.Team-11.Write.Lecturer') or hasRole('sau-admin') or hasRole('university-administrative-staff')")
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
      log.error(
          "Failed to create lecturer: external service error - {} ({})",
          e.getMessage(),
          e.getClass().getSimpleName());
      log.debug("Failed to create lecturer: external service error", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      log.error("Failed to create lecturer: {} ({})", e.getMessage(), e.getClass().getSimpleName());
      log.debug("Failed to create lecturer", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Deletes a user. Requires delete access to the corresponding user type master data.
   *
   * @param userId The ID of the user to delete.
   * @return an empty response
   */
  @DeleteMapping("/{userId}")
  @Operation(summary = "Delete a user", description = "Delete a user by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions for user type"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PreAuthorize(
      "@personService.canAccessUser(#userId, 'Delete') or hasRole('sau-admin') or hasRole('university-administrative-staff')")
  public ResponseEntity<Void> deleteUserById(
      @Parameter(description = "ID of the user to delete", required = true) @PathVariable
          String userId) {
    log.debug("DELETE /api/v1/users/{} - Deleting user with ID {}", userId, userId);
    try {
      personService.deleteById(userId);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      log.warn("Failed to delete user with ID {}: {}", userId, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Deletes a user (legacy endpoint). Requires delete access to the corresponding user type master
   * data. This endpoint is maintained for backward compatibility with legacy systems.
   *
   * @param request the request body containing the user-id
   * @return an empty response
   */
  @PostMapping("/delete")
  @Operation(
      summary = "Delete a user (legacy)",
      description =
          "Delete a user by ID. This is a legacy endpoint; use DELETE /{userId} for new implementations.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid request body"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions for user type"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PreAuthorize(
      "@personService.canAccessUser(#request.userId, 'Delete') or hasRole('sau-admin') or hasRole('university-administrative-staff')")
  public ResponseEntity<Void> deleteUserByIdLegacy(@Valid @RequestBody DeleteUserRequest request) {
    String id = request.getUserId();
    log.debug("POST /api/v1/users/delete - Deleting user with ID {} (legacy endpoint)", id);
    try {
      personService.deleteById(id);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      log.warn("Failed to delete user with ID {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
