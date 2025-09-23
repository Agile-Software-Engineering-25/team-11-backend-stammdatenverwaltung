package com.ase.stammdatenverwaltung.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for security configuration. Tests authentication and authorization for
 * different endpoint patterns and user roles.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldAllowPublicAccessToPublicEndpoints() throws Exception {
    mockMvc.perform(get("/api/v1/public/hello")).andExpect(status().isOk());
  }

  @Test
  void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
    mockMvc.perform(get("/api/v1/hello")).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAllowBasicAuthForProtectedEndpoints() throws Exception {
    mockMvc
        .perform(get("/api/v1/hello").with(httpBasic("dev-user", "dev-password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Hello from protected endpoint!"))
        .andExpect(jsonPath("$.user").value("dev-user"));
  }

  @Test
  void shouldAllowJwtForProtectedEndpoints() throws Exception {
    Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .claim("sub", "test-user")
            .claim("aud", "stammdatenverwaltung-api")
            .claim("realm_access", java.util.Map.of("roles", java.util.List.of("user")))
            .build();

    mockMvc
        .perform(get("/api/v1/hello").with(jwt().jwt(jwt)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Hello from protected endpoint!"));
  }

  @Test
  void shouldEnforceAdminRoleForAdminEndpoints() throws Exception {
    // Test without admin role (should fail)
    mockMvc
        .perform(get("/api/v1/admin/users").with(httpBasic("dev-user", "dev-password")))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowAdminRoleForAdminEndpoints() throws Exception {
    // Test with admin role (should succeed)
    mockMvc
        .perform(get("/api/v1/admin/users").with(httpBasic("dev-admin", "dev-password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Admin endpoint accessed successfully!"))
        .andExpect(jsonPath("$.admin").value("dev-admin"));
  }

  @Test
  void shouldEnforceUserRoleForUserEndpoints() throws Exception {
    // Test with user role (should succeed)
    mockMvc
        .perform(get("/api/v1/user/profile").with(httpBasic("dev-user", "dev-password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("User profile accessed successfully!"));
  }

  @Test
  void shouldValidateJWTTokenWithRoles() throws Exception {
    Jwt jwt =
        Jwt.withTokenValue("mock-token")
            .header("alg", "RS256")
            .claim("sub", "admin-user")
            .claim("preferred_username", "admin-user")
            .claim("aud", "stammdatenverwaltung-api")
            .claim("realm_access", java.util.Map.of("roles", java.util.List.of("admin", "user")))
            .build();

    mockMvc
        .perform(
            get("/api/v1/admin/users")
                .with(
                    jwt()
                        .jwt(jwt)
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_ADMIN"),
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Admin endpoint accessed successfully!"))
        .andExpect(jsonPath("$.admin").value("admin-user"));
  }

  @Test
  void shouldRejectJWTTokenWithoutRequiredRole() throws Exception {
    Jwt jwt =
        Jwt.withTokenValue("mock-token")
            .header("alg", "RS256")
            .claim("sub", "regular-user")
            .claim("preferred_username", "regular-user")
            .claim("aud", "stammdatenverwaltung-api")
            .claim("realm_access", java.util.Map.of("roles", java.util.List.of("user")))
            .build();

    mockMvc
        .perform(get("/api/v1/admin/users").with(jwt().jwt(jwt)))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldRedirectToSwaggerInDev() throws Exception {
    // Swagger UI redirects to index page - test the redirect behavior
    mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
  }

  @Test
  void shouldAllowAccessToActuatorHealth() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }
}
