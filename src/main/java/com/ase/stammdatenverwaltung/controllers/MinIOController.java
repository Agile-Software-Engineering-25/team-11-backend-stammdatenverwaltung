package com.ase.stammdatenverwaltung.controllers;

/** REST controller for managing MinIO Uploads, primarily profile pictures of users */
@RestController
@RequestMapping("/api/v1/profile-picture")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MinIO Pictures", description = "API for profile picture management")
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
  public ResponseEntity<Byte[]> getProfilePicture(
      @Parameter(description = "ID of user", required = true) @PathVariable String id) {
    log.debug("GET /api/v1/profile-picture{} - Getting Picture by user ID", id);
    Byte[] picture = minIOService.getProfilePicture(id);
    return ResponseEntity.status(HttpStatus.CREATED).body(picture);
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
  @ApiResponse(
      value = {
        @ApiResponse(responseCode = "201", description = "Profile picture set successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
      })
  public ResponseEntity<Void> setProfilePicture(
      @PathVariable("id") String id, @RequestParam("file") MultipartFile file) {
    log.debug("POST /api/v1/users/{}/avatar – uploading profile picture", id);
    try {
      MinIOService.setProfilePicture(id, file.getBytes(), file.getContentType());
      return ResponseEntity.status(HTTPStatus.CREATED).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  /**
   * Delete a Users ProfilePic in the MinIO Object Store.
   *
   * @param id The ID of the user.
   * @param file picture data (jpg/png/webp/gif), multipart field "file"
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete profile picture",
      description = "Deletes profile picture of given User in MinIO and Database")
  @ApiResponse(
      value = {
        @ApiResponse(responseCode = "204", description = "Profile picture deleted"),
        @ApiResponse(responseCode = "404", description = "No profile picture found")
      })
  public ResponseEntity<Void> deleteProfilePicture(
      @Parameter(description = "ID of user", required = true) @PathVariable("id") String id) {
    log.debug("POST /api/v1/users/{}/avatar – Deleting profile picture", id);
    try {
      MinIOService.deleteProfilePicture(id);
      return ResponseEntity.noContent().build(); // 204
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400
    }
  }
}
