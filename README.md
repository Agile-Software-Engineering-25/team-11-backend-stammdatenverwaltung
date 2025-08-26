# Stammdatenverwaltung

Stammdatenverwaltung is a Spring Boot-based microservice designed for master data management in the ASE (Agile Software Engineering) project. It provides a foundation for managing user-related master data with comprehensive security, monitoring, and documentation capabilities.

## Features

- **Multi-Profile Configuration**: Supports development (`dev`) and production (`prod`) profiles
- **Database Flexibility**: H2 for development, PostgreSQL for production
- **Security**: Spring Security with profile-specific configurations
- **API Documentation**: OpenAPI 3.0 with Swagger UI integration
- **Monitoring**: Spring Boot Actuator endpoints for health and metrics
- **Code Quality**: Checkstyle integration with CI/CD pipeline
- **Containerization**: Docker support with multi-stage builds
- **Development Tools**: Hot reloading with Spring Boot DevTools

## Project Structure

```
src/
├── main/
│   ├── java/com/ase/stammdatenverwaltung/
│   │   ├── StammdatenverwaltungApplication.java  # Main application class
│   │   ├── config/
│   │   │   ├── OpenApiConfig.java               # Swagger/OpenAPI configuration
│   │   │   └── SecurityConfig.java              # Security configuration (dev/prod)
│   │   ├── controllers/                         # REST controllers (placeholder)
│   │   ├── entities/                           # JPA entities (placeholder)
│   │   ├── repositories/                       # Data repositories (placeholder)
│   │   └── services/                           # Business logic (placeholder)
│   └── resources/
│       ├── application.yaml                    # Base configuration
│       ├── application-dev.yaml               # Development profile
│       └── application-prod.yaml              # Production profile
└── test/
    └── java/com/ase/stammdatenverwaltung/
        └── StammdatenverwaltungApplicationTests.java
```

## Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- Docker (optional, for containerized deployment)
- PostgreSQL (for production deployment)

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung.git
cd team-11-backend-stammdatenverwaltung
```

### Build the Project

```bash
./mvnw install
```

### Run the Application

#### Development Mode (Default)

```bash
# Uses H2 database, relaxed security, public Swagger UI
./mvnw spring-boot:run

# Or explicitly specify dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Production Mode

```bash
# Requires PostgreSQL and environment variables
export SPRING_PROFILES_ACTIVE=prod
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=secure-password
./mvnw spring-boot:run
```

**Development Access URLs:**

- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html (no authentication required)
- H2 Console: http://localhost:8080/h2-console (no authentication required)
- Health Check: http://localhost:8080/actuator/health

**Production Access:**

- Application: http://localhost:8080 (authentication required)
- Health Check: http://localhost:8080/actuator/health (public)
- All other endpoints require HTTP Basic Authentication

## Deployment

### Docker Deployment

#### Development Environment

```bash
docker build -t stammdatenverwaltung .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev stammdatenverwaltung
```

#### Production Environment with Docker Compose

```bash
# Copy and configure environment variables
cp .env.example .env
# Edit .env with your production values

# Start with PostgreSQL database
docker-compose up -d
```

#### Manual Production Deployment

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e ADMIN_USERNAME=admin \
  -e ADMIN_PASSWORD=your-secure-password \
  -e DATABASE_URL=jdbc:postgresql://your-db:5432/stammdatenverwaltung \
  -e DATABASE_USERNAME=db_user \
  -e DATABASE_PASSWORD=db_password \
  stammdatenverwaltung
