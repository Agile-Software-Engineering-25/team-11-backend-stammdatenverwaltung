# 🏢 Stammdatenverwaltung

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=flat-square&logo=spring-boot)](https://docs.spring.io/spring-boot/index.html)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=flat-square&logo=apache-maven)](https://maven.apache.org/)

> A modern Spring Boot microservice for **master data management** in the ASE (Agile Software Engineering) project. Built with enterprise-grade security (Keycloak JWT), monitoring, and comprehensive API documentation.

Stammdatenverwaltung provides a robust foundation for managing user-related master data with dual-profile architecture, automated code quality, and production-ready monitoring capabilities.

## ✨ Features

- 🔧 **Multi-Profile Configuration**: Seamless switching between development (`dev`) and production (`prod`) environments
- 🗄️ **Database Flexibility**: H2 file database for development, PostgreSQL for production
- 🔒 **Enterprise Security**: Spring Security OAuth2 Resource Server with Keycloak JWT (dev: Basic + JWT, prod: JWT-only)
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

**No installation required!** The application uses H2 file-based database in PostgreSQL compatibility mode.

```bash
# 🛠️ Uses H2 file-based database. Swagger/H2/public API are open; other APIs secured
./mvnw spring-boot:run

# Or explicitly specify dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Access the H2 Console** at http://localhost:8080/h2-console:

- **JDBC URL**: `jdbc:h2:file:./data/devdb`
- **Username**: `sa`
- **Password**: `password`

> 💡 The database is automatically created and migrated on startup via Flyway. Data persists across application restarts in the `./data/` directory.

#### 🏭 Production Mode

```bash
# 🔐 Requires PostgreSQL and environment variables
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/stammdatenverwaltung
export DATABASE_USERNAME=db_user
export DATABASE_PASSWORD=db_password
# Keycloak JWT configuration (required in prod)
export KEYCLOAK_ISSUER_URI=https://your-keycloak/realms/stammdatenverwaltung
export KEYCLOAK_API_AUDIENCE=stammdatenverwaltung-api
./mvnw spring-boot:run
```

## 🌐 Access Points

### 🛠️ Development Environment

| Service             | URL                                   | Authentication |
| ------------------- | ------------------------------------- | -------------- |
| 🏠 **Application**  | http://localhost:8080                 | Mixed          |
| 📖 **Swagger UI**   | http://localhost:8080/swagger-ui.html | ❌ None        |
| 🗄️ **H2 Console**   | http://localhost:8080/h2-console      | ❌ None        |
| ❤️ **Health Check** | http://localhost:8080/actuator/health | ❌ None        |

- Public API: `GET /api/v1/public/**`
- Secured API: all other ` /api/**` require auth (Basic in dev or JWT)

### 🏭 Production Environment

| Service             | URL                                   | Authentication    |
| ------------------- | ------------------------------------- | ----------------- |
| 🏠 **Application**  | http://localhost:8080                 | ✅ JWT (Keycloak) |
| 📖 **Swagger UI**   | http://localhost:8080/swagger-ui.html | ✅ JWT (Keycloak) |
| ❤️ **Health Check** | http://localhost:8080/actuator/health | ❌ None           |

- Public API: `GET /api/v1/public/**`
- Secured API: all other ` /api/**` require valid Bearer token (JWT)

> Keycloak setup and testing: see `KEYCLOAK_SETUP.md`.

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
  -e DATABASE_URL=jdbc:postgresql://your-db:5432/stammdatenverwaltung \
  -e DATABASE_USERNAME=db_user \
  -e DATABASE_PASSWORD=db_password \
  -e KEYCLOAK_ISSUER_URI=https://your-keycloak/realms/stammdatenverwaltung \
  -e KEYCLOAK_API_AUDIENCE=stammdatenverwaltung-api \
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

This project uses a **dual approach** for code quality:

### ✨ Spotless (Automatic Formatting)

- **Purpose**: Automatic code formatting using Google Java Format
- **Handles**: Indentation (2 spaces), line length, import organization, braces, spacing
- **Behavior**: Automatically fixes formatting issues

### 🔍 Checkstyle (Logic & Complexity)

- **Purpose**: Code logic and complexity analysis only (no formatting rules)
- **Focus**: Best practices, naming conventions, complexity metrics
- **Behavior**: Reports violations for manual review (warnings only, doesn't fail build)

### 🚀 Quick Commands

```bash
# Format code + run logic checks (recommended workflow)
./format-code.cmd     # Windows
./format-code.sh      # Linux/Mac

# Format code only
./format-only.cmd     # Windows
./format-only.sh      # Linux/Mac

# Individual commands
./mvnw spotless:apply    # Auto-format code
./mvnw spotless:check    # Check formatting
./mvnw checkstyle:check  # Logic & complexity checks
```

### 📊 What's Automated vs Manual

| Tool              | Purpose          | Behavior                | Focus                              |
| ----------------- | ---------------- | ----------------------- | ---------------------------------- |
| 🎨 **Spotless**   | Code formatting  | ✅ **Auto-fixes**       | Style, indentation, imports        |
| 🔍 **Checkstyle** | Logic validation | ⚠️ **Reports warnings** | Complexity, naming, best practices |

> 📖 **See [`FORMATTING_SETUP.md`](FORMATTING_SETUP.md)** for complete configuration details.

## ⚙️ Configuration

### 📊 Profile Management

This application supports dual-environment configuration:

| Profile       | Database   | Security | Purpose               |
| ------------- | ---------- | -------- | --------------------- |
| 🛠️ **`dev`**  | H2 File DB | Relaxed  | Development & Testing |
| 🏭 **`prod`** | PostgreSQL | Strict   | Production Deployment |

> 📖 **See [`PROFILE_SETUP.md`](PROFILE_SETUP.md)** for detailed profile configuration.

### 🗄️ Database Management

This project uses **Flyway** for database schema versioning combined with **JPA/Hibernate** for object-relational mapping:

- **Schema Migrations**: All database changes managed through versioned SQL files in `src/main/resources/db/migration/`
- **Automatic Migration**: Flyway runs automatically on application startup
- **Schema Validation**: Hibernate validates database schema matches JPA entities (`hibernate.ddl-auto: validate`)
- **Cross-Database Support**:
  - **Development**: H2 file-based database with PostgreSQL compatibility mode
  - **Testing**: H2 in-memory database with PostgreSQL compatibility mode
  - **Production**: PostgreSQL database

#### Database Configuration by Profile

| Profile     | Database      | Connection      | Migration Support |
| ----------- | ------------- | --------------- | ----------------- |
| 🛠️ **dev**  | H2 file-based | PostgreSQL mode | ✅ Yes            |
| 🧪 **test** | H2 in-memory  | PostgreSQL mode | ✅ Yes            |
| 🏭 **prod** | PostgreSQL    | Native          | ✅ Yes            |

**Key Benefits:**

- ✅ **No local PostgreSQL required** for development
- ✅ **Persistent data** across application restarts in development
- ✅ **Shared Flyway migrations** work on both H2 and PostgreSQL
- ✅ **Fast test execution** with in-memory database
- ✅ **Production-ready** PostgreSQL configuration

> 📖 **See [`DATABASE_MANAGEMENT.md`](DATABASE_MANAGEMENT.md)** for complete database setup and migration guide.

## 🔧 Dependencies & Tech Stack

### Core Framework

- **`spring-boot-starter-web`** (3.5.5): REST API development and embedded Tomcat server
- **`spring-boot-starter-data-jpa`**: JPA integration with Hibernate for data persistence
- **`spring-boot-starter-security`**: Core authentication/authorization
- **`spring-boot-starter-oauth2-resource-server`**: JWT validation (Keycloak) via issuer discovery
- **`spring-boot-starter-validation`**: Bean validation using JSR-303 annotations

### Database

- **`h2`**: File-based database for development and testing
- **`postgresql`**: Production database driver for PostgreSQL

### Documentation & Monitoring

- **`springdoc-openapi-starter-webmvc-ui`** (2.8.11): OpenAPI 3.0 spec generation and Swagger UI
- **`spring-boot-starter-actuator`**: Production monitoring endpoints (health, metrics, info)

### Development Tools

- **`spring-boot-devtools`**: Hot reloading and development utilities
- **`lombok`**: Reduces boilerplate code (getters, setters, constructors)

### Code Quality & Formatting

- **Spotless Maven Plugin** (v2.43.0): Automatic code formatting using Google Java Format
- **Maven Checkstyle Plugin** (v3.5.0): Logic and complexity analysis (checkstyle-logic-only.xml)

### Testing

- **`spring-boot-starter-test`**: Comprehensive testing stack (JUnit 5, Mockito, AssertJ)
- **`spring-security-test`**: Security testing utilities

## Configuration Profiles

### Development Profile (`dev`) - Default

- **Database**: H2 file-based (`./data/devdb`) with PostgreSQL compatibility mode
- **Security**: Dual auth (public: Swagger/H2/`/api/v1/public/**`; secured: other `/api/**` via Basic or JWT)
- **Dev Users (Basic)**: `dev-user` / `dev-password` (ROLE_USER), `dev-admin` / `dev-password` (ROLE_ADMIN, ROLE_USER)
- **Logging**: DEBUG level for application and security
- **Features**: Hot reloading, detailed error messages, persistent data across restarts

### Production Profile (`prod`)

- **Database**: PostgreSQL (configurable via environment variables)
- **Security**: JWT-only (Keycloak). Public: `/actuator/health`, `/api/v1/public/**`; all other `/api/**` secured
- **Logging**: INFO level with security warnings
- **Features**: Optimized for production deployment

## Environment Variables

| Variable                 | Profile | Required | Default | Description               |
| ------------------------ | ------- | -------- | ------- | ------------------------- |
| `SPRING_PROFILES_ACTIVE` | Both    | No       | `dev`   | Active Spring profile     |
| `DATABASE_URL`           | prod    | Yes      | -       | PostgreSQL connection URL |
| `DATABASE_USERNAME`      | prod    | Yes      | -       | Database username         |
| `DATABASE_PASSWORD`      | prod    | Yes      | -       | Database password         |
| `SERVER_PORT`            | Both    | No       | `8080`  | Server port               |
| `KEYCLOAK_ISSUER_URI`    | prod    | Yes      | -       | Keycloak realm issuer URI |
| `KEYCLOAK_API_AUDIENCE`  | prod    | Yes      | -       | JWT audience              |

## 🔐 Auth Quick Test (dev)

Without Keycloak, use Basic Auth with built-in dev users:

```bash
curl http://localhost:8080/api/v1/public/hello
curl -u dev-user:dev-password http://localhost:8080/api/v1/hello
curl -u dev-admin:dev-password http://localhost:8080/api/v1/admin/users
```

With Keycloak running, pass a Bearer token:

```bash
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/api/v1/hello
```

> Full setup guide: `KEYCLOAK_SETUP.md`.
