package com.ase.stammdatenverwaltung.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Simple example entity for demonstration purposes. */
@Entity
@Table(name = "examples")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Example {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Name is required")
  @Column(nullable = false)
  private String name;

  @NotBlank(message = "Description is required")
  @Column(nullable = false, length = 500)
  private String description;
}
