# Architecture Overview

## System Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────┐
│                    Client / Frontend                     │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP/REST
                         ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot Application                     │
│  ┌───────────────────────────────────────────────────┐  │
│  │  REST Controllers (UserController, GroupController) │  │
│  └───────────────────────────────────────────────────┘  │
│                         │                                │
│  ┌───────────────────────────────────────────────────┐  │
│  │        Security Layer (JWT, OAuth2)               │  │
│  │  - JwtAuthConverter                               │  │
│  │  - SecurityConfig                                 │  │
│  │  - PreAuthorize annotations                       │  │
│  └───────────────────────────────────────────────────┘  │
│                         │                                │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Service Layer (Business Logic)                  │  │
│  │  - PersonService, StudentService                  │  │
│  │  - EmployeeService, LecturerService               │  │
│  │  - GroupService                                   │  │
│  └───────────────────────────────────────────────────┘  │
│                         │                                │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Data Access Layer                               │  │
│  │  - Repositories (JPA Spring Data)                 │  │
│  │  - Entity Mappers & DTOs                          │  │
│  └───────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  PostgreSQL  │  │  Keycloak    │  │  H2 (Dev)    │
│  Database    │  │  OAuth2      │  │  Database    │
└──────────────┘  └──────────────┘  └──────────────┘
```

---

## Layered Architecture

### 1. **Controller Layer** (`controllers/`)

**Responsibility**: HTTP request handling and routing

- **UserController**: Manages students, employees, lecturers

  - POST endpoints for creating users
  - GET endpoints for retrieving user details
  - PUT endpoints for updates
  - DELETE endpoints for removal
  - OpenAPI documentation annotations

- **GroupController**: Manages student groups
  - GET all groups with optional member details
  - GET group by name
  - Integration with Keycloak for detailed information

**Design Pattern**: RESTful principles with standardized response formats

---

### 2. **Service Layer** (`services/`)

**Responsibility**: Business logic implementation

- **PersonService**: Base user operations

  - User retrieval, creation, updates
  - Common validation logic
  - Cross-cutting concerns

- **StudentService**: Student-specific logic

  - Student creation with validation
  - Student-specific queries

- **EmployeeService**: Employee operations

  - Employee management
  - Role-based operations

- **LecturerService**: Lecturer management

  - Lecturer-specific business rules

- **GroupService**: Group management
  - Group aggregation
  - Member list curation
  - Keycloak integration for enrichment

**Design Pattern**: Single Responsibility - each service handles one domain entity

---

### 3. **Repository Layer** (`repositories/`)

**Responsibility**: Database access and abstraction

Extends `JpaRepository<T, ID>` for CRUD operations:

- `PersonRepository`: Base person queries
- `StudentRepository`: Student-specific queries
- `EmployeeRepository`: Employee queries
- `LecturerRepository`: Lecturer queries

**Benefits**:

- Automatic CRUD implementation
- Custom query method definitions
- Database vendor independence

---

### 4. **Entity Layer** (`entities/`)

**Responsibility**: Domain model and persistence

**Class Hierarchy**:

```
Person (base entity)
  ├── Student
  ├── Employee
  └── Lecturer
