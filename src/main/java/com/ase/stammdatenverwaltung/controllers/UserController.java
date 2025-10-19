package com.ase.stammdatenverwaltung.controllers;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for handling user-related requests. */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

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
