package com.ase.stammdatenverwaltung.dto;

import java.util.List;
import lombok.Data;

@Data
public class UserFilterRequestDTO {
  private List<FilterCriterionDTO> filter;
  private String role;
}
