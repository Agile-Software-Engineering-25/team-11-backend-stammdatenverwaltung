# Contributing Guidelines

Thank you for contributing to the Stammdatenverwaltung project! This guide explains how to contribute code, report issues, and participate in the development process.

---

## 🤝 Code of Conduct

We are committed to providing a welcoming and inclusive environment. Please:

- Be respectful and constructive in all interactions
- Help others learn and grow
- Report misconduct to the team lead
- Celebrate diverse perspectives and experiences

---

## 🚀 Getting Started

### 1. Setup Your Environment

```bash
# Clone repository
git clone https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung.git
cd team-11-backend-stammdatenverwaltung

# Create feature branch
git checkout -b feature/your-feature-name

# Build and test
./mvnw clean package
./mvnw test
```

### 2. Understand Project Structure

- Read: [README.md](../README.md)
- Review: [ARCHITECTURE.md](ARCHITECTURE.md)
- Study: [DEVELOPMENT.md](DEVELOPMENT.md)

### 3. Find Something to Work On

- Check [GitHub Issues](https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung/issues)
- Look for issues labeled `good first issue` or `help wanted`
- Discuss with team before starting major changes
- Comment on issue to claim it

---

## 📋 Development Process

### Branch Naming Convention

Use descriptive branch names following this pattern:

```
{type}/{description}

Types:
  feature/    - New feature
  bugfix/     - Bug fix
  refactor/   - Code refactoring
  docs/       - Documentation only
  test/       - Test improvements
  chore/      - Maintenance tasks
  hotfix/     - Emergency production fix

Examples:
  feature/add-user-pagination
  bugfix/fix-group-member-query
  refactor/simplify-person-service
  docs/update-api-documentation
  test/add-integration-tests
```

### Commit Message Format

Follow conventional commits:

```
{type}({scope}): {subject}

{body}

{footer}

Types:
  feat     - New feature
  fix      - Bug fix
  docs     - Documentation
  style    - Formatting (code style, not CSS)
  refactor - Code refactoring
  test     - Adding or updating tests
  chore    - Build, dependencies, maintenance

Scope: Component affected (users, groups, auth, etc.)

Subject:
  - Imperative mood ("add" not "adds" or "added")
  - No capitalization
  - No period at end
  - Max 50 characters

Body (optional):
  - Explain WHAT and WHY, not HOW
  - Wrap at 72 characters
  - Separate from subject with blank line

Footer (optional):
  - Reference issues: "Fixes #123"
  - Breaking changes: "BREAKING CHANGE: description"

Examples:

feat(users): add pagination to user list endpoint

Add support for page and size query parameters to GET /api/v1/users.
Allows clients to retrieve large result sets efficiently.

Fixes #45
Depends on #42

fix(auth): resolve JWT token expiration issue

Previous implementation allowed expired tokens if Keycloak was unreachable.
Now properly validates token expiration with fallback to reject.

Fixes #89

docs(api): update endpoint examples for authentication
```

### Pull Request Process

1. **Create Pull Request**

   ```bash
   git push origin feature/your-feature-name
   # Click "New Pull Request" on GitHub
   ```

2. **Fill PR Template**

   - Title: Clear and descriptive
   - Description: What changed and why
   - Testing: How to verify changes
   - Screenshots: If UI changes
   - Checklist: Format, tests, docs

3. **PR Description Template**

   ```markdown
   ## Description

   Brief overview of changes

   ## Type of Change

   - [ ] New feature
   - [ ] Bug fix
   - [ ] Breaking change
   - [ ] Documentation update

   ## Related Issue

   Fixes #123

   ## How to Test

   Steps to verify:

   1. ...
   2. ...

   ## Testing

   - [ ] Unit tests added/updated
   - [ ] Integration tests passed
   - [ ] Manual testing done

   ## Checklist

   - [ ] Code formatted: `./format-code.sh`
   - [ ] Tests pass: `./mvnw test`
   - [ ] No checkstyle violations: `./mvnw checkstyle:check`
   - [ ] Build succeeds: `./mvnw clean package`
   - [ ] Documentation updated
   - [ ] No breaking changes (or clearly noted)
   - [ ] CLEAN principles followed
   ```

4. **Code Review**

   - Address all comments
   - Request re-review after changes
   - Ask for clarification if needed
   - Be open to feedback

5. **Merge**
   - Squash commits if requested
   - Delete feature branch after merge
   - Verify main branch build succeeds

---

## 💻 Coding Standards

### CLEAN Code Principles

**Required for all contributions:**

