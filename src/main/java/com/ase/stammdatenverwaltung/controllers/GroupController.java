package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.GroupDTO;
import com.ase.stammdatenverwaltung.dto.GroupResponseDTO;
import com.ase.stammdatenverwaltung.services.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for handling student group-related requests. */
@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
@Tag(name = "Group", description = "Endpoints for retrieving student group information")
public class GroupController {

  private final GroupService groupService;

  @GetMapping
  @Operation(
      summary = "Get all student groups",
      description = "Returns a list of all active student groups and their student counts.")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved all groups")
  public GroupResponseDTO getAllGroups(
      @io.swagger.v3.oas.annotations.Parameter(
              description = "Flag to include details from Keycloak",
              required = false)
          @org.springframework.web.bind.annotation.RequestParam(defaultValue = "true")
          boolean withDetails) {
    return groupService.getAllGroups(withDetails);
  }

  @GetMapping("/{groupName}")
  @Operation(
      summary = "Get a single student group by name",
      description = "Returns information for a specific student group.")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved group information")
  @ApiResponse(responseCode = "404", description = "Group not found")
  public GroupDTO getGroupByName(
      @PathVariable String groupName,
      @io.swagger.v3.oas.annotations.Parameter(
              description = "Flag to include details from Keycloak",
              required = false)
          @org.springframework.web.bind.annotation.RequestParam(defaultValue = "true")
          boolean withDetails) {
    return groupService.getGroupByName(groupName, withDetails);
  }
}
