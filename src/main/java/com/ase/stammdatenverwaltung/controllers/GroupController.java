package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.GroupDTO;
import com.ase.stammdatenverwaltung.dto.GroupResponseDTO;
import com.ase.stammdatenverwaltung.services.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
  @PreAuthorize(
      "hasRole('Area-3.Team-11.Read.User') or hasRole('HVS-Admin') or hasRole('Hochschulverwaltungsmitarbeiter')")
  public GroupResponseDTO getAllGroups(
      @io.swagger.v3.oas.annotations.Parameter(
              description = "Flag to include details from Keycloak",
              required = false)
          @org.springframework.web.bind.annotation.RequestParam(defaultValue = "true")
          boolean withDetails,
      @io.swagger.v3.oas.annotations.Parameter(
              description = "Include full member list in each group")
          @org.springframework.web.bind.annotation.RequestParam(defaultValue = "true")
          boolean show_members) {
    return groupService.getAllGroups(withDetails, show_members);
  }

  @GetMapping("/{groupName}")
  @Operation(
      summary = "Get a single student group by name",
      description = "Returns information for a specific student group.")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved group information")
  @ApiResponse(responseCode = "404", description = "Group not found")
  @PreAuthorize(
      "hasRole('Area-3.Team-11.Read.Student') or hasRole('HVS-Admin') or hasRole('Hochschulverwaltungsmitarbeiter')")
  public GroupDTO getGroupByName(
      @PathVariable String groupName,
      @io.swagger.v3.oas.annotations.Parameter(
              description = "Flag to include details from Keycloak",
              required = false)
          @org.springframework.web.bind.annotation.RequestParam(defaultValue = "true")
          boolean withDetails,
      @io.swagger.v3.oas.annotations.Parameter(
              description = "Include full member list of the group")
          @org.springframework.web.bind.annotation.RequestParam(defaultValue = "true")
          boolean show_members) {
    return groupService.getGroupByName(groupName, withDetails, show_members);
  }
}
