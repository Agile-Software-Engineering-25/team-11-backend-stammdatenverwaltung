package com.ase.stammdatenverwaltung.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Data Transfer Object for student group information. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {
  private String name;

  @JsonProperty("students_count")
  private int studentCount;

  @JsonProperty("students")
  private List<StudentDTO> students;
}