1. **Cohesion** - Related functionality together

   ```java
   // ✅ GOOD - Cohesive group operations
   @Service
   public class GroupService {
       // All group-related methods together
       public List<Group> getAll() { }
       public Group getByName(String name) { }
       public void addMember(Group group, Person person) { }
   }

   // ❌ POOR - Mixed concerns
   @Service
   public class UserUtility {
       public User getUser() { }
       public String formatEmail() { }
       public void saveGroup() { }  // Wrong place!
   }
   ```

2. **Low Coupling** - Minimize dependencies

   ```java
   // ✅ GOOD - Injected dependency
   @Service
   public class UserService {
       private final UserRepository repository;

       public UserService(UserRepository repository) {
           this.repository = repository;
       }
   }

   // ❌ POOR - Hard dependency
   @Service
   public class UserService {
       private UserRepository repository = new UserRepository();
   }
   ```

3. **Encapsulation** - Hide implementation details

   ```java
   // ✅ GOOD - Private helpers
   @Service
   public class PersonService {
       public PersonDTO toDTO(Person person) {
           PersonDTO dto = new PersonDTO();
           mapCommonFields(person, dto);  // Private method
           return dto;
       }

       private void mapCommonFields(Person p, PersonDTO dto) {
           // Implementation hidden
       }
   }

   // ❌ POOR - Exposed implementation
   public class PersonService {
       public void mapFirstName(Person p, PersonDTO dto) { }
       public void mapLastName(Person p, PersonDTO dto) { }
       public void mapEmail(Person p, PersonDTO dto) { }
       // Leaked implementation details
   }
   ```

4. **Abstraction** - Use meaningful abstractions

   ```java
   // ✅ GOOD - Clear abstraction
   public interface AuthenticationProvider {
       User authenticate(String username, String password);
   }

   // ❌ POOR - Over-abstracted
   public interface DoStuff {
       Object process(Object input);
   }
   ```

5. **Naming** - Clear, self-documenting

   ```java
   // ✅ GOOD
   private LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
   public List<User> getInactiveUsers() { }

   // ❌ POOR
   private LocalDateTime ud = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
   public List<User> getU() { }
   ```

### Code Formatting

**Automatic formatting required:**

```bash
# Format code (Google Java Format)
./format-code.sh

# Format without building
./format-only.sh

# This is mandatory - all code must pass formatting
./mvnw spotless:check
```

### Checkstyle Validation

```bash
# Check compliance
./mvnw checkstyle:check

# Fix automatically
./mvnw spotless:apply
```

---

## 🧪 Testing Requirements

### Unit Tests

```java
@Test
void testGetUserById_WhenUserExists_ThenReturnUser() {
    // Arrange
    Long userId = 1L;
    User expected = new User(userId, "John Doe");
    when(repository.findById(userId)).thenReturn(Optional.of(expected));

    // Act
    User actual = service.getUserById(userId);

    // Assert
    assertThat(actual).isEqualTo(expected);
    verify(repository).findById(userId);
}

@Test
void testGetUserById_WhenUserNotFound_ThenThrowException() {
    // Arrange
    Long userId = 999L;
    when(repository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.getUserById(userId))
        .isInstanceOf(EntityNotFoundException.class);
}
```

### Test Coverage

**Minimum requirements:**

- Unit tests for services: 80% coverage
- Unit tests for controllers: 70% coverage
- New features: 100% coverage

### Run Tests

```bash
# All tests
./mvnw test

# Specific test
./mvnw test -Dtest=UserServiceTest

# With coverage
./mvnw test jacoco:report
# View report: target/site/jacoco/index.html
```

---

## 📚 Documentation

### Update Documentation When:

- Adding new API endpoint → Update API.md
- Changing database schema → Update DATABASE.md
- Modifying architecture → Update ARCHITECTURE.md
- Changing deployment process → Update DEPLOYMENT.md
- Adding development tool/process → Update DEVELOPMENT.md

### Document Format

````markdown
# Title

## Section 1

Brief description

### Subsection 1.1

Details with examples

```bash
code example
```
````

## Section 2

More content

````

### JavaDoc Comments

