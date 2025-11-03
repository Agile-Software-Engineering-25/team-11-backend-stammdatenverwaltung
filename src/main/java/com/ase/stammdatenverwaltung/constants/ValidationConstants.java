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

  /** Maximum allowed age for validation purposes (in years). */
  public static final int MAX_AGE_YEARS = 150;
}
