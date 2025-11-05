# API Documentation

Comprehensive guide to the Stammdatenverwaltung REST API endpoints.

---

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

All protected endpoints require a valid OAuth2/Keycloak JWT token in the `Authorization` header:

```
Authorization: Bearer <access_token>
```

---

## Response Format

### Success Response (2xx)

```json
{
  "data": {
    /* response payload */
  },
  "timestamp": "2025-11-05T12:00:00Z",
  "status": 200
}
```

### Error Response (4xx, 5xx)

```json
{
  "error": "Detailed error message",
  "status": 400,
  "timestamp": "2025-11-05T12:00:00Z",
  "path": "/api/v1/users/invalid"
}
```

---

## Users API (`/api/v1/users`)

### 1. Get All Users

**Endpoint**: `GET /api/v1/users`

**Authorization**: `Area-3.Team-11.Read.User` or `HVS-Admin`

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `page` | integer | No | Page number (0-indexed) |
| `size` | integer | No | Page size (default: 20) |
| `type` | string | No | Filter by user type: `STUDENT`, `EMPLOYEE`, `LECTURER` |

**Example Request**:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/users?page=0&size=10&type=STUDENT"
```

**Response** (200 OK):

```json
{
  "content": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "type": "STUDENT",
      "createdAt": "2025-11-05T10:00:00Z"
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "currentPage": 0
}
```

---

### 2. Get User by ID

**Endpoint**: `GET /api/v1/users/{id}`

**Authorization**: `Area-3.Team-11.Read.User` or `HVS-Admin`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | User ID |

**Example Request**:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/users/1"
```

**Response** (200 OK):

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "type": "STUDENT",
  "studentId": "S001",
  "department": "Engineering",
  "createdAt": "2025-11-05T10:00:00Z",
  "updatedAt": "2025-11-05T11:00:00Z"
}
```

**Error Responses**:

- `404 Not Found`: User with the given ID does not exist

---

### 3. Create Student

**Endpoint**: `POST /api/v1/users/student`

**Authorization**: `HVS-Admin` or `Hochschulverwaltungsmitarbeiter`

**Request Body**:

```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "studentId": "S002",
  "department": "Computer Science",
  "enrollmentDate": "2025-10-01"
}
```

**Example Request**:

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "studentId": "S002",
    "department": "Computer Science"
  }' \
  "http://localhost:8080/api/v1/users/student"
```

**Response** (201 Created):

```json
{
  "id": 2,
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "type": "STUDENT",
  "studentId": "S002",
  "createdAt": "2025-11-05T12:00:00Z"
}
```

**Error Responses**:

- `400 Bad Request`: Invalid input (missing required fields, validation errors)
- `409 Conflict`: Email or student ID already exists

---

### 4. Create Employee

**Endpoint**: `POST /api/v1/users/employee`

**Authorization**: `HVS-Admin`

**Request Body**:

```json
{
  "firstName": "Bob",
  "lastName": "Johnson",
  "email": "bob.johnson@example.com",
  "employeeId": "E001",
  "department": "Human Resources",
  "position": "HR Manager"
}
```

**Response** (201 Created):

```json
{
  "id": 3,
  "firstName": "Bob",
  "lastName": "Johnson",
  "email": "bob.johnson@example.com",
  "type": "EMPLOYEE",
  "employeeId": "E001",
  "createdAt": "2025-11-05T12:30:00Z"
}
```

---

### 5. Create Lecturer

**Endpoint**: `POST /api/v1/users/lecturer`

**Authorization**: `HVS-Admin`

**Request Body**:

```json
{
  "firstName": "Dr.",
  "lastName": "Smith",
  "email": "dr.smith@example.com",
  "lecturerId": "L001",
  "department": "Engineering",
  "specialization": "Software Engineering"
}
```

**Response** (201 Created):

```json
{
  "id": 4,
  "firstName": "Dr.",
  "lastName": "Smith",
  "email": "dr.smith@example.com",
  "type": "LECTURER",
  "lecturerId": "L001",
  "createdAt": "2025-11-05T13:00:00Z"
}
```

---

### 6. Update User

**Endpoint**: `PUT /api/v1/users/{id}`

**Authorization**: `HVS-Admin` or owner's own profile

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | User ID to update |

**Request Body**:

```json
{
  "firstName": "John",
  "lastName": "Doe-Updated",
  "email": "john.doe.updated@example.com",
  "department": "Engineering"
}
```

**Example Request**:

```bash
curl -X PUT \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe-Updated",
    "email": "john.doe.updated@example.com"
  }' \
  "http://localhost:8080/api/v1/users/1"
```

**Response** (200 OK):

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe-Updated",
  "email": "john.doe.updated@example.com",
  "updatedAt": "2025-11-05T14:00:00Z"
}
```

**Error Responses**:

- `400 Bad Request`: Validation errors
- `404 Not Found`: User does not exist
- `409 Conflict`: Email already in use

---

### 7. Delete User

**Endpoint**: `DELETE /api/v1/users/{id}`

**Authorization**: `HVS-Admin`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | User ID to delete |

**Request Body** (optional):

```json
{
  "reason": "User graduation",
  "timestamp": "2025-11-05T14:30:00Z"
}
```

**Example Request**:

```bash
curl -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/users/1"
```

**Response** (204 No Content):

```
HTTP/1.1 204 No Content
```

**Error Responses**:

- `404 Not Found`: User does not exist
- `403 Forbidden`: Insufficient permissions

---

## Groups API (`/api/v1/group`)

### 1. Get All Groups

**Endpoint**: `GET /api/v1/group`

**Authorization**: `Area-3.Team-11.Read.User` or `HVS-Admin`

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `withDetails` | boolean | No | Include Keycloak group details (default: true) |
| `show_members` | boolean | No | Include full member list (default: true) |

**Example Request**:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/group?withDetails=true&show_members=true"
```

