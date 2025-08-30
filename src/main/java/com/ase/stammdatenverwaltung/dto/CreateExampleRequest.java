package com.ase.stammdatenverwaltung.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for creating example data. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExampleRequest {
  @NotBlank(message = "Name is required and cannot be blank")
  @Pattern(
      regexp = "^[a-zA-Z0-9\\s_-]+$",
      message = "Name can only contain letters, numbers, spaces, " + "underscores, and hyphens")
  private String name;

  @NotBlank(message = "Description is required and cannot be blank")
  private String description;
}
