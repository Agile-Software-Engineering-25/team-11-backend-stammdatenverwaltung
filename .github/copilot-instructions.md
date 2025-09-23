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
- Environment-driven configuration (`DATABASE_URL`, `KEYCLOAK_ISSUER_URI`, etc.)
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
- **Flyway**: Database migration and versioning tool (v10.21.0)

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
- **flyway-maven-plugin**: Database migration management with H2 and PostgreSQL support (Spring Boot integration preferred)

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

# Database migrations
./mvnw flyway:migrate                   # Apply pending migrations
./mvnw flyway:info                      # Show migration status
./mvnw flyway:validate                  # Validate migrations

# Spring Boot specific commands
./mvnw spring-boot:start                # Start app in background for testing
./mvnw spring-boot:stop                 # Stop background app
```

## Security Implementation Pattern

The application uses **Keycloak OAuth2 JWT authentication** with profile-specific configurations:

### Authentication Methods
- **Development**: Dual support for Basic Auth (dev tools) and JWT (API testing)
- **Production**: JWT-only authentication with strict validation

### Role-Based Access Control
- **Realm Roles**: Global roles from `realm_access.roles` token claim
- **Client Roles**: API-specific roles from `resource_access.stammdatenverwaltung-api.roles`
- **Spring Security**: Roles mapped to `ROLE_*` authorities for `@PreAuthorize` annotations

### Security Patterns for New Endpoints
- **Public endpoints**: Use `/api/v1/public/**` pattern, no authentication required
- **Admin endpoints**: Use `/api/v1/admin/**` pattern, requires `ROLE_ADMIN`
- **User endpoints**: Use `/api/v1/user/**` pattern, requires `ROLE_USER`
- **Protected endpoints**: All other `/api/**` endpoints require valid JWT authentication

### Configuration
- **Keycloak Integration**: OAuth2 Resource Server with issuer-uri discovery
- **JWT Validation**: Automatic signature verification, audience validation, role extraction
- **Method Security**: Use `@PreAuthorize("hasRole('ROLE_NAME')")` for fine-grained control

### Environment Variables
- `KEYCLOAK_ISSUER_URI`: Keycloak realm issuer URL (required for prod)
- `KEYCLOAK_API_AUDIENCE`: Audience in JWT tokens (default: stammdatenverwaltung-api)

## Database Management & Migration Strategy

The project uses **Flyway** for database schema versioning combined with **JPA/Hibernate** for object-relational mapping:

### Migration-First Approach
- **Schema Changes**: All database schema changes MUST be done through Flyway migrations
- **JPA Configuration**: `hibernate.ddl-auto: validate` in all profiles (no auto-schema generation)
- **Migration Location**: `src/main/resources/db/migration/`
- **Migration Naming**: `V{version}__{description}.sql` (e.g., `V1__Create_initial_schema.sql`)

### Database Workflow
1. **Create JPA Entity**: Define your entity classes with proper annotations
2. **Create Migration**: Write SQL migration file for schema changes
3. **Apply Migration**: Run `./mvnw spring-boot:run` (automatic) or start the application
4. **Validate**: Hibernate validates schema matches entity definitions

### Flyway Commands
```bash
# Start application (runs migrations automatically)
./mvnw spring-boot:run                 # Preferred: automatic migration

# Build and test (includes migration validation)
./mvnw clean install

# Note: Maven plugin commands may have compatibility issues with Spring Boot 3.x
# Use Spring Boot integration for reliable migration execution
```

### Migration Best Practices
- **Version Control**: All migrations are version-controlled SQL files
- **Incremental Changes**: Small, atomic changes per migration
- **Naming Convention**: Use descriptive names with underscores
- **Testing**: Test migrations on development database first
- **Rollback Planning**: Plan for rollback scenarios (manual or Flyway Pro)

## Database Access Pattern

- **Development**: H2 with Flyway migrations and schema validation
- **Production**: PostgreSQL with Flyway migrations and schema validation  
- **Migration Execution**: Automatic on application startup or manual via Maven
- **Schema Validation**: Hibernate validates database schema matches JPA entities
- **Entity Placement**: All entities should be in `entities/` package for component scanning

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
- `KEYCLOAK_ISSUER_URI`, `KEYCLOAK_API_AUDIENCE`
- `SERVER_PORT` (optional, defaults to 8080)

**Security Note**: Production authentication is JWT-only via Keycloak. Ensure `KEYCLOAK_ISSUER_URI` and `KEYCLOAK_API_AUDIENCE` are set via environment variables; no Basic Auth users are configured in production.

## Documentation Maintenance

When making fundamental changes, update documentation files such as these:

- `.github/copilot-instructions.md` - AI agent guidance
- `README.md` - Project overview and getting started
- `PROFILE_SETUP.md` - Profile-specific configuration details
- `DATABASE_MANAGEMENT.md` - Flyway and JPA/Hibernate usage guide