```

**Key Features**:

- JPA annotations for ORM mapping
- Jakarta Validation annotations
- Immutable design with Lombok `@Value` or mutable with `@Data`
- Inheritance strategy: Single table or joined table

---

### 5. **DTO Layer** (`dto/`)

**Responsibility**: Data transfer between layers

**Types**:

- **Request DTOs**: `CreateStudentRequest`, `CreateEmployeeRequest`, `UpdateUserRequest`
- **Response DTOs**: `PersonDetailsDTO`, `GroupResponseDTO`, `GroupDTO`

**Benefits**:

- API contract isolation
- Validation constraints
- Request/response transformation

---

### 6. **Mapper Layer** (`mapper/`)

**Responsibility**: Entity ↔ DTO conversions

- **UpdateUserMapper**: Converts DTOs to entity updates
- Custom mapping logic without external frameworks
- Preserves immutability and encapsulation

---

### 7. **Security Layer** (`security/`)

**Responsibility**: Authentication and authorization

- **SecurityConfig**: Spring Security configuration

  - OAuth2 resource server setup with Keycloak JWT validation
  - Profile-specific rules (dev/test/prod)
  - CORS and CSRF configuration
  - Authorization rules per endpoint pattern

- **JwtAuthConverter**: Converts JWT tokens to Spring Security authorities

  - Extracts client roles from Keycloak `groups` and `realm_access` claims
  - Maps to Spring `GrantedAuthority` for `@PreAuthorize` evaluation

- **UserInformationJWT**: Utility class for JWT claims extraction

  - Static methods to access user ID, email, username, first/last names from current authentication
  - Role retrieval from multiple JWT claim sources (groups, realm_access, resource_access)
  - `hasRole(String role)` method for programmatic role checking
  - Thread-safe access via `SecurityContextHolder`

- **RoleAwareAccessDeniedHandler**: Custom 403 access denied handler

  - Logs authorization failures with role information for debugging
  - Extracts expected roles from `@PreAuthorize` exception messages
  - Provides JSON error response with 403 Forbidden status
  - Helps troubleshoot why specific users cannot access endpoints

**Role-Based Access Control**:

- Endpoints use `@PreAuthorize` annotations for method-level security
- Two authorization patterns:

  1. **Static Role Checks** (create/list endpoints):

     ```java
     @PreAuthorize("hasRole('Area-3.Team-11.Write.Student') or hasRole('sau-admin') ...")
     ```

     Directly checks user roles for coarse-grained operations

  2. **Dynamic Type-Based Checks** (single user operations):
     ```java
     @PreAuthorize("@personService.canAccessUser(#userId, 'Read') or hasRole('sau-admin') ...")
     ```
     Calls `PersonService.canAccessUser()` which:
     - Fetches the person's type from database (Student/Employee/Lecturer)
     - Dynamically constructs required role: `Area-3.Team-11.{permission}.{type}`
     - Checks if user has that role via `UserInformationJWT.hasRole()`
     - Enables fine-grained authorization based on actual data

- Supported role formats:
  - `Area-3.Team-11.Read.Student`, `Area-3.Team-11.Write.Employee`, etc. (fine-grained permissions)
  - `sau-admin` (administrator access)
  - `university-administrative-staff` (university staff)

---

### 8. **Configuration Layer** (`config/`)

**Responsibility**: Application setup and beans

- **SecurityConfig**: Spring Security beans
- **WebClientConfig**: WebClient for reactive HTTP calls
- **WebConfig**: CORS, interceptors
- **OpenApiConfig**: Swagger/Springdoc configuration
- **KeycloakConfigProperties**: External property binding
- **JwtConfigurationValidator**: Validates JWT setup
- **DataInitializer**: Test data seeding (dev profile)

---

## Data Flow Example: Creating a Student

```
1. HTTP POST /api/v1/users/student
   ├─ Request Body: CreateStudentRequest
   └─ Authorization Header: Bearer <JWT>

2. Controller Layer (UserController)
   ├─ @PreAuthorize checks roles
   ├─ @Valid validates CreateStudentRequest
   └─ Calls StudentService.createStudent()

3. Service Layer (StudentService)
   ├─ Validates business rules
   ├─ Creates Student entity
   └─ Calls StudentRepository.save()

4. Repository Layer (StudentRepository)
   ├─ Converts entity to SQL INSERT
   └─ Executes database transaction

5. Database
   ├─ Inserts student record
   └─ Returns generated ID

6. Response Mapping
   ├─ Entity converted to PersonDetailsDTO
   └─ HTTP 201 Created with DTO payload

7. Client receives:
   {
     "id": 123,
     "firstName": "John",
     "lastName": "Doe",
     "email": "john@example.com",
     "type": "STUDENT",
     "studentId": "S001"
   }
