package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.dto.ProfilePictureData;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureStorageException;
import com.ase.stammdatenverwaltung.services.MinIOService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

  // Allowed MIME types for profile pictures
  private static final Set<String> ALLOWED_CONTENT_TYPES =
      new HashSet<>(
          Arrays.asList(
              "image/jpeg", "image/png", "image/webp", "image/gif", "application/octet-stream"));

  // Maximum file size: 1 MiB
  private static final int KB = 1024;
  private static final int MB = KB * KB;
  private static final long MAX_FILE_SIZE_BYTES = 1L * MB;

  /**
   * Get profile picture by user ID.
   *
   * @param id the user ID
   * @return the picture if found with appropriate content-type, or 404 if not found
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get profile picture by user ID",
      description = "Get profile picture by user ID from MinIO")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Picture found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Picture not found")
      })
  @PreAuthorize(
      "hasRole('Area-3.Team-11.Read.Student') or hasRole('Area-3.Team-11.Read.Employee') or hasRole('Area-3.Team-11.Read.Lecturer') or hasRole('HVS-Admin') or hasRole('Hochschulverwaltungsmitarbeiter')")
  public ResponseEntity<byte[]> getProfilePicture(
      @Parameter(description = "ID of user", required = true) @PathVariable @NotBlank String id) {
    log.debug("GET /api/v1/profile-picture/{} - Getting Picture by user ID", id);
    ProfilePictureData pictureData = minIOService.getProfilePicture(id);

    if (pictureData == null
        || pictureData.getPicture() == null
        || pictureData.getPicture().length == 0) {
      log.warn("Profile picture not found for user ID: {}", id);
      return ResponseEntity.notFound().build();
    }

    ResponseEntity<byte[]> response =
        ResponseEntity.ok()
            .contentType(
                MediaType.parseMediaType(
                    pictureData.getContentType() != null
                        ? pictureData.getContentType()
                        : "application/octet-stream"))
            .body(pictureData.getPicture());
    return response;
  }

  /**
   * Sets a Users ProfilePic in the MinIO Object Store.
   *
   * <p>Validation: File must be a valid image format (PNG, JPEG, WebP, GIF) and not exceed 10 MiB.
   *
   * @param id The ID of the user.
   * @param file picture data (jpg/png/webp/gif), multipart field "file"
   * @return 201 Created if successful
   * @throws IllegalArgumentException if file validation fails
   * @throws IOException if file read fails
   * @throws ProfilePictureStorageException if MinIO storage fails
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
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "MinIO operation failed"),
      })
  @PreAuthorize(
      "hasRole('Area-3.Team-11.Write.Student') or hasRole('Area-3.Team-11.Write.Employee') or hasRole('Area-3.Team-11.Write.Lecturer') or hasRole('HVS-Admin') or hasRole('Hochschulverwaltungsmitarbeiter')")
  public ResponseEntity<Void> setProfilePicture(
      @PathVariable @NotBlank String id, @RequestParam MultipartFile file) throws IOException {
    log.debug("POST /api/v1/profile-picture/{} - uploading profile picture", id);

    // Validate file is not empty
    if (file.isEmpty()) {
      log.warn("Profile picture upload rejected: file is empty for user ID: {}", id);
      throw new IllegalArgumentException("Profile picture file cannot be empty");
    }

    // Validate file size (max 10 MiB)
    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
      log.warn(
          "Profile picture upload rejected: file too large ({} bytes, max {} MiB) for user ID: {}",
          file.getSize(),
          MAX_FILE_SIZE_BYTES / MB,
          id);
      throw new IllegalArgumentException(
          String.format(
              "Profile picture file exceeds maximum size of %d MiB", MAX_FILE_SIZE_BYTES / MB));
    }

    // Validate content type
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
      log.warn(
          "Profile picture upload rejected: invalid content type '{}' for user ID: {}",
          contentType,
          id);
      throw new IllegalArgumentException(
          String.format(
              "Invalid file type '%s'. Allowed types: image/jpeg, image/png, image/webp, image/gif",
              contentType));
    }

    // Upload profile picture (IOException and ProfilePictureStorageException bubble up to handler)
    byte[] pictureBytes = file.getBytes();
    minIOService.setProfilePicture(id, pictureBytes, contentType);

    log.info(
        "Profile picture uploaded successfully for user ID: {} (size: {} bytes, content-type: {})",
        id,
        pictureBytes.length,
        contentType);
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
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "MinIO operation failed")
      })
  @PreAuthorize(
      "hasRole('Area-3.Team-11.Write.Student') or hasRole('Area-3.Team-11.Write.Employee') or hasRole('Area-3.Team-11.Write.Lecturer') or hasRole('HVS-Admin') or hasRole('Hochschulverwaltungsmitarbeiter')")
  public ResponseEntity<Void> deleteProfilePicture(
      @Parameter(description = "ID of user", required = true) @PathVariable @NotBlank String id) {
    log.debug("DELETE /api/v1/profile-picture/{} - Deleting profile picture", id);
    minIOService.deleteProfilePicture(id);
    return ResponseEntity.noContent().build(); // 204
  }
}
