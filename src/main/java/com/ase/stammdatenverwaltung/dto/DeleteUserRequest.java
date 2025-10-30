package com.ase.stammdatenverwaltung.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data transfer object for deleting a user. */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeleteUserRequest {

  @NotBlank(message = "user-id is required")
  @JsonProperty("user-id")
  private String userId;
}
