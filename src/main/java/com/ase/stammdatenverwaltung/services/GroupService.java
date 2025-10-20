package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.dto.GroupDTO;
import com.ase.stammdatenverwaltung.dto.GroupResponseDTO;
import com.ase.stammdatenverwaltung.dto.KeycloakUser;
import com.ase.stammdatenverwaltung.dto.StudentDTO;
import com.ase.stammdatenverwaltung.entities.Student;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/** Service for handling student group-related business logic. */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

  private final StudentRepository studentRepository;
  private final KeycloakClient keycloakClient;

  public GroupResponseDTO getAllGroups(boolean withDetails) {
    List<Student> students = studentRepository.findAll();
    Map<String, KeycloakUser> keycloakUserMap = fetchKeycloakUsers(students, withDetails);

    List<GroupDTO> groups =
        students.stream().collect(Collectors.groupingBy(Student::getCohort)).entrySet().stream()
            .map(
                entry -> {
                  List<StudentDTO> studentDTOs =
                      entry.getValue().stream()
                          .map(
                              student ->
                                  toStudentDTO(student, keycloakUserMap.get(student.getId())))
                          .collect(Collectors.toList());
                  return new GroupDTO(entry.getKey(), studentDTOs.size(), studentDTOs);
                })
            .collect(Collectors.toList());
    return new GroupResponseDTO(groups.size(), groups);
  }

  public GroupDTO getGroupByName(String groupName, boolean withDetails) {
    List<Student> students = studentRepository.findByCohort(groupName);
    if (students.isEmpty()) {
      return null;
    }

    Map<String, KeycloakUser> keycloakUserMap = fetchKeycloakUsers(students, withDetails);

    List<StudentDTO> studentDTOs =
        students.stream()
            .map(student -> toStudentDTO(student, keycloakUserMap.get(student.getId())))
            .collect(Collectors.toList());
    return new GroupDTO(groupName, studentDTOs.size(), studentDTOs);
  }

  /**
   * Fetches Keycloak user details for a list of students.
   *
   * @param students the list of students
   * @param withDetails whether to fetch details from Keycloak
   * @return a map of user IDs to KeycloakUser objects
   */
  private Map<String, KeycloakUser> fetchKeycloakUsers(
      List<Student> students, boolean withDetails) {
    Map<String, KeycloakUser> keycloakUserMap = new HashMap<>();

    if (!withDetails) {
      return keycloakUserMap;
    }

    List<String> userIds = students.stream().map(Student::getId).collect(Collectors.toList());

    if (userIds.isEmpty()) {
      return keycloakUserMap;
    }

    try {
      List<KeycloakUser> keycloakUsers =
          Flux.fromIterable(userIds)
              .flatMap(keycloakClient::findUserById)
              .flatMapIterable(users -> users)
              .collectList()
              .block();

      if (keycloakUsers != null) {
        keycloakUserMap =
            keycloakUsers.stream()
                .filter(u -> u.getId() != null)
                .collect(
                    Collectors.toMap(KeycloakUser::getId, user -> user, (user1, user2) -> user1));
      }
    } catch (Exception e) {
      log.error("Failed to fetch user details from Keycloak", e);
    }

    return keycloakUserMap;
  }

  /**
   * Converts a Student entity and KeycloakUser to a StudentDTO.
   *
   * @param student the student entity
   * @param keycloakUser the keycloak user (may be null)
   * @return the student DTO
   */
  private StudentDTO toStudentDTO(Student student, KeycloakUser keycloakUser) {
    if (keycloakUser == null) {
      // Return DTO with only student data if Keycloak user is not available
      return new StudentDTO(
          student.getId(),
          null,
          null,
          null,
          null,
          student.getMatriculationNumber(),
          student.getDegreeProgram(),
          student.getSemester(),
          student.getStudyStatus(),
          student.getCohort(),
          student.getAddress(),
          student.getPhoneNumber(),
          student.getDateOfBirth());
    }

    return new StudentDTO(
        keycloakUser.getId(),
        keycloakUser.getFirstName(),
        keycloakUser.getLastName(),
        keycloakUser.getEmail(),
        keycloakUser.getUsername(),
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
