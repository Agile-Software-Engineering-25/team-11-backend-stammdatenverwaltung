package com.ase.stammdatenverwaltung.repositories;

import com.ase.stammdatenverwaltung.entities.Lecturer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Lecturer entities. Provides basic CRUD operations and custom queries
 * specific to lecturer management. Extends JpaRepository to inherit standard database operations.
 */
@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {

  /**
   * Find lecturers by their field or chair.
   *
   * @param fieldChair the field or chair to search for
   * @return list of lecturers in the given field/chair
   */
  List<Lecturer> findByFieldChairContainingIgnoreCase(String fieldChair);

  /**
   * Find lecturers by their title.
   *
   * @param title the academic title
   * @return list of lecturers with the given title
   */
  List<Lecturer> findByTitleContainingIgnoreCase(String title);

  /**
   * Find lecturers by employment status.
   *
   * @param employmentStatus the employment status
   * @return list of lecturers with the given employment status
   */
  List<Lecturer> findByEmploymentStatus(Lecturer.EmploymentStatus employmentStatus);

  /**
   * Find lecturers by department (inherited from Employee).
   *
   * @param department the department name
   * @return list of lecturers in the given department
   */
  List<Lecturer> findByDepartmentContainingIgnoreCase(String department);

  /**
   * Find lecturers by field/chair and employment status.
   *
   * @param fieldChair the field or chair
   * @param employmentStatus the employment status
   * @return list of lecturers matching both criteria
   */
  List<Lecturer> findByFieldChairContainingIgnoreCaseAndEmploymentStatus(
      String fieldChair, Lecturer.EmploymentStatus employmentStatus);

  /**
   * Find all permanent lecturers (full-time and part-time permanent).
   *
   * @return list of lecturers with permanent employment status
   */
  @Query(
      "SELECT l FROM Lecturer l WHERE l.employmentStatus IN ('FULL_TIME_PERMANENT', 'PART_TIME_PERMANENT')")
  List<Lecturer> findAllPermanentLecturers();

  /**
   * Find all external lecturers.
   *
   * @return list of lecturers with EXTERNAL employment status
   */
  @Query("SELECT l FROM Lecturer l WHERE l.employmentStatus = 'EXTERNAL'")
  List<Lecturer> findAllExternalLecturers();

  /**
   * Find all professors (lecturers with professor titles).
   *
   * @return list of lecturers with professor-related titles
   */
  @Query("SELECT l FROM Lecturer l WHERE l.title LIKE '%Prof%'")
  List<Lecturer> findAllProfessors();

  /**
   * Find all lecturers with doctoral titles.
   *
   * @return list of lecturers with Dr. titles
   */
  @Query("SELECT l FROM Lecturer l WHERE l.title LIKE '%Dr%'")
  List<Lecturer> findAllDoctors();

  /**
   * Count lecturers by employment status.
   *
   * @param employmentStatus the employment status to count
   * @return number of lecturers with the given employment status
   */
  long countByEmploymentStatus(Lecturer.EmploymentStatus employmentStatus);

  /**
   * Count lecturers by field/chair.
   *
   * @param fieldChair the field or chair to count
   * @return number of lecturers in the given field/chair
   */
  long countByFieldChairContainingIgnoreCase(String fieldChair);

  /**
   * Find fields/chairs with lecturer count.
   *
   * @return list of arrays containing field/chair name and count
   */
  @Query(
      "SELECT l.fieldChair, COUNT(l) FROM Lecturer l WHERE l.fieldChair IS NOT NULL "
          + "GROUP BY l.fieldChair ORDER BY COUNT(l) DESC")
  List<Object[]> findFieldChairLecturerCounts();

  /**
   * Find lecturers by working time model (inherited from Employee) and employment status.
   *
   * @param workingTimeModel the working time model
   * @param employmentStatus the employment status
   * @return list of lecturers matching both criteria
   */
  @Query(
      "SELECT l FROM Lecturer l WHERE l.workingTimeModel = :workingTimeModel "
          + "AND l.employmentStatus = :employmentStatus")
  List<Lecturer> findByWorkingTimeModelAndEmploymentStatus(
      @Param("workingTimeModel") Lecturer.WorkingTimeModel workingTimeModel,
      @Param("employmentStatus") Lecturer.EmploymentStatus employmentStatus);

  /**
   * Find lecturers who are also full-time employees.
   *
   * @return list of lecturers with FULL_TIME working time model
   */
  @Query("SELECT l FROM Lecturer l WHERE l.workingTimeModel = 'FULL_TIME'")
  List<Lecturer> findFullTimeLecturers();
}
