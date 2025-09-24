package com.ase.stammdatenverwaltung.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class CreateRoleTable {

  /** Name der neuen Rolle/Tabelle (lower_snake_case) */
  @NotBlank
  @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
  private String roleName;

  /** optionale Basistabelle, deren Spalten übernommen werden */
  @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
  private String inheritsFrom;

  /** zusätzliche neue Spalten */
  @NotNull
  private List<ColumnDef> columns;

  // --- getters/setters ---
  public String getRoleName() { return roleName; }
  public void setRoleName(String roleName) { this.roleName = roleName; }
  public String getInheritsFrom() { return inheritsFrom; }
  public void setInheritsFrom(String inheritsFrom) { this.inheritsFrom = inheritsFrom; }
  public List<ColumnDef> getColumns() { return columns; }
  public void setColumns(List<ColumnDef> columns) { this.columns = columns; }

  public static class ColumnDef {
    @NotBlank
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "nur lower_snake_case erlaubt")
    private String name;

    /** STRING, INT, BIGINT, BOOL, DATE, TIMESTAMP, DECIMAL */
    @NotNull
    private DataType type;


    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DataType getType() { return type; }
    public void setType(DataType type) { this.type = type; }
  }

  public enum DataType { STRING, INT, BIGINT, BOOL, DATE, TIMESTAMP, DECIMAL, LONG, FLOAT, CHAR }
}

