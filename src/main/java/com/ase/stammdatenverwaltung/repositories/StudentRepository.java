package com.ase.stammdatenverwaltung.repositories;

import com.ase.stammdatenverwaltung.entities.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Student entities. Provides basic CRUD operations and custom queries
 * specific to student management. Extends JpaRepository to inherit standard database operations.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

  /**
   * Find a student by their unique matriculation number.
   *
   * @param matriculationNumber the matriculation number to search for
   * @return optional containing the student if found
   */
  Optional<Student> findByMatriculationNumber(String matriculationNumber);

  /**
   * Check if a student exists with the given matriculation number.
   *
   * @param matriculationNumber the matriculation number to check
   * @return true if a student exists with the matriculation number
   */
  boolean existsByMatriculationNumber(String matriculationNumber);

  /**
   * Find students by their cohort.
   *
   * @param cohort the cohort identifier
   * @return list of students in the given cohort
   */
  List<Student> findByCohort(String cohort);

  /**
   * Find students by their study status.
   *
   * @param studyStatus the study status to filter by
   * @return list of students with the given status
   */
  List<Student> findByStudyStatus(Student.StudyStatus studyStatus);

  /**
   * Find students by degree program.
   *
   * @param degreeProgram the degree program name
   * @return list of students in the given degree program
   */
  List<Student> findByDegreeProgramContainingIgnoreCase(String degreeProgram);

  /**
   * Find students by semester.
   *
   * @param semester the semester number
   * @return list of students in the given semester
   */
  List<Student> findBySemester(Integer semester);

  /**
   * Find students by semester range.
   *
   * @param minSemester minimum semester (inclusive)
   * @param maxSemester maximum semester (inclusive)
   * @return list of students within the semester range
   */
  List<Student> findBySemesterBetween(Integer minSemester, Integer maxSemester);

  /**
   * Find all currently enrolled students.
   *
   * @return list of students with ENROLLED status
   */
  @Query("SELECT s FROM Student s WHERE s.studyStatus = 'ENROLLED'")
  List<Student> findAllEnrolledStudents();

  /**
   * Find students by cohort and study status.
   *
   * @param cohort the cohort identifier
   * @param studyStatus the study status
   * @return list of students matching both criteria
   */
  List<Student> findByCohortAndStudyStatus(String cohort, Student.StudyStatus studyStatus);

  /**
   * Count students by cohort.
   *
   * @param cohort the cohort to count
   * @return number of students in the cohort
   */
  long countByCohort(String cohort);

  /**
   * Count students by study status.
   *
   * @param studyStatus the study status to count
   * @return number of students with the given status
   */
  long countByStudyStatus(Student.StudyStatus studyStatus);

  /**
   * Find students with highest semester number.
   *
   * @return list of students in the highest semester
   */
  @Query("SELECT s FROM Student s WHERE s.semester = (SELECT MAX(s2.semester) FROM Student s2)")
  List<Student> findStudentsInHighestSemester();

  /**
   * Find students ready for graduation (high semester and enrolled status).
   *
   * @param minSemester minimum semester to be considered for graduation
   * @return list of students potentially ready for graduation
   */
  @Query("SELECT s FROM Student s WHERE s.semester >= :minSemester AND s.studyStatus = 'ENROLLED'")
  List<Student> findStudentsReadyForGraduation(@Param("minSemester") Integer minSemester);

  /**
   * Counts students per cohort.
   *
   * @return A list of object arrays, where each array contains the cohort name and the student
   *     count.
   */
  @Query("SELECT s.cohort, COUNT(s) FROM Student s GROUP BY s.cohort ORDER BY s.cohort")
  List<Object[]> countStudentsByCohort();
}
