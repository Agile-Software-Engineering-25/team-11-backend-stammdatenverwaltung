package com.ase.stammdatenverwaltung.constants;

/**
 * Centralized validation constants used across the application.
 *
 * <p>WHY: Defining constants in a single location eliminates duplication, ensures consistency, and
 * makes maintenance easier. Any update to validation rules is reflected immediately across all
 * modules.
 */
public final class ValidationConstants {

  private ValidationConstants() {
    // Prevent instantiation
  }

  // ===== Age Validation =====
  /** Maximum allowed age for validation purposes (in years). */
  public static final int MAX_AGE_YEARS = 150;

  // ===== Name Length Limits =====
  /** Maximum length for first names. */
  public static final int MAX_FIRST_NAME_LENGTH = 100;

  /** Maximum length for last names. */
  public static final int MAX_LAST_NAME_LENGTH = 100;

  /** Maximum length for example names and similar string fields. */
  public static final int MAX_NAME_LENGTH = 255;

  /** Minimum length for example names (must have at least 1 character). */
  public static final int MIN_NAME_LENGTH = 1;

  // ===== Address & Contact Limits =====
  /** Maximum length for address fields. */
  public static final int MAX_ADDRESS_LENGTH = 500;

  /** Maximum length for phone numbers (in formatted string). */
  public static final int MAX_PHONE_NUMBER_LENGTH = 20;

  /** Minimum length for phone numbers (in formatted string). */
  public static final int MIN_PHONE_NUMBER_LENGTH = 7;

  /** Maximum length for photo URL fields. */
  public static final int MAX_PHOTO_URL_LENGTH = 1000;

  // ===== Academic Fields =====
  /** Maximum length for matriculation numbers. */
  public static final int MAX_MATRICULATION_NUMBER_LENGTH = 20;

  /** Maximum length for degree program names. */
  public static final int MAX_DEGREE_PROGRAM_LENGTH = 200;

  /** Maximum length for cohort identifiers. */
  public static final int MAX_COHORT_LENGTH = 50;

  // ===== Employee Fields =====
  /** Maximum length for employee numbers. */
  public static final int MAX_EMPLOYEE_NUMBER_LENGTH = 20;

  /** Maximum length for department names. */
  public static final int MAX_DEPARTMENT_LENGTH = 200;

  /** Maximum length for office numbers/identifiers. */
  public static final int MAX_OFFICE_NUMBER_LENGTH = 50;

  /** Maximum length for academic titles and field/chair names. */
  public static final int MAX_FIELD_CHAIR_LENGTH = 300;

  /** Maximum length for job titles. */
  public static final int MAX_TITLE_LENGTH = 50;

  // ===== Description Limits =====
  /** Minimum length for example descriptions. */
  public static final int MIN_DESCRIPTION_LENGTH = 1;

  /** Maximum length for example descriptions. */
  public static final int MAX_DESCRIPTION_LENGTH = 500;

  // ===== Regular Expression Patterns =====
  /**
   * Phone number validation pattern. Allows: +, digits, spaces, hyphens, parentheses.
   *
   * <p>NOTE: The embedded length constraint (7-20) must match MIN_PHONE_NUMBER_LENGTH and
   * MAX_PHONE_NUMBER_LENGTH. Length validation is also enforced via {@code @Size} annotations on
   * entity fields, providing defense-in-depth. WHY: International phone numbers may have various
   * formats with these characters.
   */
  public static final String PHONE_NUMBER_PATTERN = "^[+]?[0-9\\s\\-()]{7,20}$";

  /**
   * Cohort identifier pattern. Disallows whitespace to ensure no spaces in cohort identifiers.
   *
   * <p>WHY: Cohorts are identifiers that should not contain spaces for consistency and database
   * queries.
   */
  public static final String COHORT_NO_WHITESPACE_PATTERN = "\\S+";

  /**
   * Valid name pattern for examples. Allows: alphanumerics, spaces, hyphens, underscores.
   *
   * <p>NOTE: The embedded length constraint (1-255) must match MIN_NAME_LENGTH and MAX_NAME_LENGTH.
   * Length validation is also enforced via {@code @Size} annotations on entity fields, providing
   * defense-in-depth. WHY: Names should be readable and may contain common separators like hyphens
   * or underscores.
   */
  public static final String VALID_NAME_PATTERN = "^[a-zA-Z0-9\\s\\-_]{1,255}$";
}
