package com.ase.stammdatenverwaltung.security;

import com.ase.stammdatenverwaltung.logging.ExceptionContext;
import com.ase.stammdatenverwaltung.logging.LoggingHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Custom AccessDeniedHandler that logs role information when authorization checks fail.
 *
 * <p>When an endpoint returns a 403 Forbidden response due to insufficient permissions, this
 * handler: - Logs the access violation with user role details using {@link LoggingHelper} -
 * Provides valuable debugging information for troubleshooting authorization issues - Sanitizes
 * sensitive information before logging
 *
 * <p>This is useful for debugging why specific users cannot access certain endpoints in both
 * development and production environments.
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

    String requestUri = request.getRequestURI();
    String method = request.getMethod();

    // Extract authentication and role details
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication != null ? authentication.getName() : "anonymous";
    String userRoles =
        authentication != null
            ? authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.joining(", "))
            : "none";

    ExceptionContext ctx =
        ExceptionContext.builder()
            .errorCode("AUTHZ_001")
            .errorCategory("Authorization - Insufficient Permissions")
            .status(HttpStatus.FORBIDDEN)
            .userMessage("Access denied - insufficient permissions")
            .technicalMessage("AccessDeniedException: " + accessDeniedException.getMessage())
            .withContext("endpoint", requestUri)
            .withContext("method", method)
            .withContext("username", username)
            .withContext("userRoles", userRoles)
            .cause(accessDeniedException)
            .build();

    LoggingHelper.logSecurity(ctx, "password", "credentials", "token");

    // Set response status
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // Send JSON error response
    final Map<String, Object> body =
        Map.of(
            "error",
            ctx.getErrorCode(),
            "message",
            ctx.getUserMessage(),
            "details",
            ctx.getTechnicalMessage(),
            "endpoint",
            requestUri,
            "timestamp",
            System.currentTimeMillis());

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
