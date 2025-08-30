package com.ase.stammdatenverwaltung.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for example data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleDto {
  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Description is required")
  private String description;

  @NotNull(message = "Timestamp is required")
  private Long timestamp;

  private String version;
}
