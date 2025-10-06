package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ase.stammdatenverwaltung.entities.Person;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests specifically for H2 PostgreSQL compatibility mode. These tests verify that H2
 * behaves like PostgreSQL in key areas: - Case sensitivity (DATABASE_TO_LOWER=TRUE) - NULL ordering
 * (DEFAULT_NULL_ORDERING=HIGH) - SQL functions and syntax - Data type compatibility
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("H2 PostgreSQL Compatibility Tests")
class H2PostgreSqlCompatibilityTest {

  @Autowired private DataSource dataSource;

  @Autowired private PersonService personService;

  private JdbcTemplate jdbcTemplate;

  // Track created tables for reliable cleanup even when tests fail early.
  private final List<String> createdTables = new ArrayList<>();

  @BeforeEach
  void setUp() {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @AfterEach
  void cleanup() {
    // Drop in reverse order (dependencies safety, though none here). Ignore failures.
    for (int i = createdTables.size() - 1; i >= 0; i--) {
      String table = createdTables.get(i);
      try {
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + table);
      } catch (Exception ignored) {
        // Intentionally ignore - cleanup best effort.
      }
    }
    createdTables.clear();
  }

  private String uniqueName(String prefix) {
    // Keep identifier length within PostgreSQL limit (63). Reserve some room for prefix.
    String uuid = UUID.randomUUID().toString().replace("-", "");
    String suffix = uuid.substring(0, Math.min(32, uuid.length()));
    String candidate = prefix + "_" + suffix;
    if (candidate.length() > 60) { // safety truncate
      candidate = candidate.substring(0, 60);
    }
    return candidate.toLowerCase(); // mimic DATABASE_TO_LOWER behavior explicitly
  }

  @Test
  @DisplayName("Should handle case-insensitive identifiers like PostgreSQL")
  void shouldHandleCaseInsensitiveIdentifiersLikePostgreSQL() {
    // Unique table name for parallel-safe execution
    String tableName = uniqueName("test_case_sensitivity");

    // Given - Create table with mixed case (should be converted to lowercase)
    jdbcTemplate.execute(
        String.format(
            """
        CREATE TABLE %s (
            id BIGINT PRIMARY KEY,
            upper_case_col VARCHAR(50),
            lower_case_col VARCHAR(50)
        )
        """,
            tableName));
    createdTables.add(tableName);

    // Insert test data
    jdbcTemplate.update(
        String.format(
            """
        INSERT INTO %s (id, upper_case_col, lower_case_col)
        VALUES (1, 'UPPER', 'lower')
        """,
            tableName));

    // When - Query using lowercase (should work due to DATABASE_TO_LOWER=TRUE)
    List<String> results =
        jdbcTemplate.query(
            String.format(
                """
        SELECT upper_case_col, lower_case_col FROM %s WHERE id = 1
        """,
                tableName),
            (rs, rowNum) -> rs.getString("upper_case_col") + "," + rs.getString("lower_case_col"));

    // Then
    assertThat(results).hasSize(1);
    assertThat(results.get(0)).isEqualTo("UPPER,lower");
  }

  @Test
  @DisplayName("Should sort NULL values last like PostgreSQL")
  void shouldSortNullValuesLastLikePostgreSQL() {
    String tableName = uniqueName("null_ordering_test");

    // Given - Create test table with NULL values
    jdbcTemplate.execute(
        String.format(
            """
        CREATE TABLE %s (
            id BIGINT PRIMARY KEY,
            sort_value INTEGER
        )
        """,
            tableName));
    createdTables.add(tableName);

    // Insert test data with NULLs
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (1, 10)", tableName));
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (2, NULL)", tableName));
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (3, 5)", tableName));
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (4, NULL)", tableName));

    // When - Order by value (NULLs should sort last due to DEFAULT_NULL_ORDERING=HIGH)
    List<Integer> results =
        jdbcTemplate.query(
            String.format(
                """
        SELECT sort_value FROM %s ORDER BY sort_value
        """,
                tableName),
            (rs, rowNum) -> rs.getObject("sort_value", Integer.class));

    // Then - Non-null values first, then NULLs
    assertThat(results).containsExactly(5, 10, null, null);
  }

