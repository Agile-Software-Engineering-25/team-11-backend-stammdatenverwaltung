package com.ase.stammdatenverwaltung.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the response containing multiple groups.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponseDTO {
  @JsonProperty("group_count")
  private int groupCount;

  @JsonProperty("groups")
  private List<GroupDTO> groups;
}
