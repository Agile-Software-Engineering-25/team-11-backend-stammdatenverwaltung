# GitHub Copilot Instructions for Stammdatenverwaltung

> **AI Assistant Guidelines** - Enforcing CLEAN Code Principles, Simplicity, and Research-Driven Development

## 🎯 Core Principles

### 1. **CLEAN Code First**

AI must prioritize **CLEAN principles** in all code contributions:

- **C**ohesion: Keep related functionality together
- **L**ow Coupling: Minimize dependencies between classes/modules
- **E**ncapsulation: Hide implementation details, expose only interfaces
- **A**bstraction: Use meaningful abstractions, not premature optimization
- **N**aming: Use clear, self-documenting names (classes, methods, variables)

### 2. **Simplicity Over Cleverness**

- Write code that is **easy to understand** and maintain, not impressively complex
- Prefer straightforward solutions over clever one-liners
- Use design patterns only when they genuinely solve a problem, not to show knowledge
- Ask yourself: _Would another developer immediately understand this?_
- Avoid over-engineering; build what's needed, not what might be needed

### 3. **Analyze Before Acting**

**ALWAYS** gather context before making changes:

- Read relevant existing code in the repository
- Check related tests to understand expected behavior
- Review similar patterns already established in the codebase
- Understand the purpose and constraints of the module
- Identify if changes might impact other parts of the system
- **Never assume**; verify current state using tools

### 4. **Research with Context7**

For library/framework decisions, **always** use Context7 documentation tools:

- Use `mcp_upstash_conte_resolve-library-id` to find exact library documentation
- Use `mcp_upstash_conte_get-library-docs` to retrieve up-to-date information
- Refer to current Spring Boot 3.5.x and Java 21 documentation
- Check against best practices before implementation
- Document why a specific library version or pattern was chosen

---

## 🛠️ Development Workflow

### Code Quality Standards

1. **Spotless Formatting** (automatic)

   - Run before commits: `./format-code.sh` (Linux/Mac)
   - Uses Google Java Format
   - **Never disable** formatting rules

2. **Checkstyle Validation** (manual review)

   - Validates logic and complexity (not formatting)
   - Configuration: `checkstyle-logic-only.xml`
   - Review violations but don't disable rules without justification

3. **Build and Test**
   - Always run `./mvnw clean package` before committing
   - Ensure all tests pass
   - Fix failing tests immediately

### When Adding New Features

```
1. 📋 Analyze requirements thoroughly
   - What problem does this solve?
   - What constraints exist?
   - How does it fit existing architecture?

2. 🔍 Search for existing patterns
   - How are similar features implemented?
   - Use grep_search to find patterns
   - Follow established conventions

3. 📚 Research dependencies
   - Is the library already available?
   - Use Context7 for latest documentation
   - Justify version choices

4. ✍️ Write tests first (TDD optional)
   - What should this code do?
   - What are edge cases?
   - Tests as specification

5. 💻 Implement with CLEAN principles
   - Single Responsibility: One reason to change
   - Clear naming: No abbreviations
   - Minimal dependencies: Low coupling
   - Proper abstraction: Interfaces where appropriate

6. 📖 Add documentation
   - JavaDoc for public APIs
   - README updates if significant feature
   - Comments explaining complex logic
```

---

## 🎨 Coding Conventions

### Naming Standards

```java
// ✅ GOOD - Clear intent
private final UserService userService;
public void updateEmployeeEmail(String newEmail) { }
private static final int MAX_RETRY_ATTEMPTS = 3;

// ❌ POOR - Ambiguous
private final UserService us;
public void update(String s) { }
private static final int MAX = 3;
```

### Class Organization

```java
public class UserService {
    // 1. Class-level constants
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    // 2. Dependencies
    private final UserRepository userRepository;

    // 3. Constructor
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 4. Public methods
    public User findById(Long id) { }

    // 5. Private helper methods
    private void validateInput(String input) { }
}
```

### Documentation Comments

```java
// ✅ Explain WHY and non-obvious decisions
/**
 * Retrieves users with retry logic because external cache may be temporarily
 * unavailable during scheduled maintenance windows (2-5 AM UTC).
 */
public List<User> getUsersWithFallback(String department) { }

// ❌ Obvious documentation
/**
 * Returns a list of users
 */
public List<User> getUsersWithFallback(String department) { }
```

