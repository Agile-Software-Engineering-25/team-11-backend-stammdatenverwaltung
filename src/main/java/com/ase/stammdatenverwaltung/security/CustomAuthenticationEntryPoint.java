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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Custom authentication entry point to log 401 (Unauthorized) responses at the filter level.
 *
 * <p>This is called when an unauthenticated user tries to access a protected resource. It logs:
 *
 * <ul>
 *   <li>The endpoint that was attempted
 *   <li>The HTTP method used
 *   <li>The authentication exception type and message
 *   <li>Timestamp of the failure
 * </ul>
 *
 * <p>This handler captures 401 responses earlier in the filter chain than @ControllerAdvice,
 * ensuring all unauthorized access attempts are logged consistently.
 */
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Handles authentication entry point and logs the unauthorized request.
   *
   * <p>Called by Spring Security filters when an AuthenticationException is thrown and the user is
   * not authenticated.
   *
   * @param request the HTTP request
   * @param response the HTTP response
   * @param authException the authentication exception
   * @throws IOException if response writing fails
   * @throws ServletException if servlet error occurs
   */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    String requestUri = request.getRequestURI();
    String method = request.getMethod();
    String queryString = request.getQueryString();
    String remoteAddr = request.getRemoteAddr();
    String userAgent = request.getHeader("User-Agent");
    String authHeader = request.getHeader("Authorization");

    log.warn(
        "Unauthorized access attempt (401) to endpoint: {} {} | Exception: {} | Message: '{}'",
        method,
        requestUri,
        authException.getClass().getSimpleName(),
        authException.getMessage());

    log.debug(
        "401 Request details - Remote IP: {} | Query: {} | User-Agent: {} | Has Auth Header: {}",
        remoteAddr,
        queryString != null ? queryString : "none",
        userAgent,
        authHeader != null);

    if (authException.getCause() != null) {
      log.debug("401 Exception cause: {}", authException.getCause().getMessage(), authException.getCause());
    }

    // Send JSON response with 401 status
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    Map<String, Object> body = new HashMap<>();
    body.put("error", "Unauthorized");
    body.put("message", "Authentication is required to access this resource");
    body.put("details", authException.getMessage());
    body.put("endpoint", requestUri);
    body.put("method", method);
    body.put("timestamp", System.currentTimeMillis());

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
