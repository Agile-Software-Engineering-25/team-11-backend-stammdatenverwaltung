package com.ase.stammdatenverwaltung.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.ase.stammdatenverwaltung.dto.GroupDTO;
import com.ase.stammdatenverwaltung.dto.GroupResponseDTO;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

  @Mock private StudentRepository studentRepository;

  @InjectMocks private GroupService groupService;

  @Test
  void getAllGroupsShouldReturnGroupResponseDTO() {
    // Given
    Student student1 = Student.builder().cohort("BIN-T23 F3").build();
    Student student2 = Student.builder().cohort("BIN-T24 F1").build();
    List<Student> students = Arrays.asList(student1, student2);
    when(studentRepository.findAll()).thenReturn(students);

    // When
    GroupResponseDTO result = groupService.getAllGroups(false, false);

    // Then
    assertEquals(2, result.getGroupCount());
    assertEquals(2, result.getGroups().size());
  }

  @Test
  void getGroupByNameShouldReturnGroupDTO() {
    // Given
    String groupName = "BIN-T23 F3";
    Student student1 = Student.builder().cohort(groupName).build();
    List<Student> students = Collections.singletonList(student1);
    when(studentRepository.findByCohort(groupName)).thenReturn(students);

    // When
    GroupDTO result = groupService.getGroupByName(groupName, false, false);

    // Then
    assertEquals(groupName, result.getName());
    assertEquals(1, result.getStudentCount());
    assertNull(result.getStudents());
  }

  @Test
  void getGroupByNameShouldReturnNullForNonExistentGroup() {
    // Given
    String groupName = "NonExistentGroup";
    when(studentRepository.findByCohort(groupName)).thenReturn(Collections.emptyList());

    // When
    GroupDTO result = groupService.getGroupByName(groupName, false, false);

    // Then
    assertNull(result);
  }
}
