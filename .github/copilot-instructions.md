# Copilot Instructions for Stammdatenverwaltung

## Project Overview

This is a Spring Boot 3.5.5 microservice for master data management using Java 21. The project follows a dual-profile architecture with distinct `dev` and `prod` configurations.

## Profile-Based Architecture

The application operates in two distinct modes controlled by Spring profiles:

### Development Profile (`dev`) - Default

- H2 file database (`./data/mydb`)
- Relaxed security (public access to Swagger UI, H2 console, all API endpoints)
- Credentials: `dev-user` / `dev-password`
- Debug logging enabled
- Start with: `./mvnw spring-boot:run`

### Production Profile (`prod`)

- PostgreSQL database
- Strict security (only `/actuator/health` public, all else requires auth)
- Environment-driven configuration (`DATABASE_URL`, `ADMIN_USERNAME`, etc.)
- INFO-level logging
- Start with: `SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run`

## Key Configuration Files

- `src/main/resources/application.yaml` - Base configuration
- `src/main/resources/application-dev.yaml` - Development overrides
- `src/main/resources/application-prod.yaml` - Production overrides
- `src/main/java/com/ase/stammdatenverwaltung/config/SecurityConfig.java` - Profile-specific security chains

## Available Dependencies & Plugins

The project includes the following key dependencies and Maven plugins for development:

### Core Spring Boot Starters
- **spring-boot-starter-web**: REST API development with embedded Tomcat
- **spring-boot-starter-data-jpa**: JPA/Hibernate for database operations
- **spring-boot-starter-security**: Authentication and authorization
- **spring-boot-starter-validation**: Bean validation with annotations (`@Valid`, `@NotNull`, etc.)
- **spring-boot-starter-actuator**: Production-ready monitoring endpoints (`/actuator/health`, etc.)
- **spring-boot-devtools**: Hot reload and development tools (dev profile only)

### Database Support
- **H2 Database**: File-based database for development (`./data/mydb`)
- **PostgreSQL**: Production database driver

### Documentation & Utilities
- **SpringDoc OpenAPI (v2.8.11)**: Auto-generated API documentation and Swagger UI
- **Lombok**: Reduce boilerplate code with annotations (`@Data`, `@Builder`, etc.)

### Testing Framework
- **spring-boot-starter-test**: Comprehensive testing with JUnit 5, Mockito, AssertJ
- **spring-security-test**: Security-specific testing utilities

### Code Quality & Formatting
- **Spotless Maven Plugin (v2.43.0)**: Automatic code formatting using Google Java Format
- **Maven Checkstyle Plugin (v3.5.0)**: Logic and complexity analysis (checkstyle-logic-only.xml)

### Maven Plugins
- **spring-boot-maven-plugin**: Application packaging, Docker image building, dev server
- **maven-compiler-plugin (v3.14.0)**: Java 21 compilation with Lombok annotation processing
- **maven-surefire-plugin (v3.5.2)**: Unit test execution
- **maven-failsafe-plugin (v3.5.2)**: Integration test execution

### Development Best Practices
- Use `@RestController` with `@RequestMapping("/api/v1")` for API endpoints
- Leverage `@Entity` with Lombok annotations for JPA entities
- Implement validation using Bean Validation annotations
- Use `@Service`, `@Repository`, `@Component` for proper layering
- Utilize Spring Security's method-level security with `@PreAuthorize`
- Take advantage of Spring Boot's auto-configuration and conditional beans

## Package Structure

Follow the established package organization under `com.ase.stammdatenverwaltung`:

- `config/` - Configuration classes (existing: SecurityConfig, OpenApiConfig)
- `controllers/` - REST controllers (placeholder, add new endpoints here)
- `entities/` - JPA entities (placeholder, add domain models here)
- `repositories/` - Data repositories (placeholder, add data access here)
- `services/` - Business logic (placeholder, add services here)
- `components/` - Spring components (placeholder, add utilities here)

## Code Quality & Style Requirements

The project uses a **dual approach** for code quality:

### ✨ Spotless (Automatic Formatting)
- **Purpose**: Automatic code formatting using Google Java Format
- **Handles**: Indentation (2 spaces), line length, import organization, braces, spacing
- **Behavior**: Automatically fixes formatting issues
- **Style**: Google Java Style Guide with removed unused imports and formatted annotations

