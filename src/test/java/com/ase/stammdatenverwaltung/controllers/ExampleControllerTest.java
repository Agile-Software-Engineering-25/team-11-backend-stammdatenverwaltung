package com.ase.stammdatenverwaltung.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ase.stammdatenverwaltung.dto.ExampleDto;
import com.ase.stammdatenverwaltung.services.ExampleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = ExampleController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class ExampleControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ExampleService exampleService;

  @Test
  void shouldReturnAllExamples() throws Exception {
    // Given
    List<ExampleDto> examples =
        List.of(
            ExampleDto.builder().id(1L).name("Example 1").description("Description 1").build(),
            ExampleDto.builder().id(2L).name("Example 2").description("Description 2").build());
    when(exampleService.getAllExamples()).thenReturn(examples);

    // When & Then
    mockMvc
        .perform(get("/api/v1/examples"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Example 1"))
        .andExpect(jsonPath("$[0].description").value("Description 1"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].name").value("Example 2"))
        .andExpect(jsonPath("$[1].description").value("Description 2"));
  }

  @Test
  void shouldReturnExampleById() throws Exception {
    // Given
    ExampleDto example =
        ExampleDto.builder().id(1L).name("Test Example").description("Test Description").build();
    when(exampleService.getExampleById(1L)).thenReturn(Optional.of(example));

    // When & Then
    mockMvc
        .perform(get("/api/v1/examples/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Test Example"))
        .andExpect(jsonPath("$.description").value("Test Description"));
  }

  @Test
  void shouldReturnNotFoundForNonExistentExample() throws Exception {
    // Given
    when(exampleService.getExampleById(999L)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/v1/examples/999")).andExpect(status().isNotFound());
  }

  @Test
  void shouldCreateExample() throws Exception {
    // Given
    ExampleDto request =
        ExampleDto.builder().name("New Example").description("New Description").build();
    ExampleDto response =
        ExampleDto.builder().id(1L).name("New Example").description("New Description").build();
    when(exampleService.createExample(any(ExampleDto.class))).thenReturn(response);

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("New Example"))
        .andExpect(jsonPath("$.description").value("New Description"));
  }

  @Test
  void shouldRejectInvalidRequest() throws Exception {
    // Given
    ExampleDto request =
        ExampleDto.builder()
            .name("") // Invalid - empty name
            .description("Valid description")
            .build();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
