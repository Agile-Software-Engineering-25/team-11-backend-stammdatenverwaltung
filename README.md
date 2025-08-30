# 🏢 Stammdatenverwaltung

[![Spring Boot General](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Spring Boot Specifically](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F?style=flat-square&logo=spring-boot)](https://docs.spring.io/spring-boot/index.html)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=flat-square&logo=apache-maven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

> A modern Spring Boot microservice for **master data management** in the ASE (Agile Software Engineering) project. Built with enterprise-grade security, monitoring, and comprehensive API documentation.

Stammdatenverwaltung provides a robust foundation for managing user-related master data with dual-profile architecture, automated code quality, and production-ready monitoring capabilities.

## ✨ Features

- 🔧 **Multi-Profile Configuration**: Seamless switching between development (`dev`) and production (`prod`) environments
- 🗄️ **Database Flexibility**: H2 file database for development, PostgreSQL for production
- 🔒 **Enterprise Security**: Spring Security with profile-specific authentication and authorization
- 📖 **API Documentation**: Interactive OpenAPI 3.0 specification with Swagger UI
- 📊 **Production Monitoring**: Spring Boot Actuator endpoints for health checks and metrics
- 🎨 **Code Quality**: Automated formatting with Spotless and logic validation with Checkstyle
- 🚀 **Hot Reload**: Spring Boot DevTools for rapid development cycles
- 🐳 **Containerization**: Docker support with multi-stage builds

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/ase/stammdatenverwaltung/
│   │   ├── 🚀 StammdatenverwaltungApplication.java  # Main application class
│   │   ├── ⚙️ config/
│   │   │   ├── 📋 OpenApiConfig.java               # Swagger/OpenAPI configuration
│   │   │   └── 🔐 SecurityConfig.java              # Security configuration (dev/prod)
│   │   ├── 🌐 controllers/                         # REST controllers
│   │   ├── 📦 dto/                                 # Data Transfer Objects
│   │   ├── 🏛️ entities/                            # JPA entities
│   │   ├── 💾 repositories/                        # Data repositories
│   │   └── 🔧 services/                            # Business logic
│   └── resources/
│       ├── ⚙️ application.yaml                     # Base configuration
│       ├── 🛠️ application-dev.yaml                # Development profile
│       └── 🏭 application-prod.yaml               # Production profile
└── test/
    └── java/com/ase/stammdatenverwaltung/
        └── 🧪 StammdatenverwaltungApplicationTests.java
```

## 📋 Prerequisites

| Requirement       | Version | Purpose                              |
| ----------------- | ------- | ------------------------------------ |
| ☕ **Java**       | 21+     | Runtime environment                  |
| 📦 **Maven**      | 3.8+    | Build tool and dependency management |
| 🐳 **Docker**     | Latest  | Containerized deployment (optional)  |
| 🐘 **PostgreSQL** | 13+     | Production database                  |

## 🚀 Getting Started

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung.git
cd team-11-backend-stammdatenverwaltung
```

### 2️⃣ Build the Project

```bash
./mvnw install
```

### 3️⃣ Run the Application

#### 🛠️ Development Mode (Default)

```bash
# 🛠️ Uses H2 database, relaxed security, public Swagger UI
./mvnw spring-boot:run

# Or explicitly specify dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 🏭 Production Mode

```bash
# 🔐 Requires PostgreSQL and environment variables
export SPRING_PROFILES_ACTIVE=prod
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=secure-password
export DATABASE_URL=jdbc:postgresql://localhost:5432/stammdatenverwaltung
export DATABASE_USERNAME=db_user
export DATABASE_PASSWORD=db_password
./mvnw spring-boot:run
```

## 🌐 Access Points

### 🛠️ Development Environment

| Service             | URL                                   | Authentication |
| ------------------- | ------------------------------------- | -------------- |
| 🏠 **Application**  | http://localhost:8080                 | ❌ None        |
| 📖 **Swagger UI**   | http://localhost:8080/swagger-ui.html | ❌ None        |
| 🗄️ **H2 Console**   | http://localhost:8080/h2-console      | ❌ None        |
| ❤️ **Health Check** | http://localhost:8080/actuator/health | ❌ None        |

### 🏭 Production Environment

| Service             | URL                                   | Authentication |
| ------------------- | ------------------------------------- | -------------- |
| 🏠 **Application**  | http://localhost:8080                 | ✅ Required    |
| 📖 **Swagger UI**   | http://localhost:8080/swagger-ui.html | ✅ Required    |
| ❤️ **Health Check** | http://localhost:8080/actuator/health | ❌ None        |

## 🐳 Deployment

### Docker Deployment

#### 🛠️ Development Environment

```bash
docker build -t stammdatenverwaltung .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev stammdatenverwaltung
```

#### 🏭 Production Environment with Docker Compose

```bash
# 📋 Copy and configure environment variables
cp .env.example .env
# Edit .env with your production values

# 🚀 Start with PostgreSQL database
docker-compose up -d
```

#### 🔧 Manual Production Deployment

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

## 🧪 Testing

### Unit Tests

Run unit tests using Maven:

```bash
./mvnw test
```

### Integration Tests

Currently includes basic context loading tests. Additional integration tests will be added as business logic is implemented.

### System Tests

System tests will be executed on a central server with reports provided separately.

## 🎨 Code Quality & Formatting

This project uses a **dual approach** for code quality: **Spotless** for automatic formatting and **Checkstyle** for logic validation.

### ✨ Quick Commands

```bash
# 🚀 Format code + run logic checks
./format-code.cmd     # Windows
./format-code.sh      # Linux/Mac

# 🎨 Format code only
./format-only.cmd     # Windows
./format-only.sh      # Linux/Mac

# 🔍 Individual commands
./mvnw spotless:apply    # Auto-format code
./mvnw checkstyle:check  # Logic & complexity check
```

### 📊 What's Automated vs Manual

| Tool              | Purpose          | Behavior                | Focus                              |
| ----------------- | ---------------- | ----------------------- | ---------------------------------- |
| 🎨 **Spotless**   | Code formatting  | ✅ **Auto-fixes**       | Style, indentation, imports        |
| 🔍 **Checkstyle** | Logic validation | ⚠️ **Reports warnings** | Complexity, naming, best practices |

### 📋 Current Status

After setup: **44 formatting violations → 11 meaningful logic warnings**

> 📖 **See [`FORMATTING_SETUP.md`](FORMATTING_SETUP.md)** for complete configuration details.

## ⚙️ Configuration

### 📊 Profile Management

This application supports dual-environment configuration:

| Profile       | Database   | Security | Purpose               |
| ------------- | ---------- | -------- | --------------------- |
| 🛠️ **`dev`**  | H2 File DB | Relaxed  | Development & Testing |
| 🏭 **`prod`** | PostgreSQL | Strict   | Production Deployment |

> 📖 **See [`PROFILE_SETUP.md`](PROFILE_SETUP.md)** for detailed profile configuration.

## 🔧 Dependencies & Tech Stack

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

| Variable                 | Profile | Required | Default | Description               |
| ------------------------ | ------- | -------- | ------- | ------------------------- |
| `SPRING_PROFILES_ACTIVE` | Both    | No       | `dev`   | Active Spring profile     |
| `DATABASE_URL`           | prod    | Yes      | -       | PostgreSQL connection URL |
| `DATABASE_USERNAME`      | prod    | Yes      | -       | Database username         |
| `DATABASE_PASSWORD`      | prod    | Yes      | -       | Database password         |
| `ADMIN_USERNAME`         | prod    | Yes      | -       | Admin username            |
| `ADMIN_PASSWORD`         | prod    | Yes      | -       | Admin password            |
| `SERVER_PORT`            | Both    | No       | `8080`  | Server port               |
