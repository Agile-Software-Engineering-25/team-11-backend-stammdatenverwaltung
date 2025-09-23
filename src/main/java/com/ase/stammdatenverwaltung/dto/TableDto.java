package com.ase.stammdatenverwaltung.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import java.util.List;

/** Data transfer object for example data. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableDto {
  @NotBlank(message = "Name is required")
  private String rolle;

  private String erbtVonRolle;

  @NotNull
  private List<Eigenschaften> eigenschaften;

  public String getRolle(){return this.rolle;}
  public void setRolle(String rolle) {this.rolle = rolle;}

  public String getErbtVonRolle(){return this.erbtVonRolle;}
  public void setErbtVonRolle(String erbtVonRolle) {this.erbtVonRolle = erbtVonRolle;}

  public List<Eigenschaften> getEigenschaften() {return eigenschaften;}
  public void setEigenschaften(List<Eigenschaften> eigenschaften) {this.eigenschaften = eigenschaften;}

  public static class Eigenschaften {

    @NotBlank
    private String name;

    @NotNull
    private JacksonProperties.Datatype datentyp;

    public String getName() {return this.name;}
    public void setName(String name) {this.name = name;}

    public Datatype getDatentyp() {return this.datentyp;}
    public void setDatentyp(Datatype datentyp) {this.datentyp = datentyp;}
  }

  public enum DataType {
    STRING,     // VARCHAR(length)  -> length defaulten (z.B. 255), wenn null
    INT,        // INTEGER
    BIGINT,     // BIGINT
    BOOL,       // BOOLEAN
    DATE,       // DATE
    TIMESTAMP,  // TIMESTAMP (ohne TZ) – bei Bedarf später WITH TIME ZONE
    DECIMAL     // NUMERIC(precision,scale)
  }
}
