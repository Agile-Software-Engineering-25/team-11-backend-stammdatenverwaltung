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
    if (request.getDegreeProgram() != null) {
      student.setDegreeProgram(request.getDegreeProgram());
    }
    if (request.getSemester() != null) {
      student.setSemester(request.getSemester());
    }
    if (request.getStudyStatus() != null) {
      student.setStudyStatus(Student.StudyStatus.valueOf(request.getStudyStatus()));
    }
    if (request.getCohort() != null) {
      student.setCohort(request.getCohort());
    }
  }

  private void updateEmployeeFields(Employee employee, UpdateUserRequest request) {
    if (request.getDepartment() != null) {
      employee.setDepartment(request.getDepartment());
    }
    if (request.getJobTitle() != null) {
      employee.setOfficeNumber(request.getJobTitle());
    }
  }

  private void updateLecturerFields(Lecturer lecturer, UpdateUserRequest request) {
    // First update employee fields (lecturer is a subtype)
    updateEmployeeFields(lecturer, request);

    // Then update lecturer-specific fields
    if (request.getEmploymentStatus() != null) {
      lecturer.setEmploymentStatus(
          Lecturer.EmploymentStatus.valueOf(request.getEmploymentStatus()));
    }
    if (request.getSpecialization() != null) {
      lecturer.setFieldChair(request.getSpecialization());
    }
    if (request.getOfficeLocation() != null) {
      lecturer.setOfficeNumber(request.getOfficeLocation());
    }
  }
}
