# Stammdatenverwaltung - Master Data Management Service

A Spring Boot 3.5.7 backend service for managing master data (students, employees, lecturers, and groups) in the SAU Portal ecosystem. Built with Java 21, PostgreSQL, OAuth2/Keycloak authentication, and comprehensive API documentation.

**Repository**: [team-11-backend-stammdatenverwaltung](https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung)  
**Organization**: [Agile Software Engineering 25](https://github.com/Agile-Software-Engineering-25)

---

## 🚀 Quick Start

### Prerequisites

- **Java 21** (or later)
- **Maven 3.8+**
- **Docker** & **Docker Compose** (for containerized deployment)
- **PostgreSQL 15** (for production; H2 for development)

### Local Development Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung.git
   cd team-11-backend-stammdatenverwaltung
   ```

2. **Build the project**

   ```bash
   ./mvnw clean package
   ```

3. **Run tests**

   ```bash
   ./mvnw test
   ```

4. **Start the application**

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
   ```

5. **Access the application**
   - API Base: `http://localhost:8080/api/v1`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/api-docs`
   - Health Check: `http://localhost:8080/actuator/health`

### Docker Deployment

```bash
# Start services with Docker Compose
docker-compose up -d

# Access the running application
curl http://localhost:8080/api/v1/users
```

---

## 📋 Project Structure

```
src/main/java/com/ase/stammdatenverwaltung/
├── controllers/           # REST API endpoints
├── services/             # Business logic
├── repositories/         # Database access layer
├── entities/             # JPA entities (Person, Student, Employee, Lecturer)
├── dto/                  # Data Transfer Objects
├── mapper/               # Entity to DTO mappers
├── config/               # Spring Boot configuration classes
├── security/             # JWT and authentication handling
├── clients/              # External service clients
├── components/           # Reusable Spring components
├── constants/            # Application constants
└── model/                # Domain models

src/main/resources/
├── db/migration/         # Flyway database migrations
│   ├── common/          # Shared migrations
│   ├── h2/              # H2-specific migrations
│   └── postgresql/      # PostgreSQL-specific migrations
├── application.yaml      # Default configuration
├── application-dev.yaml  # Development profile
├── application-prod.yaml # Production profile
└── application-test.yaml # Test profile
```

---

## 🔑 Key Features

### User Management

- **Person API** (`/api/v1/users`): Manage persons (students, employees, lecturers)
  - Create students, employees, lecturers
  - Retrieve user details
  - Update user information
  - Delete users

### Group Management

- **Group API** (`/api/v1/group`): Manage student groups
  - List all student groups
  - Get group details by name
  - Retrieve group members with Keycloak integration

### Authentication & Authorization

- **OAuth2 with Keycloak**: Secure API endpoints with JWT tokens
- **JWT Token Validation**: Custom JWT converter with `UserInformationJWT` utility class for extracting user data
- **Method-Level Security**: `@PreAuthorize` annotations enforce role-based access control on endpoints
- **Enhanced Error Handling**: `RoleAwareAccessDeniedHandler` logs user roles when authorization fails for debugging
- **Keycloak Integration**: External user identity provider with role mapping
- **Supported Roles**:
  - `Area-3.Team-11.Read.*` / `Area-3.Team-11.Write.*` / `Area-3.Team-11.Delete.*`: Fine-grained resource permissions
  - `HVS-Admin`: Administrator privileges
  - `Hochschulverwaltungsmitarbeiter`: University staff access

### Examples (Development Only)

- **Example API** (`/api/v1/examples`): Demonstration endpoints
  - CRUD operations on example entities
  - Only available in `dev` profile
  - Shows best practices for controller/service implementation

### API Documentation

- **Swagger UI**: Interactive API documentation
- **OpenAPI 3.0**: Machine-readable API specification
- **Springdoc**: Automatic OpenAPI schema generation

### Database

- **Flyway**: Database migration and versioning
- **Multi-Database Support**:
  - **H2** for development (in-memory)
  - **PostgreSQL 15** for production
- **JPA/Hibernate**: ORM for entity management

---

## 🔐 Authentication & Security

### Configuration (Environment Variables)

```bash
# Keycloak Configuration
KEYCLOAK_CLIENT_SECRET=your-client-secret
SPRING_PROFILES_ACTIVE=prod

# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/stammdatenverwaltung
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-password
```

### Authorization Roles

- `Area-3.Team-11.Read.User`: Read user data
- `HVS-Admin`: Administrator privileges
- `Hochschulverwaltungsmitarbeiter`: University staff

---

## 🗄️ Database

### Supported Profiles

- **dev**: H2 in-memory database
- **prod**: PostgreSQL 15
- **test**: H2 in-memory for testing

### Migration Tool

**Flyway** handles automatic database schema migrations from `src/main/resources/db/migration/`

```bash
# Flyway configuration
flyway.locations=classpath:db/migration
flyway.baselineOnMigrate=true
flyway.validateOnMigrate=true
```

---

## 📚 API Endpoints

### Users (`/api/v1/users`)

| Method   | Endpoint           | Description              | Required Role                                        |
| -------- | ------------------ | ------------------------ | ---------------------------------------------------- |
| `GET`    | `/`                | Get all persons          | `Area-3.Team-11.Read.*` or `HVS-Admin`               |
| `GET`    | `/{id}`            | Get person by ID         | `Area-3.Team-11.Read.*` or `HVS-Admin`               |
| `POST`   | `/students`        | Create new student       | `Area-3.Team-11.Write.Student` or `HVS-Admin`        |
| `POST`   | `/employees`       | Create new employee      | `Area-3.Team-11.Write.Employee` or `HVS-Admin`       |
| `POST`   | `/lecturers`       | Create new lecturer      | `Area-3.Team-11.Write.Lecturer` or `HVS-Admin`       |
| `PUT`    | `/{id}`            | Update person            | `Area-3.Team-11.Write.*` or `HVS-Admin`              |
| `DELETE` | `/{id}`            | Delete person            | `Area-3.Team-11.Delete.*` or `HVS-Admin`             |

### Groups (`/api/v1/group`)

| Method | Endpoint       | Description       | Required Role                                        |
| ------ | -------------- | ----------------- | ---------------------------------------------------- |
| `GET`  | `/`            | Get all groups    | `Area-3.Team-11.Read.User` or `HVS-Admin`            |
| `GET`  | `/{groupName}` | Get group by name | `Area-3.Team-11.Read.Student` or `HVS-Admin`         |

### Examples (`/api/v1/examples`) - Dev Only

| Method   | Endpoint | Description       |
| -------- | -------- | ----------------- |
| `GET`    | `/`      | List all examples |
| `GET`    | `/{id}`  | Get example by ID |
| `POST`   | `/`      | Create example    |
| `PUT`    | `/{id}`  | Update example    |
| `DELETE` | `/{id}`  | Delete example    |

### Health & Monitoring

| Endpoint            | Description               |
| ------------------- | ------------------------- |
| `/actuator/health`  | Application health status |
| `/actuator/metrics` | Application metrics       |
| `/api-docs`         | OpenAPI specification     |
| `/swagger-ui.html`  | Swagger UI                |

---

## 🛠️ Build & Deployment

### Maven Build

```bash
# Full build with testing
./mvnw clean package

# Format code (Google Java Format)
./format-code.sh

# Format without building
./format-only.sh

# Run checkstyle validation
./mvnw checkstyle:check
```

### Docker Build

```bash
# Build Docker image
docker build -t stammdatenverwaltung:latest .

# Run container
docker run -e SPRING_PROFILES_ACTIVE=prod \
           -e DATABASE_URL=jdbc:postgresql://host:5432/stammdatenverwaltung \
           -e DATABASE_USERNAME=postgres \
           -e DATABASE_PASSWORD=password \
           -p 8080:8080 \
           stammdatenverwaltung:latest
```

### Kubernetes Deployment

Configuration files available in `k8s/` directory:

- `deployment.yaml`: Application deployment
- `service.yaml`: Kubernetes service
- `ingress.yaml`: Ingress routing
- `kustomization.yaml`: Kustomize overlays

---

## 📝 Code Quality Standards

### Standards Enforced

- **Google Java Format**: Automatic code formatting via Spotless
- **Checkstyle**: Logic and complexity validation
- **CLEAN Code Principles**:
  - Cohesion: Related functionality grouped together
  - Low Coupling: Minimized dependencies
  - Encapsulation: Hidden implementation details
  - Abstraction: Meaningful abstractions
  - Naming: Clear, self-documenting names

### Pre-Commit Workflow

1. Format code: `./format-code.sh`
2. Run tests: `./mvnw test`
3. Check styles: `./mvnw checkstyle:check`
4. Build: `./mvnw clean package`

---

## 🧪 Testing

### Test Framework

- **JUnit 5**: Test framework
- **Mockito**: Mocking framework
- **AssertJ**: Fluent assertions
- **Spring Boot Test**: Spring-specific testing utilities

### Run Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=UserControllerTest

# With coverage report
./mvnw test jacoco:report
```

### Test Organization

```
src/test/java/com/ase/stammdatenverwaltung/
├── controllers/   # Controller tests
├── services/      # Service tests
└── integration/   # Integration tests
```

---

## 📖 Documentation

### API Documentation

- **Swagger UI**: Available at `/swagger-ui.html` (development and production)
- **OpenAPI Spec**: Available at `/api-docs` in JSON format
- **Springdoc**: Auto-generates OpenAPI 3.0 documentation from annotations

### Development Guides

- **CONTRIBUTING.md**: Contribution guidelines
- **.github/copilot-instructions.md**: AI Assistant guidelines for development

### External Documentation

- [Spring Boot 3.5.x Docs](https://docs.spring.io/spring-boot/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

---

## 🔧 Configuration

### Application Profiles

#### Development (`dev`)

- H2 in-memory database
- Full Swagger UI enabled
- All actuator endpoints exposed
- Detailed error messages
- Dev tools enabled (hot reload)

#### Production (`prod`)

- PostgreSQL database
- Limited actuator endpoints
- JWT validation required
- Keycloak authentication enabled
- Optimized JVM settings

#### Test (`test`)

- H2 in-memory database
- Flyway migrations for tests
- Mock Keycloak setup

### Environment Variables

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=dev|prod|test

# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/stammdatenverwaltung
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=password

# Keycloak
KEYCLOAK_CLIENT_SECRET=your-secret
KEYCLOAK_SERVER_URL=https://keycloak.sau-portal.de
KEYCLOAK_REALM=sau
KEYCLOAK_CLIENT_ID=team-11
KEYCLOAK_USER_API_URL=https://sau-portal.de/userapi
```

---

## 🚦 Troubleshooting

### Common Issues

**Issue: Port 8080 already in use**

```bash
# Change port in application.yaml
server:
  port: 8081
```

**Issue: Database connection failed**

```bash
# Verify connection string and credentials
DATABASE_URL=jdbc:postgresql://localhost:5432/stammdatenverwaltung
# Check PostgreSQL is running
docker-compose ps
```

**Issue: JWT validation failures**

```bash
# Verify Keycloak is reachable
# Check KEYCLOAK_CLIENT_SECRET is set
# Verify JWT token in Authorization header: Bearer <token>
```

**Issue: Migration failure**

```bash
# Check Flyway configuration
# Verify database user has proper permissions
# Review migration files in src/main/resources/db/migration/
```

---

## 📞 Support & Contact

- **Issues**: [GitHub Issues](https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung/issues)
- **Pull Requests**: [GitHub PRs](https://github.com/Agile-Software-Engineering-25/team-11-backend-stammdatenverwaltung/pulls)
- **Team**: ASE Team 11

---

**Last Updated**: November 5, 2025  
**Project Version**: 0.0.1-SNAPSHOT  
**Java Version**: 21  
**Spring Boot**: 3.5.7
