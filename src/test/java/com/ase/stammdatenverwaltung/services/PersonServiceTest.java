package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ase.stammdatenverwaltung.clients.KeycloakClient;
import com.ase.stammdatenverwaltung.constants.ValidationConstants;
import com.ase.stammdatenverwaltung.dto.PersonDetailsDTO;
import com.ase.stammdatenverwaltung.dto.UpdateUserRequest;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.mapper.PersonDtoMapper;
import com.ase.stammdatenverwaltung.mapper.UpdateUserMapper;
import com.ase.stammdatenverwaltung.repositories.EmployeeRepository;
import com.ase.stammdatenverwaltung.repositories.LecturerRepository;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import com.ase.stammdatenverwaltung.repositories.StudentRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonService Tests")
class PersonServiceTest {

  @Mock private PersonRepository personRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private LecturerRepository lecturerRepository;
  @Mock private EmployeeRepository employeeRepository;
  @Mock private KeycloakClient keycloakClient;
  @Mock private PersonDtoMapper personDtoMapper;

  // Use real UpdateUserMapper because it's a stateless, pure component with no external
  // dependencies. Tests that exercise update logic need its actual behavior to verify correct
  // validation and mapping.
  private final UpdateUserMapper updateUserMapper = new UpdateUserMapper();

  private PersonService personService;

  private Person testPerson;

  @BeforeEach
  void setUp() {
    // Manually construct PersonService with real UpdateUserMapper and mocked dependencies.
    // This provides explicit control while eliminating the fragile factory method.
    personService =
        new PersonService(
            personRepository,
            studentRepository,
            lecturerRepository,
            employeeRepository,
            keycloakClient,
            personDtoMapper,
            updateUserMapper);

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
    PersonDetailsDTO result = personService.findById("test-id", false).block();

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
    List<PersonDetailsDTO> result = personService.findAll(false, null).collectList().block();

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
            .dateOfBirth(LocalDate.now().minusYears(ValidationConstants.MAX_AGE_YEARS).minusDays(1))
            .address("Test Address")
            .phoneNumber("+49 123 456789")
            .build();

    // When & Then
    assertThatThrownBy(() -> personService.create(invalidPerson))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Date of birth cannot be more than "
                + ValidationConstants.MAX_AGE_YEARS
                + " years ago");
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
  @DisplayName("Should update person with partial data successfully")
  void shouldPartiallyUpdatePersonSuccessfully() {
    // Given
    UpdateUserRequest updateRequest =
        UpdateUserRequest.builder().address("New Address").phoneNumber("+49 555 666777").build();

    Person updatedPerson =
        Person.builder()
            .id("1")
            .address("New Address")
            .phoneNumber("+49 555 666777")
            .dateOfBirth(testPerson.getDateOfBirth())
            .build();

    when(personRepository.findById("1")).thenReturn(Optional.of(testPerson));
    when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);

    // When
    Person result = personService.updatePartial("1", updateRequest);

    // Then
    assertThat(result.getAddress()).isEqualTo("New Address");
    assertThat(result.getPhoneNumber()).isEqualTo("+49 555 666777");
    verify(personRepository).findById("1");
    verify(personRepository).save(any(Person.class));
  }

  @Test
  @DisplayName("Should throw exception when partial update contains future birth date")
  void shouldThrowExceptionWhenPartialUpdateContainsFutureBirthDate() {
    // Given
    UpdateUserRequest updateRequest =
        UpdateUserRequest.builder().dateOfBirth(LocalDate.now().plusDays(1)).build();

    when(personRepository.findById("1")).thenReturn(Optional.of(testPerson));

    // When & Then
    assertThatThrownBy(() -> personService.updatePartial("1", updateRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Date of birth cannot be in the future");
  }

  @Test
  @DisplayName("Should throw exception when partial update contains birth date too far in past")
  void shouldThrowExceptionWhenPartialUpdateContainsBirthDateTooFarInPast() {
    // Given
    UpdateUserRequest updateRequest =
        UpdateUserRequest.builder()
            .dateOfBirth(LocalDate.now().minusYears(ValidationConstants.MAX_AGE_YEARS).minusDays(1))
            .build();

    when(personRepository.findById("1")).thenReturn(Optional.of(testPerson));

    // When & Then
    assertThatThrownBy(() -> personService.updatePartial("1", updateRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Date of birth cannot be more than "
                + ValidationConstants.MAX_AGE_YEARS
                + " years ago");
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when partially updating non-existent person")
  void shouldThrowEntityNotFoundExceptionWhenPartiallyUpdatingNonExistentPerson() {
    // Given
    UpdateUserRequest updateRequest = UpdateUserRequest.builder().address("New Address").build();
    when(personRepository.findById("1")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> personService.updatePartial("1", updateRequest))
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
