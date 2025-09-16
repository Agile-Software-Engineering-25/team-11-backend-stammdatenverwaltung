package com.ase.stammdatenverwaltung.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

  // Test constants
  private static final int MAX_AGE_VALIDATION = 151;

  @Mock private PersonRepository personRepository;

  @InjectMocks private PersonService personService;

  private Person testPerson;

  @BeforeEach
  void setUp() {
    testPerson =
        Person.builder()
            .id(1L)
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
    when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));

    // When
    Optional<Person> result = personService.findById(1L);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testPerson);
    verify(personRepository).findById(1L);
  }

  @Test
  @DisplayName("Should return empty optional when person not found by ID")
  void shouldReturnEmptyOptionalWhenPersonNotFoundById() {
    // Given
    when(personRepository.findById(1L)).thenReturn(Optional.empty());

    // When
    Optional<Person> result = personService.findById(1L);

    // Then
    assertThat(result).isEmpty();
    verify(personRepository).findById(1L);
  }

  @Test
  @DisplayName("Should get person by ID when person exists")
  void shouldGetPersonByIdWhenPersonExists() {
    // Given
    when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));

    // When
    Person result = personService.getById(1L);

    // Then
    assertThat(result).isEqualTo(testPerson);
    verify(personRepository).findById(1L);
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when getting person by non-existent ID")
  void shouldThrowEntityNotFoundExceptionWhenGettingPersonByNonExistentId() {
    // Given
    when(personRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> personService.getById(1L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Person not found with ID: 1");
  }

  @Test
  @DisplayName("Should find all persons")
  void shouldFindAllPersons() {
    // Given
    Person person2 =
        Person.builder()
            .id(2L)
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
            .dateOfBirth(LocalDate.of(1995, 3, 10))
            .address("New Address 789")
            .phoneNumber("+49 555 123456")
            .build();

    Person savedPerson =
        Person.builder()
            .id(3L)
            .dateOfBirth(LocalDate.of(1995, 3, 10))
            .address("New Address 789")
            .phoneNumber("+49 555 123456")
            .build();

    when(personRepository.save(newPerson)).thenReturn(savedPerson);

    // When
    Person result = personService.create(newPerson);

    // Then
    assertThat(result).isEqualTo(savedPerson);
    assertThat(result.getId()).isEqualTo(3L);
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
            .dateOfBirth(LocalDate.now().minusYears(MAX_AGE_VALIDATION))
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
            .id(1L)
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .address("Updated Address 999")
            .phoneNumber("+49 999 888777")
            .photoUrl("http://example.com/new-photo.jpg")
            .build();

    when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
    when(personRepository.save(any(Person.class))).thenReturn(savedPerson);

    // When
    Person result = personService.update(1L, updatedData);

    // Then
    assertThat(result.getAddress()).isEqualTo("Updated Address 999");
    assertThat(result.getPhoneNumber()).isEqualTo("+49 999 888777");
    assertThat(result.getPhotoUrl()).isEqualTo("http://example.com/new-photo.jpg");
    verify(personRepository).findById(1L);
    verify(personRepository).save(any(Person.class));
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when updating non-existent person")
  void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistentPerson() {
    // Given
    Person updatedData = Person.builder().address("Updated Address").build();
    when(personRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> personService.update(1L, updatedData))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Person not found with ID: 1");
  }

  @Test
  @DisplayName("Should delete person by ID when person exists")
  void shouldDeletePersonByIdWhenPersonExists() {
    // Given
    when(personRepository.existsById(1L)).thenReturn(true);

    // When
    personService.deleteById(1L);

    // Then
    verify(personRepository).existsById(1L);
    verify(personRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when deleting non-existent person")
  void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistentPerson() {
    // Given
    when(personRepository.existsById(1L)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> personService.deleteById(1L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Person not found with ID: 1");
  }

  @Test
  @DisplayName("Should find persons by phone number")
  void shouldFindPersonsByPhoneNumber() {
    // Given
    List<Person> persons = Arrays.asList(testPerson);
    when(personRepository.findByPhoneNumber("+49 123 456789")).thenReturn(persons);

    // When
    List<Person> result = personService.findByPhoneNumber("+49 123 456789");

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testPerson);
    verify(personRepository).findByPhoneNumber("+49 123 456789");
  }

  @Test
  @DisplayName("Should find persons by address containing text")
  void shouldFindPersonsByAddressContainingText() {
    // Given
    List<Person> persons = Arrays.asList(testPerson);
    when(personRepository.findByAddressContainingIgnoreCase("Test")).thenReturn(persons);

    // When
    List<Person> result = personService.findByAddressContaining("Test");

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testPerson);
    verify(personRepository).findByAddressContainingIgnoreCase("Test");
  }

  @Test
  @DisplayName("Should find persons by date of birth between dates")
  void shouldFindPersonsByDateOfBirthBetweenDates() {
    // Given
    LocalDate startDate = LocalDate.of(1989, 1, 1);
    LocalDate endDate = LocalDate.of(1991, 12, 31);
    List<Person> persons = Arrays.asList(testPerson);
    when(personRepository.findByDateOfBirthBetween(startDate, endDate)).thenReturn(persons);

    // When
    List<Person> result = personService.findByDateOfBirthBetween(startDate, endDate);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testPerson);
    verify(personRepository).findByDateOfBirthBetween(startDate, endDate);
  }

  @Test
  @DisplayName("Should find oldest person")
  void shouldFindOldestPerson() {
    // Given
    when(personRepository.findOldestPerson()).thenReturn(Optional.of(testPerson));

    // When
    Optional<Person> result = personService.findOldestPerson();

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testPerson);
    verify(personRepository).findOldestPerson();
  }

  @Test
  @DisplayName("Should find youngest person")
  void shouldFindYoungestPerson() {
    // Given
    when(personRepository.findYoungestPerson()).thenReturn(Optional.of(testPerson));

    // When
    Optional<Person> result = personService.findYoungestPerson();

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testPerson);
    verify(personRepository).findYoungestPerson();
  }

  @Test
  @DisplayName("Should find persons by age range successfully")
  void shouldFindPersonsByAgeRangeSuccessfully() {
    // Given
    int minAge = 25;
    int maxAge = 40;
    List<Person> persons = Arrays.asList(testPerson);
    when(personRepository.findByAgeRange(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(persons);

    // When
    List<Person> result = personService.findByAgeRange(minAge, maxAge);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testPerson);
    verify(personRepository).findByAgeRange(any(LocalDate.class), any(LocalDate.class));
  }

  @Test
  @DisplayName("Should throw exception when minAge is negative")
  void shouldThrowExceptionWhenMinAgeIsNegative() {
    // When & Then
    assertThatThrownBy(() -> personService.findByAgeRange(-1, 40))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Minimum age cannot be negative");
  }

  @Test
  @DisplayName("Should throw exception when maxAge is negative")
  void shouldThrowExceptionWhenMaxAgeIsNegative() {
    // When & Then
    assertThatThrownBy(() -> personService.findByAgeRange(25, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Maximum age cannot be negative");
  }

  @Test
  @DisplayName("Should throw exception when minAge is greater than maxAge")
  void shouldThrowExceptionWhenMinAgeIsGreaterThanMaxAge() {
    // When & Then
    assertThatThrownBy(() -> personService.findByAgeRange(50, 25))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Minimum age cannot be greater than maximum age");
  }

  @Test
  @DisplayName("Should throw exception when maxAge exceeds maximum allowed")
  void shouldThrowExceptionWhenMaxAgeExceedsMaximumAllowed() {
    // When & Then
    assertThatThrownBy(() -> personService.findByAgeRange(25, 151))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Maximum age cannot exceed 150 years");
  }

  @Test
  @DisplayName("Should count persons by age range successfully")
  void shouldCountPersonsByAgeRangeSuccessfully() {
    // Given
    int minAge = 25;
    int maxAge = 40;
    long expectedCount = 5L;
    when(personRepository.countByAgeRange(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(expectedCount);

    // When
    long result = personService.countByAgeRange(minAge, maxAge);

    // Then
    assertThat(result).isEqualTo(expectedCount);
    verify(personRepository).countByAgeRange(any(LocalDate.class), any(LocalDate.class));
  }

  @Test
  @DisplayName("Should validate age range for count method")
  void shouldValidateAgeRangeForCountMethod() {
    // When & Then
    assertThatThrownBy(() -> personService.countByAgeRange(-1, 40))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Minimum age cannot be negative");
  }

  @Test
  @DisplayName("Should find persons by exact age successfully")
  void shouldFindPersonsByExactAgeSuccessfully() {
    // Given
    int age = 30;
    List<Person> persons = Arrays.asList(testPerson);
    when(personRepository.findByAge(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(persons);

    // When
    List<Person> result = personService.findByAge(age);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testPerson);
    verify(personRepository).findByAge(any(LocalDate.class), any(LocalDate.class));
  }

  @Test
  @DisplayName("Should throw exception when age is negative")
  void shouldThrowExceptionWhenAgeIsNegative() {
    // When & Then
    assertThatThrownBy(() -> personService.findByAge(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Age cannot be negative");
  }

  @Test
  @DisplayName("Should throw exception when age exceeds maximum allowed")
  void shouldThrowExceptionWhenAgeExceedsMaximumAllowed() {
    // When & Then
    assertThatThrownBy(() -> personService.findByAge(151))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Age cannot exceed 150 years");
  }

  @Test
  @DisplayName("Should accept valid age range with equal min and max")
  void shouldAcceptValidAgeRangeWithEqualMinAndMax() {
    // Given
    int age = 30;
    List<Person> persons = Arrays.asList(testPerson);
    when(personRepository.findByAgeRange(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(persons);

    // When
    List<Person> result = personService.findByAgeRange(age, age);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testPerson);
    verify(personRepository).findByAgeRange(any(LocalDate.class), any(LocalDate.class));
  }

  @Test
  @DisplayName("Should accept age range at boundaries")
  void shouldAcceptAgeRangeAtBoundaries() {
    // Given
    int minAge = 0;
    int maxAge = 150;
    List<Person> persons = Arrays.asList();
    when(personRepository.findByAgeRange(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(persons);

    // When
    List<Person> result = personService.findByAgeRange(minAge, maxAge);

    // Then
    assertThat(result).isEmpty();
    verify(personRepository).findByAgeRange(any(LocalDate.class), any(LocalDate.class));
  }

  // Note: Age-based counting test removed due to removal of countByAgeRange method

  @Test
  @DisplayName("Should check if person exists by ID")
  void shouldCheckIfPersonExistsById() {
    // Given
    when(personRepository.existsById(1L)).thenReturn(true);

    // When
    boolean result = personService.existsById(1L);

    // Then
    assertThat(result).isTrue();
    verify(personRepository).existsById(1L);
  }
}
