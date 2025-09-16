package com.ase.stammdatenverwaltung.repositories;

import com.ase.stammdatenverwaltung.entities.Person;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Person entities. Provides basic CRUD operations and custom queries for
 * the base Person class. This repository can be used to query across all person types due to the
 * inheritance hierarchy.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

  /**
   * Find persons by phone number.
   *
   * @param phoneNumber the phone number to search for
   * @return list of persons with the given phone number
   */
  List<Person> findByPhoneNumber(String phoneNumber);

  /**
   * Find persons by address containing the given text (case-insensitive).
   *
   * @param address the address text to search for
   * @return list of persons with addresses containing the text
   */
  List<Person> findByAddressContainingIgnoreCase(String address);

  /**
   * Find persons born between two dates (inclusive).
   *
   * @param startDate the start date
   * @param endDate the end date
   * @return list of persons born between the dates
   */
  List<Person> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);

  /**
   * Find persons by exact date of birth.
   *
   * @param dateOfBirth the date of birth
   * @return list of persons born on the given date
   */
  List<Person> findByDateOfBirth(LocalDate dateOfBirth);

  /**
   * Check if a person exists with the given phone number.
   *
   * @param phoneNumber the phone number to check
   * @return true if a person exists with the phone number
   */
  boolean existsByPhoneNumber(String phoneNumber);

  /**
   * Find the oldest person in the system.
   *
   * @return optional containing the oldest person, or empty if no persons exist
   */
  @Query("SELECT p FROM Person p WHERE p.dateOfBirth = (SELECT MIN(p2.dateOfBirth) FROM Person p2)")
  Optional<Person> findOldestPerson();

  /**
   * Find the youngest person in the system.
   *
   * @return optional containing the youngest person, or empty if no persons exist
   */
  @Query("SELECT p FROM Person p WHERE p.dateOfBirth = (SELECT MAX(p2.dateOfBirth) FROM Person p2)")
  Optional<Person> findYoungestPerson();

  // Note: Age-based queries removed due to complexity with date arithmetic in JPQL
  // Can be implemented later with native queries if needed
}
