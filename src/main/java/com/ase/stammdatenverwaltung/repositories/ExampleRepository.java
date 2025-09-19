package com.ase.stammdatenverwaltung.repositories;

import com.ase.stammdatenverwaltung.entities.Example;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Example entities. Demonstrates basic Spring Data JPA repository patterns
 * with custom queries. Only available in development profile.
 */
@Repository
@Profile("dev")
public interface ExampleRepository extends JpaRepository<Example, Long> {

  /**
   * Find examples by name (case-insensitive).
   *
   * @param name the name to search for
   * @return list of examples with matching names
   */
  List<Example> findByNameContainingIgnoreCase(String name);

  /**
   * Find example by exact name match.
   *
   * @param name the exact name to search for
   * @return optional containing the example if found
   */
  Optional<Example> findByName(String name);

  /**
   * Custom query to find examples with description containing specific text.
   *
   * @param searchText the text to search for in descriptions
   * @return list of examples with matching descriptions
   */
  @Query(
      "SELECT e FROM Example e WHERE LOWER(e.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
  List<Example> findByDescriptionContaining(@Param("searchText") String searchText);

  /**
   * Check if an example with the given name exists.
   *
   * @param name the name to check
   * @return true if an example with this name exists
   */
  boolean existsByName(String name);
}
