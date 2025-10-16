package com.ase.stammdatenverwaltung.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ase.stammdatenverwaltung.config.JwtConfigurationValidator;
import com.ase.stammdatenverwaltung.config.JwtSecurityProperties;
import com.ase.stammdatenverwaltung.config.KeycloakJwtAuthenticationConverter;
import com.ase.stammdatenverwaltung.dto.GroupDTO;
import com.ase.stammdatenverwaltung.dto.GroupResponseDTO;
import com.ase.stammdatenverwaltung.dto.StudentDTO;
import com.ase.stammdatenverwaltung.services.GroupService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GroupService groupService;

  @MockBean private JwtSecurityProperties jwtSecurityProperties;

  @MockBean private KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

  @MockBean private JwtConfigurationValidator jwtConfigurationValidator;

  @Test
  @WithMockUser
  void getAllGroupsShouldReturnListOfGroups() throws Exception {
    // Given
    StudentDTO student1 = new StudentDTO();
    GroupDTO group1 = new GroupDTO("BIN-T23 F3", 1, Collections.singletonList(student1));
    GroupResponseDTO response = new GroupResponseDTO(1, Collections.singletonList(group1));
    when(groupService.getAllGroups()).thenReturn(response);

    // When & Then
    mockMvc
        .perform(get("/api/v1/group"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.group_count").value(1))
        .andExpect(jsonPath("$.groups[0].name").value("BIN-T23 F3"))
        .andExpect(jsonPath("$.groups[0].students_count").value(1))
        .andExpect(jsonPath("$.groups[0].students.size()").value(1));
  }

  @Test
  @WithMockUser
  void getGroupByNameShouldReturnGroup() throws Exception {
    // Given
    String groupName = "BIN-T23 F3";
    StudentDTO student1 = new StudentDTO();
    GroupDTO group = new GroupDTO(groupName, 1, Collections.singletonList(student1));
    when(groupService.getGroupByName(groupName)).thenReturn(group);

    // When & Then
    mockMvc
        .perform(get("/api/v1/group/{groupName}", groupName))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(groupName))
        .andExpect(jsonPath("$.students_count").value(1))
        .andExpect(jsonPath("$.students.size()").value(1));
  }

  @Test
  @WithMockUser
  void getGroupByNameShouldReturnNotFound() throws Exception {
    // Given
    String groupName = "NonExistentGroup";
    when(groupService.getGroupByName(groupName)).thenReturn(null);

    // When & Then
    mockMvc.perform(get("/api/v1/group/{groupName}", groupName)).andExpect(status().isOk());
  }
}
