package com.ase.stammdatenverwaltung.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI documentation setup. Provides custom OpenAPI configuration and
 * grouped API definitions for the application.
 */
@Configuration
public class OpenApiConfig {

  @Value("${server.port:8080}")
  private String serverPort;

  @Value("${openapi.server.url:http://localhost:8080}")
  private String serverUrl;

  @Value("${openapi.server.description:Development Server}")
  private String serverDescription;

  @Bean
  OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Stammdatenverwaltung API")
                .version("1.0.0")
                .description("ASE Project - User Service für die Verwaltung von Stammdaten")
                .contact(new Contact().name("ASE Team 11")))
        .servers(List.of(new Server().url(serverUrl).description(serverDescription)))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token")));
  }

  @Bean
  GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public-endpoints")
        .displayName("Public API")
        .pathsToMatch("/api/v1/public/**")
        .build();
  }

  @Bean
  GroupedOpenApi authenticatedApi() {
    return GroupedOpenApi.builder()
        .group("authenticated-endpoints")
        .displayName("Authenticated API")
        .pathsToMatch("/api/v1/**")
        .pathsToExclude("/api/v1/public/**")
        .addOpenApiCustomizer(
            openApi -> openApi.addSecurityItem(new SecurityRequirement().addList("bearerAuth")))
        .build();
  }

  @Bean
  GroupedOpenApi actuatorApi() {
    return GroupedOpenApi.builder()
        .group("actuator-endpoints")
        .displayName("Actuator Endpoints")
        .pathsToMatch("/actuator/**")
        .build();
  }

  @Bean
  GroupedOpenApi allApi() {
    return GroupedOpenApi.builder()
        .group("all-endpoints")
        .displayName("All Endpoints")
        .pathsToMatch("/api/**", "/actuator/**")
        .build();
  }
}
