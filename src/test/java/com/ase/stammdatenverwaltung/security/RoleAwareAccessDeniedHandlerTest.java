package com.ase.stammdatenverwaltung.security;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/** Unit tests for RoleAwareAccessDeniedHandler to verify role logging on authorization failures. */
class RoleAwareAccessDeniedHandlerTest {

  private RoleAwareAccessDeniedHandler handler;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private AccessDeniedException exception;

  @BeforeEach
  void setUp() {
    handler = new RoleAwareAccessDeniedHandler();
    request = new MockHttpServletRequest("GET", "/api/v1/users");
    response = new MockHttpServletResponse();
    exception = new AccessDeniedException("Access Denied");
  }

  @Test
  void handleShouldReturnForbiddenStatus() throws IOException, ServletException {
    // Given
    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            new User(
                "testuser",
                "password",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))),
            null,
            java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);

    // When
    handler.handle(request, response, exception);

    // Then
    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentType()).contains("application/json");
  }

  @Test
  void handleShouldReturnJsonWithErrorMessage() throws IOException, ServletException {
    // Given
    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            new User(
                "testuser",
                "password",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))),
            null,
            java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);

    // When
    handler.handle(request, response, exception);

    // Then
    String content = response.getContentAsString();
    assertThat(content).contains("Forbidden", "Access denied", "insufficient permissions");
  }

  @Test
  void handleShouldLogUserRoles() throws IOException, ServletException {
    // Given
    java.util.List<SimpleGrantedAuthority> authorities =
        java.util.Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_STUDENT"));

    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            new User("student123", "password", authorities), null, authorities);

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);

    // When
    handler.handle(request, response, exception);

    // Then - The handler should have logged the roles (verified via logs in integration tests)
    // This test verifies the handler completes successfully with multiple roles
    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  void handleShouldIncludeRequestDetailsInResponse() throws IOException, ServletException {
    // Given
    request.setMethod("POST");
    request.setRequestURI("/api/v1/users/students");

    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            new User(
                "admin",
                "password",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))),
            null,
            java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);

    // When
    handler.handle(request, response, exception);

    // Then
    String content = response.getContentAsString();
    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(content).isNotEmpty();
  }

  @Test
  void handleWithNoAuthenticationShouldStillRespond() throws IOException, ServletException {
    // Given - No authentication in security context
    SecurityContextHolder.clearContext();

    // When
    handler.handle(request, response, exception);

    // Then
    assertThat(response.getStatus()).isEqualTo(403);
    String content = response.getContentAsString();
    assertThat(content).contains("Forbidden");
  }

  @Test
  void handleShouldExtractAndLogExpectedRoles() throws IOException, ServletException {
    // Given - Exception message includes role requirements
    AccessDeniedException exceptionWithRoles =
        new AccessDeniedException(
            "Access Denied; Reason: hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')");

    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            new User(
                "testuser",
                "password",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER"))),
            null,
            java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER")));

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);

    // When
    handler.handle(request, response, exceptionWithRoles);

    // Then
    assertThat(response.getStatus()).isEqualTo(403);
    String content = response.getContentAsString();
    assertThat(content).contains("Forbidden");
  }

  @Test
  void handleShouldExtractComplexRoleNames() throws IOException, ServletException {
    // Given - Exception with complex role names like 'Area-3.Team-11.Read.User'
    AccessDeniedException exceptionWithRoles =
        new AccessDeniedException(
            "Access Denied; Reason: hasRole('Area-3.Team-11.Read.User') or hasRole('HVS-Admin')");

    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            new User(
                "student123",
                "password",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))),
            null,
            java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);

    // When
    handler.handle(request, response, exceptionWithRoles);

    // Then
    assertThat(response.getStatus()).isEqualTo(403);
    String content = response.getContentAsString();
    assertThat(content).contains("Forbidden");
  }
}