### 🔍 Checkstyle (Logic & Complexity)
- **Purpose**: Code logic and complexity analysis only (no formatting rules)
- **Focus**: Best practices, naming conventions, complexity metrics
- **Rules**: Method length (max 50 lines), cyclomatic complexity (max 10), parameter count (max 7)
- **Naming**: PascalCase classes, camelCase methods/variables, ALL_CAPS constants
- **Behavior**: Reports violations for manual review (warnings only, doesn't fail build)

### Quick Commands
```bash
# Format and check (recommended workflow)
./format-code.cmd                       # Windows
./format-code.sh                        # Linux/Mac

# Format only
./format-only.cmd                       # Windows  
./format-only.sh                        # Linux/Mac

# Individual commands
./mvnw.cmd spotless:apply              # Auto-format code
./mvnw.cmd spotless:check              # Check formatting
./mvnw.cmd checkstyle:check            # Logic/complexity checks
```

## Development Workflow

```bash
# Quick development cycle (Spring Boot DevTools enables hot reload)
./mvnw spring-boot:run                  # Start dev mode with auto-restart
# Access: http://localhost:8080/swagger-ui.html (no auth)

# Build and test
./mvnw clean install                    # Full build with unit and integration tests
./mvnw test                             # Unit tests only (Surefire plugin)
./mvnw integration-test                 # Integration tests only (Failsafe plugin)
./mvnw verify                           # Full verification including integration tests

# Code quality and formatting
./format-code.cmd                       # Format code + logic checks (Windows)
./format-code.sh                        # Format code + logic checks (Linux/Mac)
./mvnw spotless:apply                   # Auto-format code only
./mvnw checkstyle:check                 # Logic/complexity checks only

# Spring Boot specific commands
./mvnw spring-boot:start                # Start app in background for testing
./mvnw spring-boot:stop                 # Stop background app
```

## Security Implementation Pattern

When adding new endpoints, follow the existing security model:

- `@Profile("dev")` security chain: Allow public access to new API endpoints
- `@Profile("prod")` security chain: Require authentication for all endpoints except health
- Use `app.security.relaxed` property to conditionally enable features

## Database Access Pattern

- Development: H2 with `spring.jpa.hibernate.ddl-auto: update`
- Production: PostgreSQL with `spring.jpa.hibernate.ddl-auto: validate`
- Entities should be placed in `entities/` package for component scanning

## API Documentation

OpenAPI 3.0 with SpringDoc (v2.8.11) providing enhanced Swagger UI:

- All API endpoints should use `/api/v1/` prefix for automatic documentation
- Swagger UI available at `/swagger-ui.html` (dev) or with auth (prod)
- Three API groups configured: `public-api`, `actuator`, `all`
- Use `@Operation`, `@ApiResponse`, `@Schema` annotations for detailed API documentation
- Validation annotations (`@Valid`, `@NotNull`, `@Size`) automatically reflected in OpenAPI spec

## Testing Conventions

Maven plugins configured for comprehensive testing:

- **Unit tests**: `**/*Test.java`, `**/*Tests.java` (Surefire plugin v3.5.2)
- **Integration tests**: `**/*IT.java`, `**/*IntegrationTest.java` (Failsafe plugin v3.5.2)
- **Spring Boot Test Support**: Use `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest` for different test slices
- **Security Testing**: Spring Security Test dependency available for authentication/authorization tests
- **Test Data**: Use `@Sql`, `@TestPropertySource` for test-specific configurations
- **Context loading test**: Exists in `StammdatenverwaltungApplicationTests.java`

Run tests individually:
```bash
./mvnw test                              # Unit tests (Surefire)
./mvnw integration-test                  # Integration tests (Failsafe)
./mvnw verify                           # Both unit and integration tests
```

## Environment Variables (Production)

Essential variables for production deployment (all required):

- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- `ADMIN_USERNAME`, `ADMIN_PASSWORD`
- `SERVER_PORT` (optional, defaults to 8080)

**Security Note**: As of commit 7ff88d1, all authentication credentials must be explicitly provided via environment variables for production deployment - no default values are used to enhance security.

## Documentation Maintenance

When making fundamental changes, update documentation files such as these:

- `.github/copilot-instructions.md` - AI agent guidance
- `README.md` - Project overview and getting started
- `PROFILE_SETUP.md` - Profile-specific configuration details
