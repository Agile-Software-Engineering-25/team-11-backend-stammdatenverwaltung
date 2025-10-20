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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling user-related requests. Provides endpoints for creating and retrieving
 *
 * <p>different types of users (students, employees, lecturers).
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
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
        @Parameter(description = "Filter by user type (student, lecturer, employee)",
            required = false)
            @RequestParam(required = false)
            String userType,
        @Parameter(description = "Filter by cohort", required = false)
            @RequestParam(required = false)
            String cohort) {
      List<PersonDetailsDTO> users = personService.findAll(withDetails, userType);
      return ResponseEntity.ok(users);
    }

  /**
   * Creates a new employee.
   *
   * @param request The request body containing the employee data.
   * @return The created employee.
   */
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

    PersonDetailsDTO user = personService.findById(userId, withDetails);

    return ResponseEntity.ok(user);
  }

  /**
   * Creates a new lecturer.
   *
   * @param request The request body containing the lecturer data.
   * @return The created lecturer.
   */
  @PostMapping("/lecturers")
  public ResponseEntity<Lecturer> createLecturer(
      @Valid @RequestBody CreateLecturerRequest request) {

    Lecturer createdLecturer = lecturerService.create(request);

    return new ResponseEntity<>(createdLecturer, HttpStatus.CREATED);
  }
}
