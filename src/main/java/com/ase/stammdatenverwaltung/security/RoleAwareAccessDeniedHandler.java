package com.ase.stammdatenverwaltung.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Custom AccessDeniedHandler that logs role information when authorization checks fail. Provides
 * valuable debugging information for troubleshooting authentication/authorization issues in
 * production and development environments.
 *
 * <p>When an endpoint returns a 403 Forbidden response due to insufficient permissions, this
 * handler logs: - The request endpoint and HTTP method - The current user's roles/authorities - The
 * expected roles for that endpoint - Any additional security context information
 *
 * <p>This is useful for debugging why specific users cannot access certain endpoints.
 */
@Slf4j
public class RoleAwareAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {

    // Extract authentication details
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Extract expected roles from the exception message if available
    String expectedRoles = extractExpectedRolesFromException(accessDeniedException);

    // Log the failed authorization attempt with role information
    if (authentication != null) {
      String roles =
          authentication.getAuthorities().stream()
              .map(auth -> auth.getAuthority())
              .collect(Collectors.joining(", "));

      String username = authentication.getName();

      if (expectedRoles != null && !expectedRoles.isEmpty()) {
        log.warn(
            "Authorization check failed - User: '{}', Roles: [{}], Expected: [{}], Request: {} {}",
            username,
            roles,
            expectedRoles,
            request.getMethod(),
            request.getRequestURI());
      } else {
        log.warn(
            "Authorization check failed - User: '{}', Roles: [{}], Request: {} {}",
            username,
            roles,
            request.getMethod(),
            request.getRequestURI());
      }
    } else {
      if (expectedRoles != null && !expectedRoles.isEmpty()) {
        log.warn(
            "Authorization check failed - No authentication found, Expected: [{}], Request: {} {}",
            expectedRoles,
            request.getMethod(),
            request.getRequestURI());
      } else {
        log.warn(
            "Authorization check failed - No authentication found, Request: {} {}",
            request.getMethod(),
            request.getRequestURI());
      }
    }

    // Set response status
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // Send JSON error response
    final Map<String, Object> body =
        Map.of(
            "status", HttpStatus.FORBIDDEN.value(),
            "error", "Forbidden",
            "message", "Access denied - insufficient permissions to access this resource");

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }

  /**
   * Extracts expected roles from the AccessDeniedException message. Spring Security's PreAuthorize
   * violations include role information in the exception message.
   *
   * @param exception the AccessDeniedException
   * @return a string containing the expected roles, or null if not available
   */
  private String extractExpectedRolesFromException(AccessDeniedException exception) {
    if (exception == null || exception.getMessage() == null) {
      return null;
    }

    String message = exception.getMessage();

    // Pattern to extract role requirements from Spring Security exception messages
    // Looks for patterns like "hasRole('ROLE_NAME')" or "hasRole('Area-3.Team-11.Read.User')"
    Pattern rolePattern = Pattern.compile("hasRole\\(['\"]([^'\"]+)['\"]\\)");
    Matcher matcher = rolePattern.matcher(message);

    StringBuilder roles = new StringBuilder();
    while (matcher.find()) {
      if (roles.length() > 0) {
        roles.append(", ");
      }
      roles.append(matcher.group(1));
    }

    return roles.length() > 0 ? roles.toString() : null;
  }
}
