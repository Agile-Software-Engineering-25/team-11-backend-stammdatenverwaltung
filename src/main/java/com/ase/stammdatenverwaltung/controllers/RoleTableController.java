package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.CreateRoleTable;
import com.ase.stammdatenverwaltung.services.RoleTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Tables", description = "Endpoint for creation of new role table")
public class RoleTableController {

  private final RoleTableService service;

  @PostMapping
  @Operation(summary = "Creates new role table (inherites from base table)")
  public ResponseEntity<Void> create(@Valid @RequestBody CreateRoleTable request) {
    log.debug("POST /api/v1/tables - {}", request.getRoleName());
    service.createRoleTable(request);
    return ResponseEntity.status(201).build();
  }
}

