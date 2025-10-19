package com.ase.stammdatenverwaltung.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.ase.stammdatenverwaltung.config.JwtSecurityProperties;
import com.ase.stammdatenverwaltung.dto.CreateEmployeeRequest;
import com.ase.stammdatenverwaltung.dto.CreateLecturerRequest;
import com.ase.stammdatenverwaltung.dto.CreateStudentRequest;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.services.EmployeeService;
import com.ase.stammdatenverwaltung.services.LecturerService;
import com.ase.stammdatenverwaltung.services.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = UserController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private StudentService studentService;

  @MockBean private EmployeeService employeeService;

  @MockBean private LecturerService lecturerService;

  @MockBean private JwtSecurityProperties jwtSecurityProperties;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createStudent() throws Exception {
    CreateStudentRequest request = new CreateStudentRequest();
    request.setDateOfBirth(LocalDate.of(2000, 1, 1));
    request.setMatriculationNumber("12345");
    request.setStudyStatus(Student.StudyStatus.ENROLLED);

    Student student = new Student();
    student.setId("test-id");
    student.setMatriculationNumber("12345");

    when(studentService.create(any(CreateStudentRequest.class), any(String.class))).thenReturn(student);

    Jwt mockJwt = Jwt.withTokenValue("mock-token")
            .header("alg", "RS256")
            .claim("sub", "test-id")
            .build();

    mockMvc
        .perform(
            post("/api/v1/users/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().jwt(mockJwt)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", Matchers.is("test-id")))
        .andExpect(jsonPath("$.matriculationNumber", Matchers.is("12345")));
  }

  @Test
  void createEmployee() throws Exception {
    CreateEmployeeRequest request = new CreateEmployeeRequest();
    request.setDateOfBirth(LocalDate.of(1980, 1, 1));
    request.setEmployeeNumber("E12345");

    Employee employee = new Employee();
    employee.setId("test-id");
    employee.setEmployeeNumber("E12345");

    when(employeeService.create(any(CreateEmployeeRequest.class))).thenReturn(employee);

    mockMvc
        .perform(
            post("/api/v1/users/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", Matchers.is("test-id")))
        .andExpect(jsonPath("$.employeeNumber", Matchers.is("E12345")));
  }

  @Test
  void createLecturer() throws Exception {
    CreateLecturerRequest request = new CreateLecturerRequest();
    request.setDateOfBirth(LocalDate.of(1970, 1, 1));
    request.setEmployeeNumber("L12345");
    request.setFieldChair("Computer Science");

    Lecturer lecturer = new Lecturer();
    lecturer.setId("test-id");
    lecturer.setEmployeeNumber("L12345");
    lecturer.setFieldChair("Computer Science");

    when(lecturerService.create(any(CreateLecturerRequest.class))).thenReturn(lecturer);

    mockMvc
        .perform(
            post("/api/v1/users/lecturers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", Matchers.is("test-id")))
        .andExpect(jsonPath("$.employeeNumber", Matchers.is("L12345")))
        .andExpect(jsonPath("$.fieldChair", Matchers.is("Computer Science")));
  }
}
