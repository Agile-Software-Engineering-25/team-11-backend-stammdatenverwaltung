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

## Package Structure

Follow the established package organization under `com.ase.stammdatenverwaltung`:

- `config/` - Configuration classes (existing: SecurityConfig, OpenApiConfig)
- `controllers/` - REST controllers (placeholder, add new endpoints here)
- `entities/` - JPA entities (placeholder, add domain models here)
- `repositories/` - Data repositories (placeholder, add data access here)
- `services/` - Business logic (placeholder, add services here)
- `components/` - Spring components (placeholder, add utilities here)

## Code Style Requirements

Strict Checkstyle enforcement with specific rules:

- **Indentation**: 2 spaces (no tabs), continuation indent 4 spaces
- **Line length**: 80 characters max
- **Braces**: Opening brace on same line (`eol`), closing brace alone (`alone`)
- **Imports**: Ordered groups `java, javax, org, com` with static imports sorted alphabetically
- **Naming**: PascalCase classes, camelCase methods/variables, ALL_CAPS constants
- Run locally: `java -jar checkstyle-11.0.0-all.jar -c checkstyle.xml -f plain src\main\java src\test\java`

## Development Workflow

```bash
# Quick development cycle
./mvnw spring-boot:run                    # Start dev mode
# Access: http://localhost:8080/swagger-ui.html (no auth)

# Build and test
./mvnw clean install                      # Full build with tests
./mvnw test                              # Unit tests only

# Code quality check
java -jar checkstyle-11.0.0-all.jar -c checkstyle.xml -f plain src\main\java src\test\java
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

OpenAPI 3.0 with grouped endpoints:

- All API endpoints should use `/api/v1/` prefix for automatic documentation
- Swagger UI available at `/swagger-ui.html` (dev) or with auth (prod)
- Three API groups configured: `public-api`, `actuator`, `all`

## Testing Conventions

- Unit tests: `**/*Test.java`, `**/*Tests.java` (Surefire plugin)
- Integration tests: `**/*IT.java`, `**/*IntegrationTest.java` (Failsafe plugin)
- Context loading test exists in `StammdatenverwaltungApplicationTests.java`

## Environment Variables (Production)

Essential variables for production deployment:

- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- `ADMIN_USERNAME`, `ADMIN_PASSWORD`
- `SERVER_PORT` (optional, defaults to 8080)

## Documentation Maintenance

When making fundamental changes, update documentation files such as these:

- `.github/copilot-instructions.md` - AI agent guidance
- `README.md` - Project overview and getting started
- `PROFILE_SETUP.md` - Profile-specific configuration details
