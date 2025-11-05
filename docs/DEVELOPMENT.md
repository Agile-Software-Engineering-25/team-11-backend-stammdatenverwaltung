# Development Guide

This guide covers the development workflow, code standards, and best practices for contributing to the Stammdatenverwaltung project.

---

## 🏗️ Project Structure

```
src/main/
├── java/com/ase/stammdatenverwaltung/
│   ├── controllers/          # REST API endpoints
│   ├── services/             # Business logic layer
│   ├── repositories/         # Data access layer
│   ├── entities/             # JPA domain models
│   ├── dto/                  # Data transfer objects
│   ├── mapper/               # Entity-to-DTO mappers
│   ├── config/               # Spring configuration
│   ├── security/             # Authentication/authorization
│   ├── clients/              # External API clients
│   ├── components/           # Reusable components
│   ├── constants/            # Application constants
│   ├── model/                # Domain models
│   └── StammdatenverwaltungApplication.java  # Entry point
└── resources/
    ├── db/migration/         # Flyway migrations
    ├── application.yaml      # Default config
    ├── application-dev.yaml  # Development config
    ├── application-prod.yaml # Production config
    └── application-test.yaml # Test config

src/test/
└── java/com/ase/stammdatenverwaltung/
    ├── controllers/          # Controller tests
    ├── services/             # Service tests
    └── integration/          # Integration tests
```

---

## 🔄 Development Workflow

### 1. Setup Your Environment

```bash
# Clone repository
git clone https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung.git
cd team-11-backend-stammdatenverwaltung

# Verify Java 21
java -version
# Output: java version "21.x.x"

# Build project
./mvnw clean package

# Run tests
./mvnw test
```

### 2. Create a Feature Branch

```bash
# Create feature branch from main
git checkout main
git pull origin main
git checkout -b feature/your-feature-name

# Or use conventional format:
# feature/add-user-pagination
# bugfix/fix-jwt-validation
# refactor/simplify-group-service
```

### 3. Implement Your Feature

Follow these steps:

1. **Analyze existing code** - Review similar implementations
2. **Write tests first** (optional but recommended)
3. **Implement the feature**
4. **Format code** - `./format-code.sh`
5. **Run all tests** - `./mvnw test`
6. **Validate code quality** - `./mvnw checkstyle:check`
7. **Build** - `./mvnw clean package`

### 4. Commit & Push

```bash
# Stage changes
git add .

# Commit with descriptive message
git commit -m "feat: add pagination to user list endpoint"

# Push to origin
git push origin feature/your-feature-name
```

### 5. Create Pull Request

- Push your branch to GitHub
- Click "New Pull Request"
- Fill in description with:
  - What changed
  - Why it changed
  - How to test it
- Request review from team members

### 6. Code Review & Merge

- Address review comments
- Update code based on feedback
- Merge once approved
- Delete feature branch

---

## 💻 Adding a New Feature

### Example: Adding a New Endpoint

#### Step 1: Define the DTO

```java
// src/main/java/com/ase/stammdatenverwaltung/dto/UserFilterDTO.java

package com.ase.stammdatenverwaltung.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserFilterDTO {
    @NotBlank(message = "Department cannot be blank")
    private String department;

    private String role; // Optional filter
}
```

#### Step 2: Add Service Method

```java
// In PersonService.java

@Service
public class PersonService {
    private final PersonRepository personRepository;

    public List<PersonDetailsDTO> findByDepartment(String department) {
        List<Person> persons = personRepository.findByDepartment(department);
        return persons.stream()
            .map(this::convertToDto)
            .toList();
    }

    private PersonDetailsDTO convertToDto(Person person) {
        // Mapping logic
    }
}
```

#### Step 3: Add Repository Query

```java
// In PersonRepository.java

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByDepartment(String department);
}
```

#### Step 4: Add Controller Endpoint

```java
// In UserController.java

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('Area-3.Team-11.Read.User')")
    @Operation(summary = "Get users by department")
    public List<PersonDetailsDTO> getUsersByDepartment(
        @PathVariable String department) {
        return personService.findByDepartment(department);
    }
}
```

#### Step 5: Write Tests

```java
// In UserControllerTest.java

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Test
    void testGetUsersByDepartment() {
        // Arrange
        String department = "Engineering";
        // ... setup mocks

        // Act
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mockMvc.perform(get("/api/v1/users/department/{department}", department))

        // Assert
            .andExpect(status().isOk());
    }
}
```

---

## 📋 Code Standards

### Naming Conventions

```java
// ✅ GOOD - Clear and descriptive

// Classes
public class PersonService { }
public class CreateStudentRequest { }

// Methods
public List<Person> findActiveEmployees() { }
private void validateUserEmail(String email) { }

// Variables
private final PersonRepository personRepository;
LocalDateTime lastLoginTime = LocalDateTime.now();

// Constants
private static final int MAX_RETRY_ATTEMPTS = 3;
private static final String DEFAULT_TIMEZONE = "UTC";
```

