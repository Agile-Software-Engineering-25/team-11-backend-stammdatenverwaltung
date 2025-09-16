package com.ase.stammdatenverwaltung.repositories;

import com.ase.stammdatenverwaltung.entities.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Employee entities. Provides basic CRUD operations and custom queries
 * specific to employee management. Extends JpaRepository to inherit standard database operations.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  /**
   * Find an employee by their employee number.
   *
   * @param employeeNumber the employee number to search for
   * @return optional containing the employee if found
   */
  Optional<Employee> findByEmployeeNumber(String employeeNumber);

  /**
   * Check if an employee exists with the given employee number.
   *
   * @param employeeNumber the employee number to check
   * @return true if an employee exists with the employee number
   */
  boolean existsByEmployeeNumber(String employeeNumber);

  /**
   * Find employees by department.
   *
   * @param department the department name
   * @return list of employees in the given department
   */
  List<Employee> findByDepartmentContainingIgnoreCase(String department);

  /**
   * Find employees by working time model.
   *
   * @param workingTimeModel the working time model
   * @return list of employees with the given working time model
   */
  List<Employee> findByWorkingTimeModel(Employee.WorkingTimeModel workingTimeModel);

  /**
   * Find employees by office number.
   *
   * @param officeNumber the office number
   * @return list of employees in the given office
   */
  List<Employee> findByOfficeNumber(String officeNumber);

  /**
   * Find employees by department and working time model.
   *
   * @param department the department name
   * @param workingTimeModel the working time model
   * @return list of employees matching both criteria
   */
  List<Employee> findByDepartmentContainingIgnoreCaseAndWorkingTimeModel(
      String department, Employee.WorkingTimeModel workingTimeModel);

  /**
   * Find all full-time employees.
   *
   * @return list of employees with FULL_TIME working time model
   */
  @Query("SELECT e FROM Employee e WHERE e.workingTimeModel = 'FULL_TIME'")
  List<Employee> findAllFullTimeEmployees();

  /**
   * Find all part-time and contract employees.
   *
   * @return list of employees with PART_TIME, CONTRACT, or MINI_JOB working time models
   */
  @Query(
      "SELECT e FROM Employee e WHERE e.workingTimeModel IN ('PART_TIME', 'CONTRACT', 'MINI_JOB')")
  List<Employee> findAllFlexibleEmployees();

  /**
   * Count employees by department.
   *
   * @param department the department to count
   * @return number of employees in the department
   */
  long countByDepartmentContainingIgnoreCase(String department);

  /**
   * Count employees by working time model.
   *
   * @param workingTimeModel the working time model to count
   * @return number of employees with the given working time model
   */
  long countByWorkingTimeModel(Employee.WorkingTimeModel workingTimeModel);

  /**
   * Find employees without an employee number (external contractors, etc.).
   *
   * @return list of employees without employee numbers
   */
  @Query("SELECT e FROM Employee e WHERE e.employeeNumber IS NULL")
  List<Employee> findEmployeesWithoutEmployeeNumber();

  /**
   * Find departments with employee count.
   *
   * @return list of arrays containing department name and count
   */
  @Query(
      "SELECT e.department, COUNT(e) FROM Employee e WHERE e.department IS NOT NULL "
          + "GROUP BY e.department ORDER BY COUNT(e) DESC")
  List<Object[]> findDepartmentEmployeeCounts();

  /**
   * Find employees in a specific office building (assuming office numbers start with building
   * identifier).
   *
   * @param buildingPrefix the building prefix (e.g., "A" for building A)
   * @return list of employees in offices starting with the building prefix
   */
  @Query("SELECT e FROM Employee e WHERE e.officeNumber LIKE :buildingPrefix%")
  List<Employee> findEmployeesInBuilding(@Param("buildingPrefix") String buildingPrefix);
}
