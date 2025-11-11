package com.ase.stammdatenverwaltung.clients;

import com.ase.stammdatenverwaltung.config.KeycloakConfigProperties;
import com.ase.stammdatenverwaltung.dto.KeycloakUser;
import com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest;
import com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse;
import com.ase.stammdatenverwaltung.dto.keycloak.TokenResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client for interacting with the Keycloak administration API. Handles obtaining admin access
 * tokens and fetching user information.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakClient {

  private static final int MALFORMED_KEY_LENGTH = "\"init-password\"".length();
  private final WebClient webClient;
  private final KeycloakConfigProperties keycloakConfigProperties;
  private String accessToken;
  private Instant tokenExpirationTime;

  /**
   * Creates a new user in Keycloak via the wrapped user API.
   *
   * <p>Temporary workaround: the user API currently returns invalid JSON — an array followed by an
   * "init-password" field. Parsing is handled manually until the endpoint is fixed to return proper
   * JSON.
   */
  public Mono<CreateUserResponse> createUser(CreateUserRequest request) {
    return getAdminAccessToken()
        .flatMap(
            token ->
                webClient
                    .post()
                    .uri(keycloakConfigProperties.getUserApiUrl() + "/v1/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class) // <-- read raw text
                    .map(this::parseCreateUserResponseSafe)
                    .flatMap(Mono::justOrEmpty)
                    .doOnSuccess(
                        response ->
                            log.info(
                                "Successfully created user in Keycloak with ID: {}",
                                response != null ? response.getId() : "unknown"))
                    .doOnError(
                        error ->
                            log.error(
                                "Failed to create user in Keycloak for username: {}",
                                request.getUsername(),
                                error)));
  }

  /**
   * Finds a user in Keycloak by their ID. Returns an empty list if the user is not found (404) or
   * if the API call fails. This graceful handling prevents cascading failures when enriching user
   * data from external sources.
   *
   * @param userId The UUID of the user to find.
   * @return A Mono emitting a list of matching users, or empty list on 404 or error.
   */
  public Mono<List<KeycloakUser>> findUserById(String userId) {
    return getAdminAccessToken()
        .flatMap(
            token ->
                webClient
                    .get()
                    .uri(keycloakConfigProperties.getUserApiUrl() + "/v1/user?id=" + userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> {
                          log.debug(
                              "User not found in Keycloak for ID: {} (404 Not Found)", userId);
                          return Mono.empty();
                        })
                    .bodyToMono(String.class)
                    .map(this::parseFindUserByIdResponse)
                    .onErrorResume(
                        error -> {
                          log.warn(
                              "Failed to fetch user from Keycloak for ID: {} - returning empty list",
                              userId,
                              error);
                          return Mono.just(Collections.emptyList());
                        })
                    .doOnSuccess(
                        users -> {
                          if (users != null && !users.isEmpty()) {
                            log.info(
                                "Successfully fetched {} user(s) from Keycloak for ID: {}",
                                users.size(),
                                userId);
                          }
                        }))
        .onErrorResume(
            error -> {
              log.warn(
                  "Failed to fetch admin token or complete request for user ID: {} - returning empty list",
                  userId,
                  error);
              return Mono.just(Collections.emptyList());
            });
  }

  /**
   * Finds a user in Keycloak by their email. Returns an empty list if the user is not found (404)
   * or if the API call fails. This graceful handling prevents cascading failures when enriching
   * user data from external sources.
   *
   * @param email The email of the user to find.
   * @return A Mono emitting a list of matching users, or empty list on 404 or error.
   */
  public Mono<List<KeycloakUser>> findUserByEmail(String email) {
    return getAdminAccessToken()
        .flatMap(
            token ->
                webClient
                    .get()
                    .uri(keycloakConfigProperties.getUserApiUrl() + "/v1/user?email=" + email)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> {
                          log.debug(
                              "User not found in Keycloak for email: {} (404 Not Found)", email);
                          return Mono.empty();
                        })
                    .bodyToMono(String.class)
                    .map(this::parseFindUserByIdResponse)
                    .onErrorResume(
                        error -> {
                          log.warn(
                              "Failed to fetch user from Keycloak for email: {} - returning empty list",
                              email,
                              error);
                          return Mono.just(Collections.emptyList());
                        })
                    .doOnSuccess(
                        users -> {
                          if (users != null && !users.isEmpty()) {
                            log.info(
                                "Successfully fetched {} user(s) from Keycloak for email: {}",
                                users.size(),
                                email);
                          }
                        }))
        .onErrorResume(
            error -> {
              log.warn(
                  "Failed to fetch admin token or complete request for email: {} - returning empty list",
                  email,
                  error);
              return Mono.just(Collections.emptyList());
            });
  }

  private List<KeycloakUser> parseFindUserByIdResponse(String raw) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(raw, new TypeReference<List<KeycloakUser>>() {});
    } catch (Exception e) {
      log.error(
          "Failed to parse Keycloak user find by id response ({})", e.getClass().getSimpleName());
      log.debug("Failed to parse Keycloak user find by id response", e);
      log.debug("Raw response: {}", raw);
      return Collections.emptyList();
    }
  }

  /**
   * Obtains an admin access token from Keycloak using client credentials. The token is cached and
   * reused until it expires.
   *
   * @return A Mono emitting the admin access token string.
   */
  private Mono<String> getAdminAccessToken() {
    if (isTokenValid()) {
      return Mono.just(accessToken);
    }

    // Use the correct Keycloak token endpoint for sau-portal
    String tokenUrl =
        "https://keycloak.sau-portal.de/realms/"
            + keycloakConfigProperties.getRealm()
            + "/protocol/openid-connect/token";

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("client_id", keycloakConfigProperties.getClientId());
    formData.add("client_secret", keycloakConfigProperties.getClientSecret());
    formData.add("grant_type", keycloakConfigProperties.getGrantType());

    return webClient
        .post()
        .uri(tokenUrl)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(formData))
        .retrieve()
        .bodyToMono(TokenResponse.class)
        .doOnSuccess(
            tokenResponse -> {
              this.accessToken = tokenResponse.getAccessToken();
              this.tokenExpirationTime =
                  Instant.now()
                      .plus(
                          Duration.ofMinutes(
                              4)); // Assume token is valid for 5 minutes, with a 1-minute buffer
              log.info("Successfully obtained new admin access token.");
            })
        .map(TokenResponse::getAccessToken)
        .doOnError(error -> log.error("Failed to obtain admin access token", error));
  }

  private boolean isTokenValid() {
    return accessToken != null
        && tokenExpirationTime != null
        && Instant.now().isBefore(tokenExpirationTime);
  }

  /**
   * Temporary workaround: safely parse the malformed Keycloak response that returns a JSON array
   * followed by an "init-password" field on a new line. The response format is:
   * [{"id":"...","username":"...",...}] "init-password": "..."
   *
   * <p>TODO: Remove this when the other team's endpoint returns valid JSON.
   */
  private CreateUserResponse parseCreateUserResponseSafe(String raw) {
    try {
      log.debug("Parsing Keycloak response (length: {})", raw.length());

      // Find the LAST occurrence of "]" followed by newline and "init-password"
      // This is the end of the main array, not a nested array
      int endOfArray = findMainArrayEnd(raw);
      log.debug("End of main array found at index: {}", endOfArray);

      if (endOfArray <= 0) {
        log.warn("Unexpected Keycloak response format - no array end found: {}", raw);
        return null;
      }

      // Extract just the array part (including the closing bracket)
      String arrayJson = raw.substring(0, endOfArray + 1);
      log.debug("Extracted array JSON (length: {})", arrayJson.length());
      log.debug(
          "Array JSON last 100 chars: {}",
          arrayJson.substring(Math.max(0, arrayJson.length() - 100)));

      // Check for any trailing characters in the extracted part
      String afterArray = raw.substring(endOfArray + 1);
      log.debug(
          "Content after array (first 100 chars): {}",
          afterArray.substring(0, Math.min(100, afterArray.length())));

      // Parse the array
      ObjectMapper mapper = new ObjectMapper();
      log.debug("About to parse with Jackson...");
      List<CreateUserResponse> list = mapper.readValue(arrayJson, new TypeReference<>() {});
      log.debug("Successfully parsed, list size: {}", list.size());

      if (list.isEmpty()) {
        log.warn("Keycloak response contained empty array");
        return null;
      }

      // Extract the init-password from the malformed part if present
      String initPassword = extractInitPassword(afterArray);
      CreateUserResponse response = list.getFirst();

      if (initPassword != null && response != null) {
        log.debug("Extracted init-password from response: {}", initPassword);
      }

      return response;
    } catch (Exception e) {
      log.error("Failed to parse Keycloak user creation response", e);
      log.error("Raw response length: {}", raw.length());
      return null;
    }
  }

  /**
   * Finds the end of the main JSON array by looking for the pattern: ] "init-password"
   *
   * @param raw The full response string
   * @return The index of the closing bracket of the main array, or -1 if not found
   */
  private int findMainArrayEnd(String raw) {
    // Look for "init-password" in the response
    int initPasswordPos = raw.indexOf("\"init-password\"");
    if (initPasswordPos == -1) {
      // If there's no init-password, just find the last ]
      return raw.lastIndexOf("]");
    }

    // Find the last ] before "init-password"
    int pos = initPasswordPos - 1;
    while (pos >= 0) {
      char c = raw.charAt(pos);
      if (c == ']') {
        return pos;
      }
      if (!Character.isWhitespace(c) && c != '\n' && c != '\r') {
        // Found non-whitespace before ], something is wrong
        break;
      }
      pos--;
    }

    return -1;
  }

  /**
   * Extracts the init-password value from the malformed part of the response.
   *
   * @param remaining The part after the main array
   * @return The password string, or null if not found
   */
  private String extractInitPassword(String remaining) {
    try {
      remaining = remaining.trim();

      // Look for "init-password": "value"
      int passwordStart = remaining.indexOf("\"init-password\"");
      if (passwordStart == -1) {
        return null;
      }

      // Find the opening quote of the password value
      int valueStart = remaining.indexOf("\"", passwordStart + MALFORMED_KEY_LENGTH);
      // "init-password"
      if (valueStart == -1) {
        return null;
      }

      // Find the closing quote (next quote after valueStart)
      int valueEnd = remaining.indexOf("\"", valueStart + 1);
      if (valueEnd == -1) {
        return null;
      }

      return remaining.substring(valueStart + 1, valueEnd);
    } catch (Exception e) {
      log.warn("Could not extract init-password from response", e);
      return null;
    }
  }
}
