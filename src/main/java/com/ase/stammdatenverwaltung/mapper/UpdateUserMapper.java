package com.ase.stammdatenverwaltung.mapper;

import com.ase.stammdatenverwaltung.dto.UpdateUserRequest;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.entities.Student;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component to apply partial updates from UpdateUserRequest to Person entities. Supports updating
 * all Person subtypes (Student, Employee, Lecturer) by selectively updating only provided fields.
 * Fields that are null in the request remain unchanged in the entity.
 *
 * <p>This maintains cohesion by centralizing update logic and keeping it consistent across all
 * entity types.
 */
@Component
@NoArgsConstructor
public class UpdateUserMapper {

  /**
   * Applies partial updates from UpdateUserRequest to the given person entity. Only non-null fields
   * in the request are applied to the entity. The specific entity type is preserved and
   * type-specific fields are updated appropriately.
   *
   * @param person the person entity to update
   * @param updateRequest the request containing fields to update
   */
  public void applyUpdates(Person person, UpdateUserRequest updateRequest) {
    // Update common Person fields
    updatePersonFields(person, updateRequest);

    // Update type-specific fields
    if (person instanceof Student) {
      updateStudentFields((Student) person, updateRequest);
    } else if (person instanceof Lecturer) {
      updateLecturerFields((Lecturer) person, updateRequest);
    } else if (person instanceof Employee) {
      updateEmployeeFields((Employee) person, updateRequest);
    }
  }

  private void updatePersonFields(Person person, UpdateUserRequest request) {
    if (request.getDateOfBirth() != null) {
      person.setDateOfBirth(request.getDateOfBirth());
    }
    if (request.getAddress() != null) {
      person.setAddress(request.getAddress());
    }
    if (request.getPhoneNumber() != null) {
      person.setPhoneNumber(request.getPhoneNumber());
    }
    if (request.getPhotoUrl() != null) {
      person.setPhotoUrl(request.getPhotoUrl());
    }
  }

  private void updateStudentFields(Student student, UpdateUserRequest request) {
    if (request.getMatriculationNumber() != null) {
      student.setMatriculationNumber(request.getMatriculationNumber());
    }
    if (request.getDegreeProgram() != null) {
      student.setDegreeProgram(request.getDegreeProgram());
    }
    if (request.getSemester() != null) {
      student.setSemester(request.getSemester());
    }
    if (request.getStudyStatus() != null) {
      try {
        student.setStudyStatus(Student.StudyStatus.valueOf(request.getStudyStatus()));
      } catch (IllegalArgumentException ex) {
        // WHY: Provide context for invalid enum values to aid debugging and client error handling
        throw new IllegalArgumentException(
            "Invalid studyStatus value: '"
                + request.getStudyStatus()
                + "'. Allowed values: "
                + java.util.Arrays.toString(Student.StudyStatus.values()),
            ex);
      }
    }
    if (request.getCohort() != null) {
      student.setCohort(request.getCohort());
    }
  }

  private void updateEmployeeFields(Employee employee, UpdateUserRequest request) {
    if (request.getEmployeeNumber() != null) {
      employee.setEmployeeNumber(request.getEmployeeNumber());
    }
    if (request.getDepartment() != null) {
      employee.setDepartment(request.getDepartment());
    }
    if (request.getOfficeNumber() != null) {
      employee.setOfficeNumber(request.getOfficeNumber());
    }
    if (request.getWorkingTimeModel() != null) {
      try {
        employee.setWorkingTimeModel(
            Employee.WorkingTimeModel.valueOf(request.getWorkingTimeModel()));
      } catch (IllegalArgumentException ex) {
        // WHY: Provide context for invalid enum values to aid debugging and client error handling
        throw new IllegalArgumentException(
            "Invalid workingTimeModel value: '"
                + request.getWorkingTimeModel()
                + "'. Allowed values: "
                + java.util.Arrays.toString(Employee.WorkingTimeModel.values()),
            ex);
      }
    }
  }

  private void updateLecturerFields(Lecturer lecturer, UpdateUserRequest request) {
    // First update employee fields (lecturer is a subtype)
    updateEmployeeFields(lecturer, request);

    // Then update lecturer-specific fields
    if (request.getFieldChair() != null) {
      lecturer.setFieldChair(request.getFieldChair());
    }
    if (request.getTitle() != null) {
      lecturer.setTitle(request.getTitle());
    }
    if (request.getEmploymentStatus() != null) {
      try {
        lecturer.setEmploymentStatus(
            Lecturer.EmploymentStatus.valueOf(request.getEmploymentStatus()));
      } catch (IllegalArgumentException ex) {
        // WHY: Provide context for invalid enum values to aid debugging and client error handling
        throw new IllegalArgumentException(
            "Invalid employment status: '"
                + request.getEmploymentStatus()
                + "'. Allowed values: "
                + java.util.Arrays.toString(Lecturer.EmploymentStatus.values()),
            ex);
      }
    }
  }
}
