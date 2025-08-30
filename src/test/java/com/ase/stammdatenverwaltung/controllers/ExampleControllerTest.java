package com.ase.stammdatenverwaltung.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ase.stammdatenverwaltung.dto.CreateExampleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ExampleController.class, 
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class ExampleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldReturnGreeting() throws Exception {
    mockMvc.perform(get("/api/v1/example"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Hello from Spring Boot")));
  }

  @Test
  void shouldReturnExampleData() throws Exception {
    mockMvc.perform(get("/api/v1/example/data"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("example"))
        .andExpect(jsonPath("$.description").value("This is example data"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.version").value("1.0"));
  }

  @Test
  void shouldCreateExampleData() throws Exception {
    var request = CreateExampleRequest.builder()
        .name("test-name")
        .description("test description")
        .build();

    mockMvc.perform(post("/api/v1/example")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("test-name"))
        .andExpect(jsonPath("$.description").value("test description"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.version").value("1.0"));
  }

  @Test
  void shouldRejectInvalidRequest() throws Exception {
    var request = CreateExampleRequest.builder()
        .name("")  // Invalid - empty name
        .description("test description")
        .build();

    mockMvc.perform(post("/api/v1/example")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
