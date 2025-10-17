package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonService Tests")
class PersonServiceTest {

  @Mock private PersonRepository personRepository;

  @InjectMocks private PersonService personService;

  private Person testPerson;

  @BeforeEach
  void setUp() {
    testPerson =
        Person.builder()
            .id("test-id")
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .address("Test Address 123")
            .phoneNumber("+49 123 456789")
            .photoUrl("http://example.com/photo.jpg")
            .build();
  }

  @Test
  @DisplayName("Should find person by ID when person exists")
  void shouldFindPersonByIdWhenPersonExists() {
    // Given
    when(personRepository.findById("test-id")).thenReturn(Optional.of(testPerson));

    // When
    Optional<Person> result = personService.findById("test-id");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testPerson);
    verify(personRepository).findById("test-id");
  }

  @Test
  @DisplayName("Should return empty optional when person not found by ID")
  void shouldReturnEmptyOptionalWhenPersonNotFoundById() {
    // Given
    when(personRepository.findById("test-id")).thenReturn(Optional.empty());

    // When
    Optional<Person> result = personService.findById("test-id");

    // Then
    assertThat(result).isEmpty();
    verify(personRepository).findById("test-id");
  }

  @Test
  @DisplayName("Should get person by ID when person exists")
  void shouldGetPersonByIdWhenPersonExists() {
    // Given
    when(personRepository.findById("test-id")).thenReturn(Optional.of(testPerson));

    // When
    Person result = personService.getById("test-id");

    // Then
    assertThat(result).isEqualTo(testPerson);
    verify(personRepository).findById("test-id");
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when getting person by non-existent ID")
  void shouldThrowEntityNotFoundExceptionWhenGettingPersonByNonExistentId() {
    // Given
    when(personRepository.findById("test-id")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> personService.getById("test-id"))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Person not found with ID: test-id");
  }

  @Test
  @DisplayName("Should find all persons")
  void shouldFindAllPersons() {
    // Given
    Person person2 =
        Person.builder()
            .id("test-id-2")
            .dateOfBirth(LocalDate.of(1985, 10, 20))
            .address("Another Address 456")
            .phoneNumber("+49 987 654321")
            .build();
    List<Person> persons = Arrays.asList(testPerson, person2);
    when(personRepository.findAll()).thenReturn(persons);

    // When
    List<Person> result = personService.findAll();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(testPerson, person2);
    verify(personRepository).findAll();
  }

  @Test
  @DisplayName("Should create person successfully")
  void shouldCreatePersonSuccessfully() {
    // Given
    Person newPerson =
        Person.builder()
            .id("test-id-3")
            .dateOfBirth(LocalDate.of(1995, 3, 10))
            .address("New Address 789")
            .phoneNumber("+49 555 123456")
            .build();

    when(personRepository.save(newPerson)).thenReturn(newPerson);

    // When
    Person result = personService.create(newPerson);

    // Then
    assertThat(result).isEqualTo(newPerson);
    verify(personRepository).save(newPerson);
  }
}
