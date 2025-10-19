package com.ase.stammdatenverwaltung.dto;

import lombok.Data;

@Data
public class FilterCriterionDTO {
  private String key;
  private String operator;
  private Object value;
}
