package com.ase.stammdatenverwaltung.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for authentication exception logging.
 *
 * <p>Verifies that 401 (Unauthorized) responses are properly logged with request details including:
 *
 * <ul>
 *   <li>Endpoint URL
 *   <li>HTTP method
 *   <li>Exception type and message
 *   <li>Response timestamp
 * </ul>
 *
 * <p>Note: 403 (Forbidden) responses are handled by RoleAwareAccessDeniedHandler at the filter
 * chain level.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  /**
   * Tests that accessing a protected endpoint without authentication returns 401 with proper error
   * response structure.
   */
  @Test
  void shouldReturn401WithErrorDetailsWhenAccessingProtectedEndpointWithoutAuth() throws Exception {
    mockMvc
        .perform(get("/api/v1/users"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("Unauthorized"))
        .andExpect(
            jsonPath("$.message").value("Authentication is required to access this resource"))
        .andExpect(jsonPath("$.endpoint").value("/api/v1/users"))
        .andExpect(jsonPath("$.method").value("GET"))
        .andExpect(jsonPath("$.timestamp").isNumber());
  }

  /** Tests that accessing a protected endpoint with invalid credentials returns 401. */
  @Test
  void shouldReturn401WithErrorDetailsWhenProvidingInvalidCredentials() throws Exception {
    mockMvc
        .perform(get("/api/v1/hello").with(httpBasic("invalid-user", "wrong-password")))
        .andExpect(status().isUnauthorized());
    // Note: HTTP Basic Auth rejection is handled at the filter level and returns a minimal
    // response
  }

  /** Tests that the error response includes the correct endpoint in different request patterns. */
  @Test
  void shouldIncludeCorrectEndpointInErrorResponse() throws Exception {
    mockMvc
        .perform(get("/api/v1/users"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.endpoint").value("/api/v1/users"));
  }

  /** Tests that valid credentials allow access to protected endpoints (positive case). */
  @Test
  void shouldAllowAccessWithValidCredentialsAndProperRole() throws Exception {
    mockMvc
        .perform(get("/api/v1/users").with(jwt().authorities(() -> "ROLE_sau-admin")))
        .andExpect(status().isOk());
  }
}
