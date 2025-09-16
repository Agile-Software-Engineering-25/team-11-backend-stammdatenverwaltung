package com.ase.stammdatenverwaltung.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Simple DTO for example data (used for both requests and responses). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleDto {
  private Long id; // For responses

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Description is required")
  private String description;
}
