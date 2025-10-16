package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.dto.GroupDTO;
import com.ase.stammdatenverwaltung.dto.GroupResponseDTO;
import com.ase.stammdatenverwaltung.dto.StudentDTO;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service for handling student group-related business logic. */
@Service
@RequiredArgsConstructor
public class GroupService {

  private final StudentRepository studentRepository;

  public GroupResponseDTO getAllGroups() {
    List<Student> students = studentRepository.findAll();
    List<GroupDTO> groups =
        students.stream().collect(Collectors.groupingBy(Student::getCohort)).entrySet().stream()
            .map(
                entry -> {
                  List<StudentDTO> studentDTOs =
                      entry.getValue().stream()
                          .map(this::toStudentDTO)
                          .collect(Collectors.toList());
                  return new GroupDTO(entry.getKey(), studentDTOs.size(), studentDTOs);
                })
            .collect(Collectors.toList());
    return new GroupResponseDTO(groups.size(), groups);
  }

  public GroupDTO getGroupByName(String groupName) {
    List<Student> students = studentRepository.findByCohort(groupName);
    if (students.isEmpty()) {
      return null;
    }
    List<StudentDTO> studentDTOs =
        students.stream().map(this::toStudentDTO).collect(Collectors.toList());
    return new GroupDTO(groupName, studentDTOs.size(), studentDTOs);
  }

  private StudentDTO toStudentDTO(Student student) {
    return new StudentDTO(
        "PlaceholderFirstName",
        "PlaceholderLastName",
        student.getMatriculationNumber(),
        student.getDegreeProgram(),
        student.getSemester(),
        student.getStudyStatus(),
        student.getCohort(),
        student.getAddress(),
        student.getPhoneNumber(),
        student.getDateOfBirth());
  }
}
