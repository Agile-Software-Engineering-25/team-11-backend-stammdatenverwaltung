package com.ase.stammdatenverwaltung.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Custom access denied handler to log 403 (Forbidden) responses at the filter level.
 *
 * <p>This is called when an authenticated user tries to access a resource without the required
 * permissions/roles. It logs:
 *
 * <ul>
 *   <li>The endpoint that was attempted
 *   <li>The HTTP method used
 *   <li>The reason for access denial
 *   <li>Timestamp of the denial
 * </ul>
 *
 * <p>This handler captures 403 responses in the filter chain, ensuring all unauthorized access
 * attempts (due to insufficient permissions) are logged consistently.
 */
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Handles access denied situations and logs the request.
   *
   * <p>Called by Spring Security when an AccessDeniedException is thrown, typically when a user
   * lacks required authorities/roles for an endpoint.
   *
   * @param request the HTTP request
   * @param response the HTTP response
   * @param accessDeniedException the access denied exception
   * @throws IOException if response writing fails
   * @throws ServletException if servlet error occurs
   */
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {

    String requestUri = request.getRequestURI();
    String method = request.getMethod();

    log.warn(
        "Access denied for endpoint: {} {} | Exception: {} | Message: '{}'",
        method,
        requestUri,
        accessDeniedException.getClass().getSimpleName(),
        accessDeniedException.getMessage());

    // Send JSON response with 403 status
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);

    Map<String, Object> body = new HashMap<>();
    body.put("error", "Forbidden");
    body.put("message", "You do not have permission to access this resource");
    body.put("details", accessDeniedException.getMessage());
    body.put("endpoint", requestUri);
    body.put("method", method);
    body.put("timestamp", System.currentTimeMillis());

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
