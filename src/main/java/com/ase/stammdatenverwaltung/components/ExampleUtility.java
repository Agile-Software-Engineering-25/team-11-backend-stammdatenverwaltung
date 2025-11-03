package com.ase.stammdatenverwaltung.components;

import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import com.ase.stammdatenverwaltung.entities.Example;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Utility component for Example-related operations. Demonstrates how to create @Component classes
 * for shared functionality and utilities. Only available in development profile.
 */
@Component
@Slf4j
@Profile("dev")
public class ExampleUtility {

  private static final Pattern VALID_NAME_PATTERN =
      Pattern.compile(ValidationConstants.VALID_NAME_PATTERN);
  private static final Pattern FORBIDDEN_WORDS_PATTERN =
      Pattern.compile("(?i).*(test|debug|temp|delete).*");
  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  // Constants for scoring and validation
  private static final int MIN_DESCRIPTION_LENGTH = 10;
  private static final int DESCRIPTION_TRUNCATE_LENGTH = 50;
  private static final int QUALITY_SCORE_NAME_BASE = 20;
  private static final int QUALITY_SCORE_NAME_BONUS = 10;
  private static final int QUALITY_SCORE_DESCRIPTION_BASE = 40;
  private static final int QUALITY_SCORE_DESCRIPTION_BONUS = 15;
  private static final int MIN_NAME_LENGTH_FOR_BONUS = 5;
  private static final int MIN_DESCRIPTION_LENGTH_FOR_BONUS = 50;
  private static final int MIN_DESCRIPTION_LENGTH_FOR_EXTRA_BONUS = 100;
  private static final int TRUNCATE_ELLIPSIS_LENGTH = 3;

  /**
   * Validate if an example name follows the naming conventions.
   *
   * @param name the name to validate
   * @return true if the name is valid
   */
  public boolean isValidExampleName(String name) {
    if (name == null || name.trim().isEmpty()) {
      log.debug("Example name validation failed: name is null or empty");
      return false;
    }

    String trimmedName = name.trim();

    // Check length and character pattern
    if (!VALID_NAME_PATTERN.matcher(trimmedName).matches()) {
      log.debug("Example name validation failed: invalid characters or length in '{}'", name);
      return false;
    }

    // Check for forbidden words
    if (FORBIDDEN_WORDS_PATTERN.matcher(trimmedName).matches()) {
      log.debug("Example name validation failed: contains forbidden words in '{}'", name);
      return false;
    }

    log.debug("Example name validation passed for: '{}'", name);
    return true;
  }

  /**
   * Sanitize example name by removing extra whitespace and normalizing.
   *
   * @param name the name to sanitize
   * @return sanitized name
   */
  public String sanitizeExampleName(String name) {
    if (name == null) {
      return null;
    }

    // Remove extra whitespace and normalize
    String sanitized = name.trim().replaceAll("\\s+", " ");
    log.debug("Sanitized example name from '{}' to '{}'", name, sanitized);
    return sanitized;
  }

  /**
   * Generate a summary of examples list.
   *
   * @param examples the list of examples
   * @return summary string
   */
  public String generateExamplesSummary(List<Example> examples) {
    if (examples == null || examples.isEmpty()) {
      return "No examples found";
    }

    StringBuilder summary = new StringBuilder();
    summary.append("Examples Summary (").append(examples.size()).append(" total):\n");

    examples.forEach(
        example -> {
          summary
              .append("- ID: ")
              .append(example.getId())
              .append(", Name: '")
              .append(example.getName())
              .append("', Description: '")
              .append(truncateText(example.getDescription(), DESCRIPTION_TRUNCATE_LENGTH))
              .append("'\n");
        });

    String result = summary.toString();
    log.debug("Generated summary for {} examples", examples.size());
    return result;
  }

  /**
   * Create a formatted timestamp string.
   *
   * @return formatted timestamp
   */
  public String getCurrentTimestamp() {
    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    log.debug("Generated timestamp: {}", timestamp);
    return timestamp;
  }

  /**
   * Check if an example has a meaningful description.
   *
   * @param example the example to check
   * @return true if the description is meaningful
   */
  public boolean hasMeaningfulDescription(Example example) {
    if (example == null || example.getDescription() == null) {
      return false;
    }

    String description = example.getDescription().trim();

    // Check minimum length
    if (description.length() < MIN_DESCRIPTION_LENGTH) {
      log.debug("Example {} has too short description", example.getId());
      return false;
    }

    // Check for placeholder text
    String lowerDescription = description.toLowerCase();
    if (lowerDescription.contains("todo")
        || lowerDescription.contains("placeholder")
        || lowerDescription.contains("example")
        || lowerDescription.equals("description")) {
      log.debug("Example {} has placeholder description", example.getId());
      return false;
    }

    log.debug("Example {} has meaningful description", example.getId());
    return true;
  }

  /**
   * Calculate a simple quality score for an example.
   *
   * @param example the example to score
   * @return quality score (0-100)
   */
  public int calculateQualityScore(Example example) {
    if (example == null) {
      return 0;
    }

    int score = 0;

    // Name quality (30 points max)
    if (isValidExampleName(example.getName())) {
      score += QUALITY_SCORE_NAME_BASE;
      if (example.getName().length() > MIN_NAME_LENGTH_FOR_BONUS) {
        score += QUALITY_SCORE_NAME_BONUS;
      }
    }

    // Description quality (70 points max)
    if (hasMeaningfulDescription(example)) {
      score += QUALITY_SCORE_DESCRIPTION_BASE;
      if (example.getDescription().length() > MIN_DESCRIPTION_LENGTH_FOR_BONUS) {
        score += QUALITY_SCORE_DESCRIPTION_BONUS;
      }
      if (example.getDescription().length() > MIN_DESCRIPTION_LENGTH_FOR_EXTRA_BONUS) {
        score += QUALITY_SCORE_DESCRIPTION_BONUS;
      }
    }

    log.debug("Calculated quality score {} for example '{}'", score, example.getName());
    return score;
  }

  /**
   * Truncate text to specified length with ellipsis.
   *
   * @param text the text to truncate
   * @param maxLength maximum length
   * @return truncated text
   */
  private String truncateText(String text, int maxLength) {
    if (text == null || text.length() <= maxLength) {
      return text;
    }
    return text.substring(0, maxLength - TRUNCATE_ELLIPSIS_LENGTH) + "...";
  }
}