### Error Handling

```java
// ✅ Specific exceptions with context
throw new EntityNotFoundException("User with ID " + id + " not found in database");

// ❌ Generic errors
throw new Exception("Error");
```

---

## ❌ Common Pitfalls to Avoid

### 1. Premature Optimization

```java
// ❌ Unnecessarily complex
@Cacheable(cacheNames = "users", unless = "#result.isEmpty()")
public List<User> getActiveUsersWithCaching(String department) { }

// ✅ Simple until profiling shows need
public List<User> getActiveUsers(String department) { }
```

### 2. Tight Coupling

```java
// ❌ Hard dependency
public class UserController {
    private UserService userService = new UserService();
}

// ✅ Injected dependency
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

### 3. Cryptic Variable Names

```java
// ❌ No context
LocalDateTime ud = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
List<User> u = repository.findByLastLoginBefore(ud);

// ✅ Self-documenting
LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
List<User> inactiveUsers = repository.findByLastLoginBefore(thirtyDaysAgo);
```

### 4. Ignoring Existing Patterns

```java
// ❌ Creates inconsistency
public class NewMapper {
    public static UserDto map(User user) { }  // Different style
}

// ✅ Follow existing pattern
public class UserMapper {
    public UserDto toDto(User user) { }  // Consistent with codebase
}
```

### 5. Missing Error Context

```java
// ❌ Silent failures
try {
    userRepository.save(user);
} catch (Exception e) {
    LOG.error("Error");
}

// ✅ Meaningful logging
try {
    userRepository.save(user);
} catch (DataIntegrityViolationException e) {
    LOG.error("Failed to save user: email='{}' already exists", user.getEmail(), e);
    throw new DuplicateEmailException(user.getEmail());
}
```

---

## 📝 Code Review Checklist for AI

Before submitting any code change:

- [ ] **CLEAN Principles**: Does it follow C-L-E-A-N?
- [ ] **Simplicity**: Is this the simplest solution?
- [ ] **Analysis**: Did I read existing code and tests first?
- [ ] **Consistency**: Does it match existing patterns in the codebase?
- [ ] **Testing**: Are there tests? Do they pass?
- [ ] **Documentation**: Is the WHY documented in comments?
- [ ] **Naming**: Are names clear and unambiguous?
- [ ] **Coupling**: Are dependencies injected? Is coupling minimal?
- [ ] **Formatting**: Does it pass Spotless and Checkstyle?
- [ ] **Security**: Are there any security implications?
- [ ] **Performance**: Is performance acceptable without premature optimization?
- [ ] **Logging**: Are errors logged with sufficient context?

---

## 🔗 Useful References

- **Spring Boot 3.5.x**: https://docs.spring.io/spring-boot/reference/
- **Spring Data JPA**: https://docs.spring.io/spring-data/jpa/reference/
- **Spring Security**: https://docs.spring.io/spring-security/reference/
- **Jakarta Validation**: https://jakarta.ee/specifications/bean-validation/
- **Flyway**: https://flywaydb.org/documentation/
- **Google Java Style Guide**: https://google.github.io/styleguide/javaguide.html
- **Clean Code**: Robert C. Martin - "Clean Code: A Handbook of Agile Software Craftsmanship"
- **Project Repo**: https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung

---

## 📞 When to Ask for Human Review

AI should **defer to human developers** in these situations:

1. **Architectural Decisions**: Major changes to project structure
2. **Security-Sensitive Code**: Authentication, authorization, encryption
3. **Performance-Critical Code**: Caching, async operations, batch processing
4. **Cross-Module Impact**: Changes affecting multiple services/modules
5. **Business Logic Ambiguity**: When requirements are unclear or contradictory
6. **Complex Algorithms**: Non-trivial calculations or data transformations
7. **Database Schema Changes**: Flyway migrations and schema design

---

**Last Updated**: November 2, 2025  
**Project**: Stammdatenverwaltung - Master Data Management Service  
**Team**: ASE Team 11