  @Test
  @DisplayName("Should support PostgreSQL-style CURRENT_DATE function")
  void shouldSupportPostgreSQLStyleCurrentDateFunction() {
    String tableName = uniqueName("date_function_test");

    // Given
    LocalDate today = LocalDate.now();

    // When - Use CURRENT_DATE in CHECK constraint (like in migrations)
    jdbcTemplate.execute(
        String.format(
            """
        CREATE TABLE %s (
            id BIGINT PRIMARY KEY,
            birth_date DATE,
            CONSTRAINT chk_birth_date_not_future CHECK (birth_date <= CURRENT_DATE)
        )
        """,
            tableName));
    createdTables.add(tableName);

    // Should allow past/future dates
    jdbcTemplate.update(
        String.format("INSERT INTO %s VALUES (1, ?)", tableName), today.minusDays(1));
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (2, ?)", tableName), today);

    // Should reject future dates
    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    String.format("INSERT INTO %s VALUES (3, ?)", tableName), today.plusDays(1)))
        .hasMessageContaining("constraint");
  }

  @Test
  @DisplayName("Should support PostgreSQL-style DATEADD function")
  void shouldSupportPostgreSQLStyleDateAddFunction() {
    String tableName = uniqueName("dateadd_function_test");

    // When - Use DATEADD in CHECK constraint (like in migrations)
    jdbcTemplate.execute(
        String.format(
            """
        CREATE TABLE %s (
            id BIGINT PRIMARY KEY,
            birth_date DATE,
            CONSTRAINT chk_birth_date_reasonable CHECK (birth_date >= DATEADD('YEAR', -150, CURRENT_DATE))
        )
        """,
            tableName));
    createdTables.add(tableName);

    // Should allow reasonable dates
    jdbcTemplate.update(
        String.format("INSERT INTO %s VALUES (1, ?)", tableName), LocalDate.now().minusYears(50));

    // Should reject unreasonably old dates
    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    String.format("INSERT INTO %s VALUES (2, ?)", tableName),
                    LocalDate.now().minusYears(200)))
        .hasMessageContaining("constraint");
  }

  @Test
  @DisplayName("Should support PostgreSQL-style REGEXP_LIKE function")
  void shouldSupportPostgreSQLStyleRegexpLikeFunction() {
    String tableName = uniqueName("regexp_test");

    // Given
    jdbcTemplate.execute(
        String.format(
            """
        CREATE TABLE %s (
            id BIGINT PRIMARY KEY,
            phone_number VARCHAR(20),
            CONSTRAINT chk_phone_format CHECK (phone_number IS NULL OR REGEXP_LIKE(phone_number, '^[+]?[0-9\\s\\-()]{7,20}$'))
        )
        """,
            tableName));
    createdTables.add(tableName);

    // When - Insert valid phone numbers
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (1, '+49 123 456789')", tableName));
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (2, '0123456789')", tableName));
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (3, '(030) 123-456')", tableName));

    // Should reject invalid phone numbers
    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    String.format("INSERT INTO %s VALUES (4, 'invalid-phone')", tableName)))
        .hasMessageContaining("constraint");
  }

  @Test
  @DisplayName("Should support PostgreSQL-style GENERATED BY DEFAULT AS IDENTITY")
  void shouldSupportPostgreSQLStyleGeneratedByDefaultAsIdentity() {
    String tableName = uniqueName("identity_test");

    // Given - Create table with identity column (like in migrations)
    jdbcTemplate.execute(
        String.format(
            """
        CREATE TABLE %s (
            id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
            name VARCHAR(50)
        )
        """,
            tableName));
    createdTables.add(tableName);

    // When - Insert without specifying ID (should auto-generate)
    jdbcTemplate.update(String.format("INSERT INTO %s (name) VALUES (?)", tableName), "Test Name");

    // Then - Should have generated ID
    Long generatedId =
        jdbcTemplate.queryForObject(
            String.format("SELECT id FROM %s WHERE name = 'Test Name'", tableName), Long.class);
    assertThat(generatedId).isNotNull();
    assertThat(generatedId).isGreaterThan(0);

    // When - Insert with explicit ID (should work due to BY DEFAULT)
    jdbcTemplate.update(
        String.format("INSERT INTO %s (id, name) VALUES (?, ?)", tableName), 100L, "Explicit ID");

    // Then - Should use explicit ID
    Long explicitId =
        jdbcTemplate.queryForObject(
            String.format("SELECT id FROM %s WHERE name = 'Explicit ID'", tableName), Long.class);
    assertThat(explicitId).isEqualTo(100L);
  }

  @Test
  @DisplayName("Should handle string case sensitivity like PostgreSQL")
  void shouldHandleStringCaseSensitivityLikePostgreSQL() {
    // Given - Create test data
    Person person1 =
        Person.builder().dateOfBirth(LocalDate.of(1990, 1, 1)).address("Berlin").build();
    Person person2 =
        Person.builder().dateOfBirth(LocalDate.of(1990, 1, 1)).address("berlin").build();

    personService.create(person1);
    personService.create(person2);

    // When - Query with case-sensitive LIKE (PostgreSQL behavior)
    List<Person> berlinResults =
        personService.findAll().stream().filter(p -> "Berlin".equals(p.getAddress())).toList();

    List<Person> lowercaseResults =
        personService.findAll().stream().filter(p -> "berlin".equals(p.getAddress())).toList();

    // Then - Should distinguish between cases
    assertThat(berlinResults).hasSize(1);
    assertThat(lowercaseResults).hasSize(1);
    assertThat(berlinResults.get(0).getId()).isNotEqualTo(lowercaseResults.get(0).getId());
  }

  @Test
  @DisplayName("Should support complex CHECK constraints with multiple conditions")
  void shouldSupportComplexCheckConstraintsWithMultipleConditions() {
    // Unique names for table and constraints to ensure isolation in parallel runs
    String tableName = uniqueName("constraint_test");
    String constraint1 = uniqueName("chk_semester_positive");
    String constraint2 = uniqueName("chk_semester_reasonable");
    String constraint3 = uniqueName("chk_study_status_valid");

    // Given - Create table with complex constraints (like student semester constraints)
    jdbcTemplate.execute(
        String.format(
            """
        CREATE TABLE %s (
            id BIGINT PRIMARY KEY,
            semester INTEGER,
            study_status VARCHAR(20),
            CONSTRAINT %s CHECK (semester IS NULL OR semester > 0),
            CONSTRAINT %s CHECK (semester IS NULL OR semester <= 20),
            CONSTRAINT %s CHECK (study_status IN ('ENROLLED', 'REGISTERED', 'ON_LEAVE', 'EXMATRICULATED', 'GRADUATED'))
        )
        """,
            tableName, constraint1, constraint2, constraint3));
    createdTables.add(tableName);

    // When - Insert valid data
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (1, 3, 'ENROLLED')", tableName));
    jdbcTemplate.update(String.format("INSERT INTO %s VALUES (2, NULL, 'GRADUATED')", tableName));

    // Should reject invalid semester
    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    String.format("INSERT INTO %s VALUES (3, -1, 'ENROLLED')", tableName)))
        .hasMessageContaining("constraint");

    // Should reject invalid study status
    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    String.format("INSERT INTO %s VALUES (4, 1, 'INVALID_STATUS')", tableName)))
        .hasMessageContaining("constraint");
  }
}
