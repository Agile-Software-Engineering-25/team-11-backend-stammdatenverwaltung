package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.CreateEmployeeRequest;
import com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest;
import com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.model.KeycloakGroup;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service class for managing Employee entities. Provides business logic for employee-specific CRUD
 * operations with proper validation and transaction management.
 */
@Service
@Transactional
@Validated
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final KeycloakClient keycloakClient;

  /**
   * Creates a new employee from a request DTO. First checks if the user already exists in Keycloak,
   * then creates the user in Keycloak with the "university-administrative-staff" group, and finally
   * stores the employee data locally using the Keycloak user ID.
   *
   * @param request The request body containing the employee data.
   * @return The created employee.
   * @throws com.ase.stammdatenverwaltung.exceptions.KeycloakUserAlreadyExistsException if the user
   *     already exists in Keycloak
   */
  public Employee create(CreateEmployeeRequest request) {
    log.debug("Creating new employee with employee number: {}", request.getEmployeeNumber());

    // Check if user already exists in Keycloak (prevents 409 conflicts)
    if (keycloakClient.userExists(request.getUsername())) {
      throw new com.ase.stammdatenverwaltung.exceptions.KeycloakUserAlreadyExistsException(
          request.getUsername());
    }

    // Create user in Keycloak with university-administrative-staff group
    CreateUserRequest keycloakRequest =
        CreateUserRequest.builder()
            .username(request.getUsername())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .groups(java.util.List.of(KeycloakGroup.UNIVERSITY_ADMINISTRATIVE_STAFF.getGroupName()))
            .build();

    CreateUserResponse keycloakResponse = keycloakClient.createUser(keycloakRequest).block();

    if (keycloakResponse == null || keycloakResponse.getId() == null) {
      throw new IllegalStateException(
          "Failed to create user in Keycloak for username: " + request.getUsername());
    }

    // Create employee entity with Keycloak user ID
    Employee employee =
        Employee.builder()
            .id(keycloakResponse.getId())
            .dateOfBirth(request.getDateOfBirth())
            .address(request.getAddress())
            .phoneNumber(request.getPhoneNumber())
            .photoUrl(request.getPhotoUrl())
            .employeeNumber(request.getEmployeeNumber())
            .department(request.getDepartment())
            .officeNumber(request.getOfficeNumber())
            .workingTimeModel(request.getWorkingTimeModel())
            .build();

    validateEmployeeForCreation(employee);
    Employee savedEmployee = employeeRepository.save(employee);
    log.info(
        "Successfully created employee with ID: {} and employee number: {}",
        savedEmployee.getId(),
        savedEmployee.getEmployeeNumber());
    return savedEmployee;
  }

  /**
   * Find an employee by their ID.
   *
   * @param id the employee ID
   * @return optional containing the employee if found
   */
  @Transactional(readOnly = true)
  public Optional<Employee> findById(String id) {
    log.debug("Finding employee with ID: {}", id);
    return employeeRepository.findById(id);
  }

  /**
   * Get an employee by their ID, throwing an exception if not found.
   *
   * @param id the employee ID
   * @return the employee entity
   * @throws EntityNotFoundException if the employee is not found
   */
  @Transactional(readOnly = true)
  public Employee getById(String id) {
    log.debug("Getting employee with ID: {}", id);
    return employeeRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));
  }

  /**
   * Find an employee by their employee number.
   *
   * @param employeeNumber the employee number
   * @return optional containing the employee if found
   */
  @Transactional(readOnly = true)
  public Optional<Employee> findByEmployeeNumber(String employeeNumber) {
    log.debug("Finding employee by employee number: {}", employeeNumber);
    return employeeRepository.findByEmployeeNumber(employeeNumber);
  }

  /**
   * Find all employees in the system.
   *
   * @return list of all employees
   */
  @Transactional(readOnly = true)
  public List<Employee> findAll() {
    log.debug("Finding all employees");
    return employeeRepository.findAll();
  }

  /**
   * Update an existing employee.
   *
   * @param id the employee ID to update
   * @param updatedEmployee the updated employee data
   * @return the updated employee entity
   * @throws EntityNotFoundException if the employee is not found
   */
  public Employee update(String id, @Valid Employee updatedEmployee) {
    log.debug("Updating employee with ID: {}", id);
    Employee existingEmployee = getById(id);

    existingEmployee.setEmployeeNumber(updatedEmployee.getEmployeeNumber());
    existingEmployee.setDepartment(updatedEmployee.getDepartment());
    existingEmployee.setOfficeNumber(updatedEmployee.getOfficeNumber());
    existingEmployee.setWorkingTimeModel(updatedEmployee.getWorkingTimeModel());

    existingEmployee.setDateOfBirth(updatedEmployee.getDateOfBirth());
    existingEmployee.setAddress(updatedEmployee.getAddress());
    existingEmployee.setPhoneNumber(updatedEmployee.getPhoneNumber());
    existingEmployee.setPhotoUrl(updatedEmployee.getPhotoUrl());

    Employee savedEmployee = employeeRepository.save(existingEmployee);
    log.info("Successfully updated employee with ID: {}", savedEmployee.getId());
    return savedEmployee;
  }

  /**
   * Delete an employee by their ID.
   *
   * @param id the employee ID to delete
   * @throws EntityNotFoundException if the employee is not found
   */
  public void deleteById(String id) {
    log.debug("Deleting employee with ID: {}", id);
    if (!employeeRepository.existsById(id)) {
      throw new EntityNotFoundException("Employee not found with ID: " + id);
    }
    employeeRepository.deleteById(id);
    log.info("Successfully deleted employee with ID: {}", id);
  }

  /**
   * Find employees by department.
   *
   * @param department the department name
   * @return list of employees in the given department
   */
  @Transactional(readOnly = true)
  public List<Employee> findByDepartment(String department) {
    log.debug("Finding employees by department: {}", department);
    return employeeRepository.findByDepartmentContainingIgnoreCase(department);
  }

  /**
   * Find employees by working time model.
   *
   * @param workingTimeModel the working time model
   * @return list of employees with the given working time model
   */
  @Transactional(readOnly = true)
  public List<Employee> findByWorkingTimeModel(Employee.WorkingTimeModel workingTimeModel) {
    log.debug("Finding employees by working time model: {}", workingTimeModel);
    return employeeRepository.findByWorkingTimeModel(workingTimeModel);
  }

  /**
   * Find all full-time employees.
   *
   * @return list of full-time employees
   */
  @Transactional(readOnly = true)
  public List<Employee> findAllFullTimeEmployees() {
    log.debug("Finding all full-time employees");
    return employeeRepository.findAllFullTimeEmployees();
  }

  /**
   * Find employees by office number.
   *
   * @param officeNumber the office number
   * @return list of employees in the given office
   */
  @Transactional(readOnly = true)
  public List<Employee> findByOfficeNumber(String officeNumber) {
    log.debug("Finding employees by office number: {}", officeNumber);
    return employeeRepository.findByOfficeNumber(officeNumber);
  }

  /**
   * Update an employee's working time model.
   *
   * @param id the employee ID
   * @param newWorkingTimeModel the new working time model
   * @return the updated employee
   * @throws EntityNotFoundException if the employee is not found
   */
  public Employee updateWorkingTimeModel(String id, Employee.WorkingTimeModel newWorkingTimeModel) {
    log.debug("Updating working time model for employee ID: {} to {}", id, newWorkingTimeModel);
    Employee employee = getById(id);
    employee.setWorkingTimeModel(newWorkingTimeModel);
    Employee savedEmployee = employeeRepository.save(employee);
    log.info(
        "Successfully updated working time model for employee ID: {} to {}",
        id,
        newWorkingTimeModel);
    return savedEmployee;
  }

  /**
   * Update an employee's department.
   *
   * @param id the employee ID
   * @param newDepartment the new department
   * @return the updated employee
   * @throws EntityNotFoundException if the employee is not found
   */
  public Employee updateDepartment(String id, String newDepartment) {
    log.debug("Updating department for employee ID: {} to {}", id, newDepartment);
    Employee employee = getById(id);
    employee.setDepartment(newDepartment);
    Employee savedEmployee = employeeRepository.save(employee);
    log.info("Successfully updated department for employee ID: {} to {}", id, newDepartment);
    return savedEmployee;
  }

  /**
   * Count employees by department.
   *
   * @param department the department to count
   * @return number of employees in the department
   */
  @Transactional(readOnly = true)
  public long countByDepartment(String department) {
    return employeeRepository.countByDepartmentContainingIgnoreCase(department);
  }

  /**
   * Count employees by working time model.
   *
   * @param workingTimeModel the working time model to count
   * @return number of employees with the given working time model
   */
  @Transactional(readOnly = true)
  public long countByWorkingTimeModel(Employee.WorkingTimeModel workingTimeModel) {
    return employeeRepository.countByWorkingTimeModel(workingTimeModel);
  }

  private void validateEmployeeForCreation(Employee employee) {
    if (employee.getEmployeeNumber() != null
        && employeeRepository.existsByEmployeeNumber(employee.getEmployeeNumber())) {
      throw new IllegalArgumentException(
          "Employee with employee number " + employee.getEmployeeNumber() + " already exists");
    }
  }
}