```

---

## Design Patterns Used

### 1. **Dependency Injection**

- Constructor-based injection with `@RequiredArgsConstructor`
- Loose coupling between components
- Easy testing with mocks

### 2. **Repository Pattern**

- Spring Data JPA abstracts database access
- Vendor-independent queries
- Automatic transaction management

### 3. **Service Locator / Facade**

- Services provide unified interface to controllers
- Simplify complex business logic
- Reusable across multiple endpoints

### 4. **Data Transfer Object (DTO)**

- Decouple API contract from internal representation
- Hide sensitive entity fields
- Validate input/output data

### 5. **Strategy Pattern**

- Different user types (Student, Employee, Lecturer)
- Handled through inheritance and polymorphism

### 6. **Interceptor Pattern**

- Security filters for authorization
- Request/response logging
- Error handling

---

## Database Design

### Entity Relationships

```
Person (parent table)
├─ student_table (inherits from Person)
├─ employee_table (inherits from Person)
└─ lecturer_table (inherits from Person)

Group
└─ has many Members (through join table)
```

### Inheritance Strategy

- **Single Table Inheritance** (default): All types in one table with discriminator column
- Alternative: **Joined Table Inheritance**: Separate tables with foreign keys

---

## Security Architecture

### Authentication Flow

```
1. Client obtains JWT from Keycloak
2. Client sends JWT in Authorization header
3. Spring Security validates JWT signature
4. JwtAuthConverter extracts authorities from token
5. @PreAuthorize checks if user has required role
6. Request proceeds to endpoint
```

### Authorization Levels

- **ROLE_Area-3.Team-11.Read.User**: Read-only access
- **ROLE_sau-admin**: Full administrative access
- **ROLE_university-administrative-staff**: University staff access

---

## Technology Stack

| Component         | Technology             | Version      |
| ----------------- | ---------------------- | ------------ |
| **Framework**     | Spring Boot            | 3.5.7        |
| **JDK**           | Eclipse Temurin        | Java 21      |
| **Build Tool**    | Maven                  | 3.8+         |
| **ORM**           | JPA/Hibernate          | Jakarta 3.1+ |
| **Database**      | PostgreSQL/H2          | 15/Latest    |
| **Security**      | Spring Security OAuth2 | 6.3+         |
| **API Docs**      | Springdoc-OpenAPI      | 2.8.11       |
| **Validation**    | Jakarta Validation     | 3.0+         |
| **Logging**       | SLF4J/Logback          | 2.x          |
| **Testing**       | JUnit 5, Mockito       | 5.x          |
| **Container**     | Docker                 | Latest       |
| **Orchestration** | Kubernetes             | 1.24+        |
| **Code Format**   | Google Java Format     | Latest       |
| **Code Quality**  | Checkstyle             | Latest       |

---

## Deployment Models

### 1. **Local Development**

- H2 in-memory database
- Hot reload with DevTools
- All endpoints available
- Mock authentication

### 2. **Docker Compose**

- PostgreSQL in separate container
- Application in separate container
- Network isolation
- Environment variable configuration

### 3. **Kubernetes**

- Declarative deployment
- Service discovery
- Load balancing via Ingress
- ConfigMaps for configuration
- Secrets for sensitive data

---

## Performance Considerations

### Caching

- Response caching not yet implemented
- Opportunity for future optimization

### Lazy Loading

- JPA entities configured with appropriate fetch strategies
- N+1 query prevention

### Connection Pooling

- HikariCP (Spring Boot default)
- Configurable pool size based on load

### Logging

- Structured logging with SLF4J
- Performance impact minimized in production

---

## Monitoring & Observability

### Actuator Endpoints

- `/actuator/health`: Application status
- `/actuator/metrics`: Performance metrics
- `/actuator/beans`: Registered beans
- `/actuator/env`: Environment properties

### Logging

- Structured logs with correlation IDs (future enhancement)
- Separate log levels for dev/prod
- Log aggregation-ready format

---

## Future Enhancements

1. **Caching Layer**: Redis for frequently accessed groups/users
2. **Event-Driven Architecture**: Kafka/RabbitMQ for async operations
3. **Audit Trail**: Track all user modifications
4. **Pagination**: Cursor-based pagination for large datasets
5. **Search**: Elasticsearch for advanced user search
6. **Monitoring**: Prometheus/Grafana integration
7. **Distributed Tracing**: Jaeger/Sleuth for request tracking
