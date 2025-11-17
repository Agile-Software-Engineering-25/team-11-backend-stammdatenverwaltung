# Consolidated Exception Handling and Logging Guide

## Overview

This document describes the simplified, clean, and consolidated approach for exception handling and logging in the Stammdatenverwaltung application. The system unifies all error handling across authentication, authorization, domain-specific, and general system errors into a cohesive framework.

## Architecture

### Core Components

#### 1. **ExceptionContext** (`com.ase.stammdatenverwaltung.logging.ExceptionContext`)

Encapsulates all contextual information about an exception in a structured format.

**Responsibilities:**

- Stores error metadata (error code, category, HTTP status, messages)
- Captures contextual information (user ID, endpoint, method, etc.)
- Provides builder pattern for fluent construction

**Key Fields:**

- `errorCode`: Machine-readable code (e.g., "AUTH_001", "USER_002")
- `errorCategory`: Human-readable category (e.g., "Authentication", "User Management")
- `status`: HTTP status code (HttpStatus)
- `userMessage`: User-facing message (safe for client display)
- `technicalMessage`: Technical details for logging (implementation context)
- `context`: Map of contextual key-value pairs
- `cause`: Root cause exception
- `timestamp`: When the error occurred

**Usage Example:**

```java
ExceptionContext ctx = ExceptionContext.builder()
    .errorCode("USER_001")
    .errorCategory("Profile Picture Retrieval")
    .status(HttpStatus.INTERNAL_SERVER_ERROR)
    .userMessage("Could not retrieve profile picture")
    .technicalMessage("MinIO connection timeout")
    .withContext("userId", "user-123")
    .withContext("endpoint", "/api/users/123/picture")
    .cause(minioException)
    .build();
```

#### 2. **LoggingHelper** (`com.ase.stammdatenverwaltung.logging.LoggingHelper`)

Centralized logging utility providing consistent structured logging patterns.

**Logging Levels:**

- **ERROR**: 5xx server errors (internal failures)
- **WARN**: 4xx client errors (bad requests, auth failures, not found)
- **DEBUG**: Full stack traces and detailed context

**Methods:**

- `log(ExceptionContext ctx)`: Standard logging with level selection based on HTTP status
- `logSecurity(ExceptionContext ctx, String... fieldsToOmit)`: Security-focused logging that sanitizes sensitive fields

**Key Features:**

- Automatic level selection based on HTTP status
- Field sanitization (never logs passwords, credentials, tokens)
- Truncation of long values to prevent log flooding
- Full stack traces logged at DEBUG level only

**Usage Example:**

```java
LoggingHelper.log(ctx);  // Auto-selects level
LoggingHelper.logSecurity(ctx, "password", "credentials");  // Omits sensitive fields
```

#### 3. **GlobalExceptionHandler** (`com.ase.stammdatenverwaltung.controllers.GlobalExceptionHandler`)

Unified @ControllerAdvice handling all exception types throughout the application.

**Responsibilities:**

- Intercepts and handles all exceptions
- Creates ExceptionContext for structured information
- Delegates to LoggingHelper for consistent logging
- Returns standardized JSON error responses

**Handled Exception Types:**

| Exception                              | Status    | Code           | Handler                                     |
| -------------------------------------- | --------- | -------------- | ------------------------------------------- |
| AuthenticationException                | 401       | AUTH_001       | handleAuthenticationException()             |
| BadCredentialsException                | 401       | AUTH_002       | handleBadCredentialsException()             |
| InsufficientAuthenticationException    | 401       | AUTH_003       | handleInsufficientAuthenticationException() |
| EntityNotFoundException                | 404       | NOT_FOUND_001  | handleEntityNotFoundException()             |
| ProfilePictureException (all subtypes) | 500/404\* | USER_001-003   | handleProfilePictureException()             |
| IllegalArgumentException               | 400       | VALIDATION_001 | handleIllegalArgumentException()            |
| IOException                            | 500       | IO_001         | handleIOException()                         |
| Unexpected (catch-all)                 | 500       | SYS_001        | handleGeneralException()                    |

\* Profile picture exceptions return 404 if caused by NoSuchKeyException, 500 otherwise

#### 4. **ApplicationException Interface** (`com.ase.stammdatenverwaltung.exceptions.ApplicationException`)

Contract for all domain-specific exceptions to provide structured error information.

**Required Methods:**

- `getHttpStatus()`: Returns appropriate HTTP status
- `getErrorCode()`: Returns error code for tracking
- `getErrorCategory()`: Returns category name
- `getUserMessage()`: Returns user-facing message
- `getTechnicalMessage()`: Returns technical details
- `getContextMap()`: Returns contextual metadata
- `getCause()`: Returns root cause

