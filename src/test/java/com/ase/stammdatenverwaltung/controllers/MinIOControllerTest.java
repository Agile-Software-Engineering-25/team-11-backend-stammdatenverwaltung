package com.ase.stammdatenverwaltung.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ase.stammdatenverwaltung.config.JwtConfigurationValidator;
import com.ase.stammdatenverwaltung.dto.ProfilePictureData;
import com.ase.stammdatenverwaltung.exceptions.ProfilePictureStorageException;
import com.ase.stammdatenverwaltung.services.MinIOService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for MinIOController. Tests verify HTTP endpoint behavior and response codes when
 * MinIOService operations succeed or fail. A live MinIO instance is not required.
 *
 * <p>Suppressed warnings: - "null": Spring Test and MockMvc framework use annotations that may
 * generate null safety warnings even though the framework guarantees these values are not null at
 * runtime.
 */
@SuppressWarnings("null")
@WebMvcTest(MinIOController.class)
@DisplayName("MinIOController Tests")
@org.springframework.test.context.TestPropertySource(properties = "minio.enabled=true")
class MinIOControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private MinIOService minIOService;

  @MockitoBean private JwtConfigurationValidator jwtConfigurationValidator;

  private static final String TEST_USER_ID = "user-123";
  private static final String API_BASE_PATH = "/api/v1/profile-picture";
  private static final byte[] TEST_IMAGE_DATA =
      new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}; // PNG header

  // GET Profile Picture Tests

  @Test
  @DisplayName("getProfilePicture_WithValidId_Returns200AndImageData")
  @WithMockUser
  void getProfilePicture_WithValidId_Returns200AndImageData() throws Exception {
    // Given
    ProfilePictureData pictureData = new ProfilePictureData(TEST_IMAGE_DATA, "image/png");
    when(minIOService.getProfilePicture(TEST_USER_ID)).thenReturn(pictureData);

    // When & Then
    mockMvc
        .perform(get(API_BASE_PATH + "/" + TEST_USER_ID))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG))
        .andExpect(content().bytes(TEST_IMAGE_DATA));

    verify(minIOService).getProfilePicture(TEST_USER_ID);
  }

  @Test
  @DisplayName("getProfilePicture_WhenPictureNotFound_Returns404")
  @WithMockUser
  void getProfilePicture_WhenPictureNotFound_Returns404() throws Exception {
    // Given - return empty picture data
    ProfilePictureData emptyData = new ProfilePictureData(new byte[0], "image/png");
    when(minIOService.getProfilePicture(TEST_USER_ID)).thenReturn(emptyData);

    // When & Then
    mockMvc.perform(get(API_BASE_PATH + "/" + TEST_USER_ID)).andExpect(status().isNotFound());

    verify(minIOService).getProfilePicture(TEST_USER_ID);
  }

  @Test
  @DisplayName("setProfilePicture_WithValidPNG_Returns201")
  @WithMockUser
  void setProfilePicture_WithValidPNG_Returns201() throws Exception {
    // Given
    MockMultipartFile file =
        new MockMultipartFile("file", "test.png", "image/png", TEST_IMAGE_DATA);
    doNothing()
        .when(minIOService)
        .setProfilePicture(eq(TEST_USER_ID), any(byte[].class), any(String.class));

    // When & Then
    mockMvc
        .perform(
            multipart(HttpMethod.POST, API_BASE_PATH + "/" + TEST_USER_ID)
                .file(file)
                .with(csrf())
                .with(
                    request -> {
                      request.setMethod("POST");
                      return request;
                    }))
        .andExpect(status().isCreated());

    verify(minIOService).setProfilePicture(eq(TEST_USER_ID), any(byte[].class), any(String.class));
  }

  @Test
  @DisplayName("setProfilePicture_WithEmptyFile_Returns400")
  @WithMockUser
  void setProfilePicture_WithEmptyFile_Returns400() throws Exception {
    // Given
    MockMultipartFile emptyFile =
        new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

    // When & Then
    mockMvc
        .perform(
            multipart(HttpMethod.POST, API_BASE_PATH + "/" + TEST_USER_ID)
                .file(emptyFile)
                .with(csrf())
                .with(
                    request -> {
                      request.setMethod("POST");
                      return request;
                    }))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("setProfilePicture_WithInvalidContentType_Returns400")
  @WithMockUser
  void setProfilePicture_WithInvalidContentType_Returns400() throws Exception {
    // Given
    MockMultipartFile invalidFile =
        new MockMultipartFile("file", "test.pdf", "application/pdf", TEST_IMAGE_DATA);

    // When & Then
    mockMvc
        .perform(
            multipart(HttpMethod.POST, API_BASE_PATH + "/" + TEST_USER_ID)
                .file(invalidFile)
                .with(csrf())
                .with(
                    request -> {
                      request.setMethod("POST");
                      return request;
                    }))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("setProfilePicture_WhenServiceThrowsException_Returns500")
  @WithMockUser
  void setProfilePicture_WhenServiceThrowsException_Returns500() throws Exception {
    // Given
    MockMultipartFile file =
        new MockMultipartFile("file", "test.png", "image/png", TEST_IMAGE_DATA);
    doThrow(new ProfilePictureStorageException("MinIO error", TEST_USER_ID))
        .when(minIOService)
        .setProfilePicture(eq(TEST_USER_ID), any(byte[].class), any(String.class));

    // When & Then
    mockMvc
        .perform(
            multipart(HttpMethod.POST, API_BASE_PATH + "/" + TEST_USER_ID)
                .file(file)
                .with(csrf())
                .with(
                    request -> {
                      request.setMethod("POST");
                      return request;
                    }))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName("deleteProfilePicture_WithValidId_Returns204")
  @WithMockUser
  void deleteProfilePicture_WithValidId_Returns204() throws Exception {
    // Given
    doNothing().when(minIOService).deleteProfilePicture(TEST_USER_ID);

    // When & Then
    mockMvc
        .perform(delete(API_BASE_PATH + "/" + TEST_USER_ID).with(csrf()))
        .andExpect(status().isNoContent());

    verify(minIOService).deleteProfilePicture(TEST_USER_ID);
  }
}
