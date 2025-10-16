package com.ase.stammdatenverwaltung.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserFilterRequestDTO {
    private List<FilterCriterionDTO> filter;
    private String role;
}