**Implementation Pattern:**
All domain exceptions should extend `RuntimeException` and implement `ApplicationException`:

```java
public class EntityNotFoundException extends RuntimeException implements ApplicationException {
  private final String resourceType;
  private final String resourceId;

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public String getErrorCode() {
    return "NOT_FOUND_001";
  }

  @Override
  public String getErrorCategory() {
    return "Entity Not Found";
  }

  @Override
  public String getUserMessage() {
    return "The requested " + resourceType + " does not exist";
  }

  @Override
  public String getTechnicalMessage() {
    return getMessage();
  }

  @Override
  public Map<String, String> getContextMap() {
    return Map.of("resourceType", resourceType, "resourceId", resourceId);
  }
}
```

#### 5. **ProfilePictureException Hierarchy** (Enhanced)

All ProfilePictureException subtypes now implement ApplicationException:

- `ProfilePictureRetrievalException` (USER_001) - 404 if not found, 500 otherwise
- `ProfilePictureStorageException` (USER_002) - 500
- `ProfilePictureDeletionException` (USER_003) - 404 if not found, 500 otherwise

#### 6. **Security Handlers** (Refactored)

Both security handlers now use LoggingHelper for consistent logging:

- **CustomAuthenticationEntryPoint**: Logs unauthenticated access attempts at 401 level
- **RoleAwareAccessDeniedHandler**: Logs authorization failures with user role information

### Error Response Format

All errors are returned as standardized JSON:

```json
{
  "error": "AUTH_001",
  "message": "Authentication failed",
  "details": "AuthenticationException: Access token invalid",
  "category": "Authentication",
  "path": "/api/users/profile",
  "timestamp": 1699999999000,
  "context": {
    "endpoint": "/api/users/profile",
    "method": "GET",
    "userId": "user-123"
  }
}
```

## Error Codes

### Authentication & Authorization (400s - 100s)

- **AUTH_001**: Generic authentication failure
- **AUTH_002**: Bad credentials (wrong username/password)
- **AUTH_003**: Insufficient authentication (missing/invalid token)
- **AUTHZ_001**: Authorization failure (403 Forbidden)

### Entity Management (100s - 200s)

- **NOT_FOUND_001**: Entity not found (404)

### User Management (100s - 200s)

- **USER_001**: Profile picture retrieval failed
- **USER_002**: Profile picture storage failed
- **USER_003**: Profile picture deletion failed
- **USER_004**: Generic profile picture operation failed

### Input Validation (200s - 300s)

- **VALIDATION_001**: Invalid input provided

### System Errors (300s+)

- **IO_001**: File I/O error
- **SYS_001**: Unexpected system error

## Logging Configuration

Logging levels by profile (in `application-{profile}.yaml`):

### Development (`application-dev.yaml`)

```yaml
logging:
  level:
    "[com.ase.stammdatenverwaltung]": DEBUG
    "[com.ase.stammdatenverwaltung.logging]": DEBUG
    "[com.ase.stammdatenverwaltung.security]": DEBUG
    "[org.springframework.security]": DEBUG
```

### Production (`application-prod.yaml`)

```yaml
logging:
  level:
    "[com.ase.stammdatenverwaltung]": INFO
    "[com.ase.stammdatenverwaltung.logging]": INFO
    "[com.ase.stammdatenverwaltung.security]": WARN
    "[org.springframework.security]": WARN
```

### Testing (`application-test.yaml`)

```yaml
logging:
  level:
    "[org.springframework.security]": DEBUG
    "[com.ase.stammdatenverwaltung]": DEBUG
    "[com.ase.stammdatenverwaltung.logging]": DEBUG
    "[com.ase.stammdatenverwaltung.security]": DEBUG
    "[org.flywaydb]": INFO
```

## Best Practices

### 1. Creating Domain Exceptions

Always implement ApplicationException:

```java
public class BusinessLogicException extends RuntimeException implements ApplicationException {
  private final String businessCode;

  public BusinessLogicException(String message, String businessCode) {
    super(message);
    this.businessCode = businessCode;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.UNPROCESSABLE_ENTITY; // 422
  }

  @Override
  public String getErrorCode() {
    return "BUSINESS_001";
  }

  @Override
  public String getErrorCategory() {
    return "Business Logic Violation";
  }

  @Override
  public String getUserMessage() {
    return "Operation could not be completed";
  }

  @Override
  public String getTechnicalMessage() {
    return getMessage();
  }

  @Override
  public Map<String, String> getContextMap() {
    return Map.of("businessCode", businessCode);
  }
}
```

### 2. Never Log Sensitive Information

