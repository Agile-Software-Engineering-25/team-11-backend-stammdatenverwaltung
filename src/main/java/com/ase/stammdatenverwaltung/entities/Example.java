package com.ase.stammdatenverwaltung.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple example entity demonstrating basic JPA entity structure. This serves as a template for
 * creating other entities in the application.
 */
@Entity
@Table(name = "examples")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Example {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "Name cannot be blank")
  @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @NotBlank(message = "Description cannot be blank")
  @Size(min = 1, max = 500, message = "Description must be between 1 and 500 characters")
  @Column(name = "description", nullable = false, length = 500)
  private String description;
}