**Response** (200 OK):

```json
{
  "groups": [
    {
      "name": "Group-SE-01",
      "description": "Software Engineering Group 1",
      "memberCount": 25,
      "members": [
        {
          "id": 1,
          "firstName": "John",
          "lastName": "Doe",
          "email": "john.doe@example.com"
        }
      ],
      "createdAt": "2025-09-01T08:00:00Z"
    }
  ],
  "totalGroups": 5
}
```

---

### 2. Get Group by Name

**Endpoint**: `GET /api/v1/group/{groupName}`

**Authorization**: `Area-3.Team-11.Read.User` or `HVS-Admin`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `groupName` | string | Yes | Name of the group |

**Example Request**:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/group/Group-SE-01"
```

**Response** (200 OK):

```json
{
  "name": "Group-SE-01",
  "description": "Software Engineering Group 1",
  "memberCount": 25,
  "members": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "type": "STUDENT",
      "studentId": "S001"
    }
  ],
  "createdAt": "2025-09-01T08:00:00Z"
}
```

**Error Responses**:

- `404 Not Found`: Group does not exist

---

## Examples API (`/api/v1/examples`)

**Note**: Only available in `dev` profile for demonstration purposes.

### 1. Get All Examples

**Endpoint**: `GET /api/v1/examples`

**Response** (200 OK):

```json
{
  "content": [
    {
      "id": 1,
      "name": "Example 1",
      "description": "First example",
      "createdAt": "2025-11-05T10:00:00Z"
    }
  ],
  "totalElements": 10
}
```

### 2. Create Example

**Endpoint**: `POST /api/v1/examples`

**Request Body**:

```json
{
  "name": "New Example",
  "description": "Example description"
}
```

**Response** (201 Created):

```json
{
  "id": 11,
  "name": "New Example",
  "description": "Example description",
  "createdAt": "2025-11-05T12:00:00Z"
}
```

### 3. Get Example by ID

**Endpoint**: `GET /api/v1/examples/{id}`

**Response** (200 OK):

```json
{
  "id": 1,
  "name": "Example 1",
  "description": "First example",
  "createdAt": "2025-11-05T10:00:00Z"
}
```

### 4. Update Example

**Endpoint**: `PUT /api/v1/examples/{id}`

**Request Body**:

```json
{
  "name": "Updated Example",
  "description": "Updated description"
}
```

**Response** (200 OK):

```json
{
  "id": 1,
  "name": "Updated Example",
  "description": "Updated description",
  "updatedAt": "2025-11-05T14:00:00Z"
}
```

### 5. Delete Example

**Endpoint**: `DELETE /api/v1/examples/{id}`

**Response** (204 No Content):

```
HTTP/1.1 204 No Content
```

---

## Health & Monitoring

### Application Health

**Endpoint**: `GET /actuator/health`

**Response** (200 OK):

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

### Metrics

**Endpoint**: `GET /actuator/metrics`

Provides Prometheus-compatible metrics for monitoring.

---

## Error Handling

### HTTP Status Codes

| Code  | Meaning                                                |
| ----- | ------------------------------------------------------ |
| `200` | OK - Successful GET/PUT request                        |
| `201` | Created - Successful POST request                      |
| `204` | No Content - Successful DELETE request                 |
| `400` | Bad Request - Invalid input                            |
| `401` | Unauthorized - Missing/invalid token                   |
| `403` | Forbidden - Insufficient permissions                   |
| `404` | Not Found - Resource doesn't exist                     |
| `409` | Conflict - Duplicate entry or business logic violation |
| `500` | Internal Server Error - Server-side error              |

### Error Response Example

```json
{
  "timestamp": "2025-11-05T12:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Field 'email' must be a valid email address",
  "path": "/api/v1/users"
}
```

---

## Rate Limiting

Currently not implemented. Consider adding in future versions using Spring Cloud Gateway.

---

## Pagination

Endpoints support pagination via query parameters:

```
GET /api/v1/users?page=0&size=20&sort=lastName,asc
```

**Response includes**:

```json
{
  "content": [
    /* items */
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

---

## Testing the API

### Using cURL

```bash
# Get all users
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/users"

# Create a student
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com"}' \
  "http://localhost:8080/api/v1/users/student"
```

### Using Swagger UI

1. Navigate to `http://localhost:8080/swagger-ui.html`
2. Click "Authorize" button
3. Enter your Bearer token
4. Test endpoints directly from the UI

### Using Bruno (REST Client)

See `bruno-collection/` directory for pre-configured API requests.

---

## Version History

### v1 (Current)

- User CRUD operations
- Group management
- OAuth2/Keycloak authentication
- OpenAPI documentation

### Future Versions

- Pagination improvements
- Advanced filtering
- Bulk operations
- Webhooks