The LoggingHelper automatically sanitizes:

- passwords, credentials, tokens, authorization headers

Use `logSecurity()` for authentication errors:

```java
LoggingHelper.logSecurity(ctx, "password", "apiKey");
```

### 3. Use Structured Context

Always add contextual information to ExceptionContext:

```java
.withContext("userId", user.getId())
.withContext("endpoint", request.getRequestURI())
.withContext("method", request.getMethod())
.withContext("orderId", order.getId())
```

### 4. Profile-Appropriate Messages

- **userMessage**: Generic, user-safe ("The requested resource does not exist")
- **technicalMessage**: Detailed, developer-safe ("User record with ID 'abc123' not found in query")

### 5. Service-Level Logging Strategy

**DO NOT log exceptions before throwing them.** Let GlobalExceptionHandler log via LoggingHelper:

#### ❌ Anti-Pattern (Duplicate Logging)

```java
try {
  storageClient.putObject(...);
} catch (Exception e) {
  log.error("Error setting profile picture for user ID: {} ({})", id, e.getClass().getSimpleName());
  log.debug("Error setting profile picture for user ID: {}", id, e);
  throw new ProfilePictureStorageException(errorMessage, id, e);
}
```

This causes duplicate log entries:
1. Service logs ERROR + DEBUG
2. GlobalExceptionHandler logs again via LoggingHelper

#### ✅ Recommended Pattern (Single Logging Point)

```java
try {
  storageClient.putObject(...);
} catch (Exception e) {
  throw new ProfilePictureStorageException(errorMessage, id, e);
}
```

GlobalExceptionHandler will:
1. Catch the exception
2. Create ExceptionContext with all metadata
3. Log once via LoggingHelper with appropriate level
4. Return standardized JSON response

#### ✅ Keep Service-Level Logs for Operations

Service-level logs are appropriate for successful operations and audit trails:

```java
public Student create(CreateStudentRequest request) {
  log.debug("Creating new student with matriculation number: {}", request.getMatriculationNumber());
  // ... business logic ...
  Student saved = studentRepository.save(student);
  log.info("Successfully created student with ID: {} and matriculation number: {}", 
           saved.getId(), saved.getMatriculationNumber());
  return saved;
}
```

These logs are **not duplicated** by the exception handler and provide valuable audit trails.

#### ✅ Graceful Degradation with WARN Logging

When a service gracefully degrades (e.g., Keycloak unavailable), log at WARN level:

```java
return keycloakClient.findUserById(person.getId())
    .onErrorResume(error -> {
      log.warn("Keycloak unavailable for person ID {}, returning basic data", person.getId());
      return Mono.just(personDtoMapper.map(person));
    });
```

This signals that enrichment failed but the operation succeeded with degraded data.

### 6. Testing

Use mock WebRequest with proper setup:

```java
WebRequest mockRequest = mock(WebRequest.class);
when(mockRequest.getDescription(false)).thenReturn("uri=/api/test");

ResponseEntity<Map<String, Object>> response = handler.handleException(ex, mockRequest);
```

## Deprecated Components

The following components are marked @Deprecated and will be removed in v1.2.0:

- `AuthenticationExceptionHandler`
- `MinIOExceptionHandler`

All their functionality is now handled by `GlobalExceptionHandler`.

## Migration Guide

### Old Code (Before)

```java
try {
  minioService.uploadPicture(file, userId);
} catch (MinIOException e) {
  log.error("Upload failed for user: {}", userId, e);
  throw new ProfilePictureStorageException("Storage failed", userId, e);
}
```

### New Code (Recommended)

No change needed! The exception is automatically caught by GlobalExceptionHandler:

```java
try {
  minioService.uploadPicture(file, userId);
} catch (MinIOException e) {
  throw new ProfilePictureStorageException("Storage failed", userId, e);
}
```

The GlobalExceptionHandler will:

1. Create an ExceptionContext with all metadata
2. Call LoggingHelper.log() for structured logging
3. Return a standardized JSON response

## Testing

Unit tests are included in `GlobalExceptionHandlerTest` and cover:

- Authentication error handling (401)
- Authorization error handling (403)
- Profile picture error handling (400/404/500)
- Input validation errors (400)
- File I/O errors (500)
- Fallback handling for unexpected errors (500)
- Response format validation
- Context map inclusion

## Summary

This consolidated approach provides:
✅ **Consistency**: All errors follow same pattern
✅ **Simplicity**: Single handler, clear flow
✅ **Security**: Automatic sensitive field sanitization
✅ **Maintainability**: Error codes and categories centralized
✅ **Debugging**: Structured logging with full context
✅ **Production-Ready**: Profile-appropriate logging levels
