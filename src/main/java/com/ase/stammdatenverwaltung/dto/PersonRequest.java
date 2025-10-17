package com.ase.stammdatenverwaltung.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public abstract class PersonRequest {

  @NotNull(message = "Date of birth is required")
  @Past(message = "Date of birth must be in the past")
  private LocalDate dateOfBirth;

  @Size(max = 500, message = "Address cannot exceed 500 characters")
  private String address;

  @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Phone number must be a valid format")
  private String phoneNumber;

  @Size(max = 1000, message = "Photo URL cannot exceed 1000 characters")
  private String photoUrl;
}
