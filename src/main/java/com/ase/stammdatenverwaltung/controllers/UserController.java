package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.UserFilterRequestDTO;
import com.ase.stammdatenverwaltung.dto.UserMasterDataResponseDTO;
import com.ase.stammdatenverwaltung.services.UserMasterDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ase.stammdatenverwaltung.dto.CreateEmployeeRequest;
import com.ase.stammdatenverwaltung.dto.CreateLecturerRequest;
import com.ase.stammdatenverwaltung.dto.CreateStudentRequest;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.services.EmployeeService;
import com.ase.stammdatenverwaltung.services.LecturerService;
import com.ase.stammdatenverwaltung.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for handling user-related requests. */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Data", description = "API for user master data management")
public class UserController {

    private final UserMasterDataService userMasterDataService;
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
  public ResponseEntity<Student> createStudent(@Valid @RequestBody CreateStudentRequest request
      // TODO: Re-enable JWT authentication when ready
      // @AuthenticationPrincipal Jwt jwt
      ) {
    Student createdStudent = studentService.create(request);
    return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
  }

    @Operation(summary = "Get master data for multiple users",
            description = "Returns master data for multiple users, with optional filters.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserMasterDataResponseDTO.class)))
            })
    @GetMapping
    public ResponseEntity<List<UserMasterDataResponseDTO>> getUsers(
            @RequestBody(required = false) UserFilterRequestDTO filterRequest,
            @Parameter(description = "Flag to include name and email in the response", required = true)
            @RequestParam boolean also_get_name_and_email) {
        List<UserMasterDataResponseDTO> users = userMasterDataService.getAllUsers(filterRequest, also_get_name_and_email);
        return ResponseEntity.ok(users);
    }
  /**
   * Creates a new employee.
   *
   * @param request The request body containing the employee data.
   * @return The created employee.
   */
  @PostMapping("/employees")
  public ResponseEntity<Employee> createEmployee(@Valid @RequestBody CreateEmployeeRequest request
      // TODO: Re-enable JWT authentication when ready
      // @AuthenticationPrincipal Jwt jwt
      ) {
    Employee createdEmployee = employeeService.create(request);
    return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
  }

    @Operation(summary = "Get master data for a single user",
            description = "Returns master data for a single user by their ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserMasterDataResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    @GetMapping("/{userId}")
    public ResponseEntity<UserMasterDataResponseDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Flag to include name and email in the response", required = true)
            @RequestParam boolean also_get_name_and_email) {
        UserMasterDataResponseDTO user = userMasterDataService.getUserById(userId, also_get_name_and_email);
        return ResponseEntity.ok(user);
    }
  /**
   * Creates a new lecturer.
   *
   * @param request The request body containing the lecturer data.
   * @return The created lecturer.
   */
  @PostMapping("/lecturers")
  public ResponseEntity<Lecturer> createLecturer(@Valid @RequestBody CreateLecturerRequest request
      // TODO: Re-enable JWT authentication when ready
      // @AuthenticationPrincipal Jwt jwt
      ) {
    Lecturer createdLecturer = lecturerService.create(request);
    return new ResponseEntity<>(createdLecturer, HttpStatus.CREATED);
  }
}
