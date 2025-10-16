package com.ase.stammdatenverwaltung.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponseDTO {
    @JsonProperty("group_count")
    private int groupCount;

    @JsonProperty("groups")
    private List<GroupDTO> groups;
}
