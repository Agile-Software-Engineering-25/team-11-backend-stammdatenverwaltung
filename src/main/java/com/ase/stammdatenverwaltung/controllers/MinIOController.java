package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.services.MinIOService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** REST controller for managing MinIO Uploads, primarily profile pictures of users */
@RestController
@RequestMapping("/api/v1/profile-picture")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MinIO Pictures", description = "API for profile picture management")
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
public class MinIOController {

  private final MinIOService minIOService;

  /**
   * Get profile picture by user ID.
   *
   * @param id the user ID
   * @return the picture if found
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get profile picture by user ID",
      description = "Get profile picture by user ID from MinIO")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Picture found"),
        @ApiResponse(responseCode = "404", description = "Picture not found")
      })
  public ResponseEntity<byte[]> getProfilePicture(
      @Parameter(description = "ID of user", required = true) @PathVariable @NotBlank String id) {
    log.debug("GET /api/v1/profile-picture{} - Getting Picture by user ID", id);
    byte[] picture = minIOService.getProfilePicture(id);
    return ResponseEntity.status(HttpStatus.OK).body(picture);
  }

  /**
   * Sets a Users ProfilePic in the MinIO Object Store.
   *
   * @param id The ID of the user.
   * @param file picture data (jpg/png/webp/gif), multipart field "file"
   */
  @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "set profile picture",
      description =
          "Uploads/overwrites the profile picture of the given user in MinIO and Database.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Profile picture set successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "MinIO operation failed"),
      })
  public ResponseEntity<Void> setProfilePicture(
      @PathVariable @NotBlank String id, @RequestParam MultipartFile file) throws IOException {
    log.debug("POST /api/v1/profile-picture/{} - uploading profile picture", id);
    minIOService.setProfilePicture(id, file.getBytes(), file.getContentType());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /**
   * Deletes a user's profile picture from the MinIO Object Store.
   *
   * @param id the ID of the user whose profile picture should be deleted
   * @return 204 No Content if deleted, 400 if invalid ID, 500 if MinIO operation failed
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete profile picture",
      description = "Deletes profile picture of given User in MinIO and Database")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Profile picture deleted"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID"),
        @ApiResponse(responseCode = "500", description = "MinIO operation failed")
      })
  public ResponseEntity<Void> deleteProfilePicture(
      @Parameter(description = "ID of user", required = true) @PathVariable @NotBlank String id) {
    log.debug("DELETE /api/v1/profile-picture/{} - Deleting profile picture", id);
    minIOService.deleteProfilePicture(id);
    return ResponseEntity.noContent().build(); // 204
  }
}