### Class Organization

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    // 1. Dependencies (final fields)
    private final UserService userService;
    private final UserMapper userMapper;

    // 2. Constructor (implicit with @RequiredArgsConstructor)

    // 3. Public methods (API endpoints)
    @GetMapping
    public List<UserDTO> getAll() { }

    // 4. Other public methods

    // 5. Private helper methods
    private void validateInput(CreateUserRequest request) { }
}
```

### Documentation Comments

```java
// ✅ GOOD - Explain WHY, not WHAT

/**
 * Retrieves active users from the database.
 *
 * Caches results for 5 minutes because external system calls
 * are expensive and data is refreshed daily at midnight.
 *
 * @param department filter by department
 * @return list of active users in the department
 * @throws EntityNotFoundException if department doesn't exist
 */
public List<Person> getActiveUsersByDepartment(String department) { }

// ❌ POOR - Obvious documentation

/**
 * Gets users
 *
 * @param department the department
 * @return a list
 */
public List<Person> getActiveUsersByDepartment(String department) { }
```

### Error Handling

```java
// ✅ GOOD - Specific exceptions with context

if (user == null) {
    throw new EntityNotFoundException(
        "User with ID " + id + " not found in database");
}

try {
    userRepository.save(user);
} catch (DataIntegrityViolationException e) {
    LOG.error("Failed to save user: email='{}' already exists",
        user.getEmail(), e);
    throw new DuplicateEmailException(user.getEmail());
}

// ❌ POOR - Generic errors

if (user == null) {
    throw new Exception("Not found");
}

try {
    userRepository.save(user);
} catch (Exception e) {
    LOG.error("Error", e);
}
```

---

## 🧪 Testing Guide

### Test Structure

```java
@WebMvcTest(UserController.class)  // Only loads UserController
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Setup test fixtures
    }

    @Test
    void testGetAllUsers() {
        // Arrange: setup test data
        List<User> users = Arrays.asList(new User(1L, "John"));
        when(userService.findAll()).thenReturn(users);

        // Act: perform the action
        ResultActions result = mockMvc.perform(get("/api/v1/users"));

        // Assert: verify results
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$[0].name").value("John"));
    }
}
```

### Test Naming

```java
// Pattern: testMethodName_WhenCondition_ThenExpectedResult

@Test
void testCreateUser_WhenValidRequest_ThenReturns201() { }

@Test
void testGetUser_WhenUserNotFound_ThenThrowsException() { }

@Test
void testUpdateUser_WhenUnauthorized_ThenReturns403() { }
```

### Run Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=UserControllerTest

# Specific test method
./mvnw test -Dtest=UserControllerTest#testGetAllUsers

# With coverage report
./mvnw test jacoco:report
```

---

## 🔍 Code Quality Checks

### Code Formatting

```bash
# Format all code (Google Java Format)
./format-code.sh

# Format only (without building)
./format-only.sh
```

### Checkstyle Validation

```bash
# Check style compliance
./mvnw checkstyle:check

# View detailed report
cat target/checkstyle-result.xml
```

### Build Verification

```bash
# Full build with tests
./mvnw clean package

# Skip tests (faster, but not recommended)
./mvnw clean package -DskipTests
```

### Code Review Checklist

Before submitting a pull request, ensure:

- [ ] Code is formatted: `./format-code.sh`
- [ ] All tests pass: `./mvnw test`
- [ ] No checkstyle violations: `./mvnw checkstyle:check`
- [ ] Build succeeds: `./mvnw clean package`
- [ ] CLEAN principles followed:
  - [ ] Single Responsibility
  - [ ] Low Coupling
  - [ ] Encapsulation
  - [ ] Abstraction
  - [ ] Clear Naming
- [ ] Tests added for new functionality
- [ ] Documentation updated if needed
- [ ] Commit messages are descriptive
- [ ] No debug code or commented-out code

---

## 🐛 Debugging

### Enable Debug Logging

```yaml
# application-dev.yaml
logging:
  level:
    com.ase.stammdatenverwaltung: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
```

### Debug with IDE

1. Set breakpoints in your code
2. Run in debug mode: `./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug"`
3. Attach debugger to localhost:5005

### Common Issues

**Issue: Tests fail locally but pass in CI**

- Ensure you're using the correct database profile
- Check environment variables are set
- Run: `./mvnw clean test`

**Issue: Code format issues in checkstyle**

- Run: `./format-code.sh`
- Review the generated diff

---

## 📚 Useful Resources

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

---

## ❓ Frequently Asked Questions

**Q: How do I add a new database migration?**
A: Create a new SQL file in `src/main/resources/db/migration/` following Flyway naming convention: `V###__description.sql`

**Q: How do I run only integration tests?**
A: Tests in `src/test/java/integration/` are automatically included. Filter with `-Dtest=*Integration`

**Q: Can I disable a checkstyle rule?**
A: Only with justification. Add `@SuppressWarnings` or document in the PR why the rule should be ignored.

**Q: How do I handle breaking changes?**
A: Create a new API version (e.g., `/api/v2/`) to maintain backward compatibility. The old version can be deprecated gradually.
