package com.ase.stammdatenverwaltung.mapper;

import com.ase.stammdatenverwaltung.dto.EmployeeDetailsDTO;
import com.ase.stammdatenverwaltung.dto.LecturerDetailsDTO;
import com.ase.stammdatenverwaltung.dto.PersonDetailsDTO;
import com.ase.stammdatenverwaltung.dto.StudentDetailsDTO;
import com.ase.stammdatenverwaltung.entities.Employee;
import com.ase.stammdatenverwaltung.entities.Lecturer;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.entities.Student;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component mapper to convert Person entities into the appropriate PersonDetailsDTO subtype.
 * Centralizing this logic avoids duplicated instanceof chains across services and allows dependency
 * injection if tests or extensions need a different implementation.
 */
@Component
@NoArgsConstructor
public class PersonDtoMapper {

  /**
   * Returns a PersonDetailsDTO for the given person, using a subtype-specific DTO when applicable.
   *
   * @param person the person to map
   * @return a details DTO corresponding to the person's runtime type
   */
  public PersonDetailsDTO map(Person person) {
    if (person instanceof Lecturer) {
      return new LecturerDetailsDTO((Lecturer) person);
    } else if (person instanceof Employee) {
      return new EmployeeDetailsDTO((Employee) person);
    } else if (person instanceof Student) {
      return new StudentDetailsDTO((Student) person);
    } else {
      return new PersonDetailsDTO(person);
    }
  }
}
