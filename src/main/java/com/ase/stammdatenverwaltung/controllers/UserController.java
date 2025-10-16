package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.UserFilterRequestDTO;
import com.ase.stammdatenverwaltung.dto.UserMasterDataResponseDTO;
import com.ase.stammdatenverwaltung.services.UserMasterDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Master Data", description = "API for user master data management")
public class UserController {

    private final UserMasterDataService userMasterDataService;

    @Operation(summary = "Get master data for multiple users",
            description = "Returns master data for multiple users, with optional filters.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserMasterDataResponseDTO.class)))
            })
    @GetMapping
    public ResponseEntity<List<UserMasterDataResponseDTO>> getUsers(
            @RequestBody(required = false) UserFilterRequestDTO filterRequest,
            @Parameter(description = "Flag to include name and email in the response", required = true)
            @RequestParam boolean also_get_name_and_email) {
        List<UserMasterDataResponseDTO> users = userMasterDataService.getAllUsers(filterRequest, also_get_name_and_email);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get master data for a single user",
            description = "Returns master data for a single user by their ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserMasterDataResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    @GetMapping("/{userId}")
    public ResponseEntity<UserMasterDataResponseDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Flag to include name and email in the response", required = true)
            @RequestParam boolean also_get_name_and_email) {
        UserMasterDataResponseDTO user = userMasterDataService.getUserById(userId, also_get_name_and_email);
        return ResponseEntity.ok(user);
    }
}
