package com.ase.stammdatenverwaltung.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ase.stammdatenverwaltung.services.MinIOService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {"spring.profiles.active=dev", "minio.enabled=true", "keycloak.enabled=true"})
@AutoConfigureMockMvc
@DisplayName("Security Dev toggle when Keycloak enabled")
class SecurityDevEnabledTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private MinIOService minIOService;

  @Test
  @DisplayName("When Keycloak is enabled in dev, protected endpoints require auth (401)")
  void whenKeycloakEnabled_thenReturn401() throws Exception {
    mockMvc.perform(get("/api/v1/profile-picture/user-123")).andExpect(status().isUnauthorized());
  }
}

@SpringBootTest(
    properties = {"spring.profiles.active=dev", "minio.enabled=true", "keycloak.enabled=false"})
@AutoConfigureMockMvc
@DisplayName("Security Dev toggle when Keycloak disabled")
class SecurityDevDisabledTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private MinIOService minIOService;

  @Test
  @DisplayName("When Keycloak is disabled in dev, protected endpoints are accessible (not 401)")
  void whenKeycloakDisabled_thenNot401() throws Exception {
    mockMvc.perform(get("/api/v1/profile-picture/user-123")).andExpect(status().isNotFound());
  }
}
