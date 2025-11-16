package com.ase.stammdatenverwaltung.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ase.stammdatenverwaltung.dto.keycloak.CreateUserRequest;
import com.ase.stammdatenverwaltung.dto.keycloak.CreateUserResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"keycloak.enabled=false"})
@DisplayName("KeycloakClient no-op behavior when disabled")
class KeycloakClientNoopTest {

  @Autowired private KeycloakClient keycloakClient;

  @Test
  @DisplayName("createUser returns synthetic ID when keycloak disabled")
  void createUserReturnsSyntheticId() {
    CreateUserRequest request =
        CreateUserRequest.builder()
            .username("test@example.com")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .groups(List.of("Area-3.Team-11.Read.Student"))
            .build();

    CreateUserResponse response = keycloakClient.createUser(request).block();
    assertNotNull(response);
    assertNotNull(response.getId());
    assertEquals(true, response.getId().startsWith("dev-"));
  }

  @Test
  @DisplayName("findUserById returns empty list when keycloak disabled")
  void findUserByIdEmpty() {
    List<?> users = keycloakClient.findUserById(UUID.randomUUID().toString()).block();
    assertNotNull(users);
    assertEquals(0, users.size());
  }

  @Test
  @DisplayName("findUserByEmail returns empty list when keycloak disabled")
  void findUserByEmailEmpty() {
    List<?> users = keycloakClient.findUserByEmail("doesnotexist@example.com").block();
    assertNotNull(users);
    assertEquals(0, users.size());
  }
}