```java
/**
 * Retrieves all active users from the specified department.
 *
 * This method queries the database and filters out inactive users.
 * Results are cached for 5 minutes to improve performance.
 *
 * @param department the department name filter (case-sensitive)
 * @return list of active users in the department
 * @throws DepartmentNotFoundException if department doesn't exist
 *
 * @see #getInactiveUsers
 * @since 1.0
 */
public List<User> getActiveUsers(String department) { }
````

---

## 🔍 Code Review Checklist

**Reviewers check:**

- [ ] Code follows CLEAN principles
- [ ] Follows project coding standards
- [ ] Tests are adequate (unit + integration)
- [ ] Documentation is updated
- [ ] No hardcoded secrets or sensitive data
- [ ] Error handling is appropriate
- [ ] Performance acceptable (no obvious bottlenecks)
- [ ] Security vulnerabilities addressed
- [ ] Follows existing patterns in codebase
- [ ] Code is maintainable and understandable
- [ ] **Authorization checks**: Protected endpoints have `@PreAuthorize` annotations with appropriate roles
- [ ] **Security logging**: Uses `RoleAwareAccessDeniedHandler` for failed authorization attempts
- [ ] **User info extraction**: Service code uses `UserInformationJWT` for accessing authenticated user data (not direct token manipulation)

**Authors ensure before requesting review:**

- [ ] Code formatted: `./format-code.sh`
- [ ] All tests pass: `./mvnw test`
- [ ] Checkstyle passes: `./mvnw checkstyle:check`
- [ ] Build succeeds: `./mvnw clean package`
- [ ] Documentation updated
- [ ] Commit messages are clear
- [ ] No debug code or commented-out lines
- [ ] No breaking changes (unless approved)
- [ ] **Protected endpoints**: All new protected endpoints have `@PreAuthorize` annotations
- [ ] **Security review**: Considered authorization requirements for new endpoints
- [ ] **No token access**: Service code doesn't directly access JWT token (use `UserInformationJWT` instead)

---

## 🐛 Reporting Issues

### Bug Report Template

```markdown
## Description

Clear description of the bug

## Reproduction Steps

1. Step one
2. Step two
3. Step three

## Expected Behavior

What should happen

## Actual Behavior

What actually happened

## Environment

- OS: [e.g. Windows 10]
- Java Version: [e.g. Java 21]
- Branch: [e.g. main]
- Reproduction: [Always / Sometimes / Rarely]

## Error Logs
```

Paste relevant error messages

```

## Possible Fix
(Optional) Suggest a solution if you have one
```

### Feature Request Template

```markdown
## Description

Clear description of the feature

## Problem It Solves

What problem does this feature address?

## Proposed Solution

How should this feature work?

## Alternatives Considered

Other approaches you've thought about

## Additional Context

Screenshots, mockups, or examples
```

---

## 🚀 Release Process

### Version Numbering

Follow [Semantic Versioning](https://semver.org/):

- MAJOR: Breaking changes
- MINOR: New features (backward compatible)
- PATCH: Bug fixes

Example: v1.2.3

### Release Checklist

- [ ] All tests pass
- [ ] Checkstyle passes
- [ ] Documentation updated
- [ ] Version bumped in pom.xml
- [ ] CHANGELOG.md updated
- [ ] Commit tagged with version
- [ ] Release notes written
- [ ] Artifacts built and tested

---

## 📞 Getting Help

### Common Questions

- **Setup issues?** See DEVELOPMENT.md "Setup Your Environment"
- **Code standards?** See this document "Coding Standards"
- **API questions?** See API.md
- **Architecture?** See ARCHITECTURE.md
- **Database?** See DATABASE.md

### Asking for Help

1. Check documentation first
2. Search existing issues
3. Ask in team chat/channel
4. Create an issue with details
5. Contact team lead if blocked

---

## 🎓 Learning Resources

### Java & Spring Boot

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/reference/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Clean Code by Robert C. Martin](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)

### Testing

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

### Database

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Flyway Documentation](https://flywaydb.org/documentation/)

---

## ✅ Contribution Workflow Summary

```
1. Fork/clone repository
2. Create feature branch: git checkout -b feature/name
3. Make changes following code standards
4. Write/update tests
5. Format code: ./format-code.sh
6. Run tests: ./mvnw test
7. Validate: ./mvnw clean package
8. Commit with clear messages
9. Push: git push origin feature/name
10. Create Pull Request on GitHub
11. Address review comments
12. Merge when approved
13. Delete feature branch
```

---

## 📊 Project Statistics

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7
- **Build Tool**: Maven
- **Test Framework**: JUnit 5 + Mockito
- **Code Quality**: Checkstyle + Spotless (Google Java Format)
- **License**: MIT

---

**Thank you for contributing!** 🎉

Questions? Check [docs/INDEX.md](INDEX.md) or ask in the team channel.

Last Updated: November 5, 2025
