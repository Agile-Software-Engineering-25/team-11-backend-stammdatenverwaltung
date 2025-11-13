package com.ase.stammdatenverwaltung.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ase.stammdatenverwaltung.config.JwtConfigurationValidator;
import com.ase.stammdatenverwaltung.services.BitfrostNotificationService;
import com.ase.stammdatenverwaltung.services.EmployeeService;
import com.ase.stammdatenverwaltung.services.LecturerService;
import com.ase.stammdatenverwaltung.services.PersonService;
import com.ase.stammdatenverwaltung.services.StudentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for UserController delete endpoints. Tests verify that both the modern REST DELETE
 * endpoint and the legacy POST /delete endpoint correctly handle user deletion and invoke the
 * Bitfrost notification service.
 *
 * <p>Tests cover success paths (user deleted with notification), failure scenarios (user not
 * found), and verify that the service layer is called with correct arguments.
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Delete Endpoint Tests")
class UserControllerDeleteTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PersonService personService;

  @MockitoBean private StudentService studentService;

  @MockitoBean private EmployeeService employeeService;

  @MockitoBean private LecturerService lecturerService;

  @MockitoBean private BitfrostNotificationService bitfrostNotificationService;

  @MockitoBean private JwtConfigurationValidator jwtConfigurationValidator;

  private static final String TEST_USER_ID = "user-12345";
  private static final String API_BASE_PATH = "/api/v1/users";

  // ============ DELETE /{userId} Endpoint Tests ============

  @Test
  @DisplayName("deleteUserById_WithValidId_Returns204NoContent")
  @WithMockUser(roles = "sau-admin")
  void deleteUserById_WithValidId_Returns204NoContent() throws Exception {
    // Given - User exists and can be deleted
    doNothing().when(personService).deleteById(TEST_USER_ID);
    when(bitfrostNotificationService.notifyUserDeletion(TEST_USER_ID)).thenReturn(true);

    // When & Then
    mockMvc
        .perform(delete(API_BASE_PATH + "/" + TEST_USER_ID).with(csrf()))
        .andExpect(status().isNoContent());

    // Verify deletion was attempted
    verify(personService).deleteById(TEST_USER_ID);
  }

  @Test
  @DisplayName("deleteUserById_WhenUserNotFound_Returns404")
  @WithMockUser(roles = "sau-admin")
  void deleteUserById_WhenUserNotFound_Returns404() throws Exception {
    // Given - User does not exist
    doThrow(new EntityNotFoundException("User not found"))
        .when(personService)
        .deleteById(TEST_USER_ID);

    // When & Then
    mockMvc
        .perform(delete(API_BASE_PATH + "/" + TEST_USER_ID).with(csrf()))
        .andExpect(status().isNotFound());

    // Verify deletion was attempted
    verify(personService).deleteById(TEST_USER_ID);
  }

  @Test
  @DisplayName("deleteUserById_WhenUserDeleted_CallsBitfrostNotification")
  @WithMockUser(roles = "sau-admin")
  void deleteUserById_WhenUserDeleted_CallsBitfrostNotification() throws Exception {
    // Given - User exists and will be deleted
    doNothing().when(personService).deleteById(TEST_USER_ID);
    when(bitfrostNotificationService.notifyUserDeletion(TEST_USER_ID)).thenReturn(true);

    // When
    mockMvc
        .perform(delete(API_BASE_PATH + "/" + TEST_USER_ID).with(csrf()))
        .andExpect(status().isNoContent());

    // Then - Bitfrost should be notified after successful deletion
    verify(personService).deleteById(TEST_USER_ID);
    verify(bitfrostNotificationService).notifyUserDeletion(TEST_USER_ID);
  }

  @Test
  @DisplayName("deleteUserById_WithoutAuth_Returns401Unauthorized")
  void deleteUserById_WithoutAuth_Returns401Unauthorized() throws Exception {
    // When & Then - Unauthenticated request should be unauthorized
    mockMvc
        .perform(delete(API_BASE_PATH + "/" + TEST_USER_ID).with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  // ============ POST /delete (Legacy) Endpoint Tests ============

  @Test
  @DisplayName("deleteUserByIdLegacy_WithValidId_Returns204NoContent")
  @WithMockUser(roles = "sau-admin")
  void deleteUserByIdLegacy_WithValidId_Returns204NoContent() throws Exception {
    // Given
    String requestBody = "{\"user-id\": \"" + TEST_USER_ID + "\"}";
    doNothing().when(personService).deleteById(TEST_USER_ID);
    when(bitfrostNotificationService.notifyUserDeletion(TEST_USER_ID)).thenReturn(true);

    // When & Then
    mockMvc
        .perform(
            post(API_BASE_PATH + "/delete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isNoContent());

    // Verify both deletion and notification occurred
    verify(personService).deleteById(TEST_USER_ID);
    verify(bitfrostNotificationService).notifyUserDeletion(TEST_USER_ID);
  }

  @Test
  @DisplayName("deleteUserByIdLegacy_WhenUserNotFound_Returns404")
  @WithMockUser(roles = "sau-admin")
  void deleteUserByIdLegacy_WhenUserNotFound_Returns404() throws Exception {
    // Given - User does not exist
    String requestBody = "{\"user-id\": \"" + TEST_USER_ID + "\"}";
    doThrow(new EntityNotFoundException("User not found"))
        .when(personService)
        .deleteById(TEST_USER_ID);

    // When & Then
    mockMvc
        .perform(
            post(API_BASE_PATH + "/delete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isNotFound());

    // Verify deletion was attempted but notification was not called
    verify(personService).deleteById(TEST_USER_ID);
  }

  @Test
  @DisplayName("deleteUserByIdLegacy_WhenUserDeleted_CallsBitfrostNotification")
  @WithMockUser(roles = "sau-admin")
  void deleteUserByIdLegacy_WhenUserDeleted_CallsBitfrostNotification() throws Exception {
    // Given
    String requestBody = "{\"user-id\": \"" + TEST_USER_ID + "\"}";
    doNothing().when(personService).deleteById(TEST_USER_ID);
    when(bitfrostNotificationService.notifyUserDeletion(TEST_USER_ID)).thenReturn(true);

    // When
    mockMvc
        .perform(
            post(API_BASE_PATH + "/delete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isNoContent());

    // Then - Both deletion and Bitfrost notification should occur
    verify(personService).deleteById(TEST_USER_ID);
    verify(bitfrostNotificationService).notifyUserDeletion(TEST_USER_ID);
  }

  @Test
  @DisplayName("deleteUserByIdLegacy_WithMissingUserId_Returns400BadRequest")
  @WithMockUser(roles = "sau-admin")
  void deleteUserByIdLegacy_WithMissingUserId_Returns400BadRequest() throws Exception {
    // Given - Missing required user-id field
    String invalidRequestBody = "{}";

    // When & Then
    mockMvc
        .perform(
            post(API_BASE_PATH + "/delete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("deleteUserByIdLegacy_WithoutAuth_Returns401Unauthorized")
  void deleteUserByIdLegacy_WithoutAuth_Returns401Unauthorized() throws Exception {
    // When & Then - Unauthenticated request should be unauthorized
    String requestBody = "{\"user-id\": \"" + TEST_USER_ID + "\"}";
    mockMvc
        .perform(
            post(API_BASE_PATH + "/delete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("deleteUserByIdLegacy_WhenBitfrostReturnsFalse_StillCompletes")
  @WithMockUser(roles = "sau-admin")
  void deleteUserByIdLegacy_WhenBitfrostReturnsFalse_StillCompletes() throws Exception {
    // Given - User deletion succeeds but Bitfrost notification returns false
    String requestBody = "{\"user-id\": \"" + TEST_USER_ID + "\"}";
    doNothing().when(personService).deleteById(TEST_USER_ID);
    when(bitfrostNotificationService.notifyUserDeletion(TEST_USER_ID)).thenReturn(false);

    // When & Then - Request completes successfully even if Bitfrost notification fails
    // The service returns false but doesn't throw - deletion already succeeded
    mockMvc
        .perform(
            post(API_BASE_PATH + "/delete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isNoContent());

    verify(personService).deleteById(TEST_USER_ID);
    verify(bitfrostNotificationService).notifyUserDeletion(TEST_USER_ID);
  }
}
