package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ase.stammdatenverwaltung.dto.PersonDetailsDTO;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonService Tests")
class PersonServiceTest {

  // Test constants
  private static final int MAX_AGE_VALIDATION = 150;

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
  @Disabled("TODO: Check why it is failing and implement the test correctly")
  @DisplayName("Should find person by ID when person exists (DTO)")
  void shouldFindPersonByIdWhenPersonExists() {
    // Given
    when(personRepository.findById("test-id")).thenReturn(Optional.of(testPerson));

    // When
    PersonDetailsDTO result = personService.findById("test-id", false);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testPerson.getId());
    assertThat(result.getAddress()).isEqualTo(testPerson.getAddress());
    verify(personRepository).findById("test-id");
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when person not found")
  void shouldThrowWhenPersonNotFound() {
    // Given
    when(personRepository.findById("test-id")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> personService.findById("test-id", false))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Person not found with ID: test-id");
    verify(personRepository).findById("test-id");
  }

  @Test
  @Disabled("TODO: Check why it is failing and implement the test correctly")
  @DisplayName("Should find all persons (DTO list)")
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
    List<PersonDetailsDTO> result = personService.findAll(false, null);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(testPerson.getId());
    assertThat(result.get(1).getId()).isEqualTo(person2.getId());
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

  @Test
  @DisplayName("Should throw exception when creating person with future birth date")
  void shouldThrowExceptionWhenCreatingPersonWithFutureBirthDate() {
    // Given
    Person invalidPerson =
        Person.builder()
            .dateOfBirth(LocalDate.now().plusDays(1))
            .address("Test Address")
            .phoneNumber("+49 123 456789")
            .build();

    // When & Then
    assertThatThrownBy(() -> personService.create(invalidPerson))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Date of birth cannot be in the future");
  }

  @Test
  @DisplayName("Should throw exception when creating person with birth date too far in past")
  void shouldThrowExceptionWhenCreatingPersonWithBirthDateTooFarInPast() {
    // Given
    Person invalidPerson =
        Person.builder()
            .dateOfBirth(LocalDate.now().minusYears(MAX_AGE_VALIDATION).minusDays(1))
            .address("Test Address")
            .phoneNumber("+49 123 456789")
            .build();

    // When & Then
    assertThatThrownBy(() -> personService.create(invalidPerson))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Date of birth cannot be more than 150 years ago");
  }

  @Test
  @DisplayName("Should update person successfully")
  void shouldUpdatePersonSuccessfully() {
    // Given
    Person updatedData =
        Person.builder()
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .address("Updated Address 999")
            .phoneNumber("+49 999 888777")
            .photoUrl("http://example.com/new-photo.jpg")
            .build();

    Person savedPerson =
        Person.builder()
            .id("1")
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .address("Updated Address 999")
            .phoneNumber("+49 999 888777")
            .photoUrl("http://example.com/new-photo.jpg")
            .build();

    when(personRepository.findById("1")).thenReturn(Optional.of(testPerson));
    when(personRepository.save(any(Person.class))).thenReturn(savedPerson);

    // When
    Person result = personService.update("1", updatedData);

    // Then
    assertThat(result.getAddress()).isEqualTo("Updated Address 999");
    assertThat(result.getPhoneNumber()).isEqualTo("+49 999 888777");
    assertThat(result.getPhotoUrl()).isEqualTo("http://example.com/new-photo.jpg");
    verify(personRepository).findById("1");
    verify(personRepository).save(any(Person.class));
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when updating non-existent person")
  void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistentPerson() {
    // Given
    Person updatedData = Person.builder().address("Updated Address").build();
    when(personRepository.findById("1")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> personService.update("1", updatedData))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Person not found with ID: 1");
  }

  @Test
  @DisplayName("Should delete person by ID when person exists")
  void shouldDeletePersonByIdWhenPersonExists() {
    // Given
    when(personRepository.existsById("1")).thenReturn(true);

    // When
    personService.deleteById("1");

    // Then
    verify(personRepository).existsById("1");
    verify(personRepository).deleteById("1");
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when deleting non-existent person")
  void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistentPerson() {
    // Given
    when(personRepository.existsById("1")).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> personService.deleteById("1"))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Person not found with ID: 1");
  }
}