```

## Testing

### Unit Tests

Run unit tests using Maven:

```bash
./mvnw test
```

### Integration Tests

Currently includes basic context loading tests. Additional integration tests will be added as business logic is implemented.

### System Tests

System tests will be executed on a central server with reports provided separately.

## Code Style & Linting

This project uses Checkstyle (CLI jar) and EditorConfig to enforce a consistent Java code style.

- How to run locally:

  - Using the bundled/dl jar:
    - Windows PowerShell:
      ```powershell
      java -jar checkstyle.jar -c checkstyle.xml -f xml -o target\checkstyle-report.xml src\main\java src\test\java
      ```
      Or, if you have `checkstyle-11.0.0-all.jar`:
      ```powershell
      java -jar checkstyle-11.0.0-all.jar -c checkstyle.xml -f xml -o target\checkstyle-report.xml src\main\java src\test\java
      ```
    - Plain, human‑readable output:
      ```powershell
      java -jar checkstyle.jar -c checkstyle.xml -f plain src\main\java src\test\java
      ```

- IDE auto-formatting:

  - `.editorconfig` sets 2‑space indentation for `*.java` and YAML, trims trailing whitespace, and enforces final newline.
  - IntelliJ import layout is aligned with our import groups.

- Checkstyle rules (high‑level):
  - Naming:
    - `TypeName`, `MethodName`, `LocalVariableName`, `MemberName`, `ConstantName`
    - Generic type parameters: `ClassTypeParameterName`, `MethodTypeParameterName`, `InterfaceTypeParameterName` (single capital letter)
  - Packages: `PackageName` (lowercase dotted segments)
  - Formatting:
    - `Indentation`: 2 spaces; continuation indent 4; tabs disallowed
    - `LeftCurly` = `eol`; `RightCurly` = `alone`
    - `MethodParamPad` = `nospace`
    - `WhitespaceAfter` for `COMMA` and control‑flow keywords
    - `OperatorWrap` enabled (default behavior)
    - `LineLength` max 80; ignores package/import/URLs
    - `ParenPad` is disabled in this config
  - Statements:
    - `NeedBraces` on all control statements
    - `FallThrough` for switch
    - `OneStatementPerLine`
  - Imports:
    - `ImportOrder` under `TreeWalker`: groups `java, javax, org, com`; `option=top`; `sortStaticImportsAlphabetically=true`
    - `UnusedImports` to flag and fail on unused imports
  - Other:
    - `MagicNumber` enabled; ignores `-1,0,1,2`, annotations, and `hashCode`

Adjust rules in `checkstyle.xml`; IDE basics are in `.editorconfig`.

## CI: GitHub Actions (Checkstyle)

We run Checkstyle in CI on every push and on PRs to `main`.

- Workflow: `.github/workflows/checkstyle.yml`

  - Sets up Temurin JDK 21
  - Uses `checkstyle-11.0.0-all.jar` (downloaded if not in repo) or `checkstyle.jar` if present
  - Runs Checkstyle against `src/main/java` and `src/test/java`
  - Fails the job on violations and uploads `target/checkstyle-report.xml` as an artifact

- Blocking PRs on failures:
  - In GitHub repo settings → Branches → Protect `main`
  - Enable “Require status checks to pass before merging”
  - Select the “Checkstyle” job as a required status check

## Dependencies

The project uses the following key dependencies:

### Core Framework

- **`spring-boot-starter-web`** (3.5.4): REST API development and embedded Tomcat server
- **`spring-boot-starter-data-jpa`**: JPA integration with Hibernate for data persistence
- **`spring-boot-starter-security`**: Authentication and authorization with HTTP Basic Auth
- **`spring-boot-starter-validation`**: Bean validation using JSR-303 annotations

### Database

- **`h2`**: In-memory database for development and testing
- **`postgresql`**: Production database driver for PostgreSQL

### Documentation & Monitoring

- **`springdoc-openapi-starter-webmvc-ui`** (2.8.10): OpenAPI 3.0 spec generation and Swagger UI
- **`spring-boot-starter-actuator`**: Production monitoring endpoints (health, metrics, info)

### Development Tools

- **`spring-boot-devtools`**: Hot reloading and development utilities
- **`lombok`**: Reduces boilerplate code (getters, setters, constructors)

### Testing

- **`spring-boot-starter-test`**: Comprehensive testing stack (JUnit 5, Mockito, AssertJ)
- **`spring-security-test`**: Security testing utilities

## Configuration Profiles

The application supports two distinct profiles:

### Development Profile (`dev`) - Default

- **Database**: H2 in-memory (file-based persistence in `./data/mydb`)
- **Security**: Relaxed (public access to Swagger UI, H2 console, API endpoints)
- **Credentials**: `dev-user` / `dev-password`
- **Logging**: DEBUG level for application and security
- **Features**: Hot reloading, detailed error messages

### Production Profile (`prod`)

- **Database**: PostgreSQL (configurable via environment variables)
- **Security**: Strict authentication required for all endpoints except health
- **Credentials**: Configurable via `ADMIN_USERNAME` / `ADMIN_PASSWORD`
- **Logging**: INFO level with security warnings
- **Features**: Optimized for production deployment

## Environment Variables

| Variable                 | Profile | Required | Default    | Description               |
| ------------------------ | ------- | -------- | ---------- | ------------------------- |
| `SPRING_PROFILES_ACTIVE` | Both    | No       | `dev`      | Active Spring profile     |
| `DATABASE_URL`           | prod    | Yes      | -          | PostgreSQL connection URL |
| `DATABASE_USERNAME`      | prod    | Yes      | -          | Database username         |
| `DATABASE_PASSWORD`      | prod    | Yes      | -          | Database password         |
| `ADMIN_USERNAME`         | prod    | No       | `admin`    | Admin username            |
| `ADMIN_PASSWORD`         | prod    | No       | `changeme` | Admin password            |
| `SERVER_PORT`            | Both    | No       | `8080`     | Server port               |
