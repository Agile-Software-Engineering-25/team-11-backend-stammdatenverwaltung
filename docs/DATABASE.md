# Database Schema and Migrations

Guide to the database structure, migrations, and management strategies.

---

## Database Overview

### Supported Databases

| Environment | Database       | Version |
| ----------- | -------------- | ------- |
| Development | H2 (in-memory) | Latest  |
| Production  | PostgreSQL     | 15+     |
| Testing     | H2 (in-memory) | Latest  |

---

## Entity Diagram

```
┌─────────────────────────────────────────────────────────┐
│                      PERSON                             │
│─────────────────────────────────────────────────────────│
│ id (PK)                                                 │
│ first_name (VARCHAR)                                    │
│ last_name (VARCHAR)                                     │
│ email (VARCHAR, UNIQUE)                                 │
│ phone (VARCHAR, NULL)                                   │
│ created_at (TIMESTAMP)                                  │
│ updated_at (TIMESTAMP)                                  │
│ person_type (VARCHAR) [DISCRIMINATOR]                   │
└─────────────────────────────────────────────────────────┘
        ▲                ▲                ▲
        │                │                │
   Inheritance       Inheritance     Inheritance
        │                │                │
        │                │                │
   ┌────────┐      ┌──────────┐      ┌────────┐
   │STUDENT │      │EMPLOYEE  │      │LECTURER│
   ├────────┤      ├──────────┤      ├────────┤
   │student_│      │employee_ │      │lecturer│
   │id      │      │id        │      │_id     │
   │matric# │      │dept      │      │dept    │
   └────────┘      └──────────┘      └────────┘


┌──────────────────────────────────┐
│            GROUP                 │
├──────────────────────────────────┤
│ id (PK)                          │
│ name (VARCHAR, UNIQUE)           │
│ description (TEXT, NULL)         │
│ created_at (TIMESTAMP)           │
│ updated_at (TIMESTAMP)           │
└──────────────────────────────────┘
           │
           │ One-to-Many
           │
┌──────────────────────────────────┐
│     GROUP_MEMBER (Join Table)    │
├──────────────────────────────────┤
│ group_id (FK)                    │
│ person_id (FK)                   │
│ joined_at (TIMESTAMP)            │
└──────────────────────────────────┘
```

---

## Table Schemas

### PERSON (Base Table)

**Purpose**: Core user information with inheritance

```sql
CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    person_type VARCHAR(31) NOT NULL,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_person_email ON person(email);
CREATE INDEX idx_person_person_type ON person(person_type);
```

**Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | BIGSERIAL | No | Primary key |
| `first_name` | VARCHAR(100) | No | User first name |
| `last_name` | VARCHAR(100) | No | User last name |
| `email` | VARCHAR(255) | No | Email address (unique) |
| `phone` | VARCHAR(20) | Yes | Phone number |
| `created_at` | TIMESTAMP | No | Creation timestamp |
| `updated_at` | TIMESTAMP | No | Last update timestamp |
| `person_type` | VARCHAR(31) | No | Discriminator: STUDENT, EMPLOYEE, LECTURER |
| `version` | BIGINT | Yes | Optimistic lock version |

---

### STUDENT (Type-Specific Table)

**Purpose**: Student-specific attributes

```sql
CREATE TABLE student (
    id BIGINT PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    matric_number VARCHAR(50),
    department VARCHAR(100),
    enrollment_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE INDEX idx_student_id ON student(student_id);
CREATE INDEX idx_student_matric ON student(matric_number);
```

**Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | BIGINT | No | Foreign key to PERSON |
| `student_id` | VARCHAR(50) | No | Unique student identifier |
| `matric_number` | VARCHAR(50) | Yes | Matriculation number |
| `department` | VARCHAR(100) | Yes | Department name |
| `enrollment_date` | DATE | Yes | Enrollment date |
| `status` | VARCHAR(20) | Yes | ACTIVE, INACTIVE, GRADUATED |

---

### EMPLOYEE (Type-Specific Table)

```sql
CREATE TABLE employee (
    id BIGINT PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
    employee_id VARCHAR(50) UNIQUE NOT NULL,
    department VARCHAR(100),
    position VARCHAR(100),
    employment_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE INDEX idx_employee_id ON employee(employee_id);
```

**Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | BIGINT | No | Foreign key to PERSON |
| `employee_id` | VARCHAR(50) | No | Unique employee identifier |
| `department` | VARCHAR(100) | Yes | Department name |
| `position` | VARCHAR(100) | Yes | Job position |
| `employment_date` | DATE | Yes | Employment start date |
| `status` | VARCHAR(20) | Yes | ACTIVE, INACTIVE, RETIRED |

---

### LECTURER (Type-Specific Table)

```sql
CREATE TABLE lecturer (
    id BIGINT PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
    lecturer_id VARCHAR(50) UNIQUE NOT NULL,
    department VARCHAR(100),
    specialization VARCHAR(200),
    office_location VARCHAR(100),
    office_hours VARCHAR(500)
);

CREATE INDEX idx_lecturer_id ON lecturer(lecturer_id);
```

**Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | BIGINT | No | Foreign key to PERSON |
| `lecturer_id` | VARCHAR(50) | No | Unique lecturer identifier |
| `department` | VARCHAR(100) | Yes | Department name |
| `specialization` | VARCHAR(200) | Yes | Area of specialization |
| `office_location` | VARCHAR(100) | Yes | Office room/building |
| `office_hours` | VARCHAR(500) | Yes | Office hours schedule |

---

### GROUP (Table)

```sql
CREATE TABLE "group" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_group_name ON "group"(name);
```

**Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | BIGSERIAL | No | Primary key |
| `name` | VARCHAR(100) | No | Group name (unique) |
| `description` | TEXT | Yes | Group description |
| `created_at` | TIMESTAMP | No | Creation timestamp |
| `updated_at` | TIMESTAMP | No | Last update timestamp |
| `created_by` | BIGINT | Yes | Creator user ID |
| `version` | BIGINT | Yes | Optimistic lock version |

---

### GROUP_MEMBER (Join Table)

```sql
CREATE TABLE group_member (
    group_id BIGINT NOT NULL REFERENCES "group"(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES person(id) ON DELETE CASCADE,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, person_id)
);

CREATE INDEX idx_group_member_person ON group_member(person_id);
```

**Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `group_id` | BIGINT | No | Foreign key to GROUP |
| `person_id` | BIGINT | No | Foreign key to PERSON |
| `joined_at` | TIMESTAMP | No | When person joined group |

---

### EXAMPLE (Demo Table)

**Purpose**: Example entity for development (dev profile only)

```sql
CREATE TABLE example (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_example_name ON example(name);
```

---

## Flyway Migrations

### File Structure

```
src/main/resources/db/migration/
├── common/
│   ├── V1__Init_Person_Tables.sql
│   ├── V2__Create_Group_Tables.sql
│   └── V3__Add_Indexes.sql
├── h2/
│   └── V100__H2_Specific_Setup.sql
└── postgresql/
    └── V100__PostgreSQL_Specific_Setup.sql
```

### Migration Naming Convention

**Format**: `V###__Description.sql`

- `V`: Flyway prefix
- `###`: Version number (zero-padded, e.g., 001, 002, 100)
- `__`: Double underscore separator
- `Description`: Human-readable description

**Examples**:

- `V1__Initial_schema.sql`
- `V2__Add_users_table.sql`
- `V3__Create_groups_table.sql`

### Create New Migration

1. **Create file** with version number:

   ```bash
   touch src/main/resources/db/migration/common/V4__Add_audit_columns.sql
   ```

2. **Write SQL migration**:

   ```sql
   -- V4__Add_audit_columns.sql
   ALTER TABLE person ADD COLUMN audit_log TEXT;
   ALTER TABLE "group" ADD COLUMN audit_log TEXT;
   ```

3. **Run application** to apply migration:
   ```bash
   ./mvnw spring-boot:run
   ```

### Rollback Strategy

Flyway does not support rollbacks by default. Instead:

1. **Create undo migration** (not executed):

   ```sql
   -- V4__Add_audit_columns.sql (original)
   ALTER TABLE person ADD COLUMN audit_log TEXT;

   -- U4__Undo_Add_audit_columns.sql (not applied)
   ALTER TABLE person DROP COLUMN audit_log;
   ```

2. **For production rollbacks**:
   - Coordinate with database team
   - Create new forward migration that corrects the issue
   - Maintain migration history for audit trail

---

## Indexes and Performance

### Primary Indexes (Automatically Created)

- All PRIMARY KEY constraints
- All UNIQUE constraints
- All FOREIGN KEY constraints

### Secondary Indexes (Performance Optimization)

```sql
-- Speed up user lookups by email
CREATE INDEX idx_person_email ON person(email);

-- Speed up type-based filtering
CREATE INDEX idx_person_person_type ON person(person_type);

-- Speed up group lookups
CREATE INDEX idx_group_name ON "group"(name);

-- Speed up member lookups within group
CREATE INDEX idx_group_member_person ON group_member(person_id);
```

### Index Strategy

**When to add indexes**:

- Columns used in WHERE clauses
- Columns used in JOIN conditions
- Columns frequently sorted (ORDER BY)
- Foreign key columns

**When NOT to add indexes**:

- Columns with low cardinality (mostly same values)
- Columns rarely queried
- Small tables (< 1000 rows)

---

## Database Configuration by Profile

### Development (H2)

```yaml
# application-dev.yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/mydb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: validate
  h2:
    console:
      enabled: true
      path: /h2-console
```

**Characteristics**:

- File-based H2 database
- Web console accessible at `/h2-console`
- Fast startup
- Good for local testing

### Production (PostgreSQL)

```yaml
# application-prod.yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
```

**Connection String Format**:

```
jdbc:postgresql://host:5432/stammdatenverwaltung
```

---

## Database Maintenance

### Backup

```bash
# PostgreSQL backup
pg_dump -U postgres -h localhost stammdatenverwaltung > backup.sql

# Restore
psql -U postgres -h localhost stammdatenverwaltung < backup.sql
```

### Health Check

```bash
# Via API
curl http://localhost:8080/actuator/health

# Direct database connection
psql -U postgres -h localhost -d stammdatenverwaltung -c "SELECT 1;"
```

### Monitor Connections

```sql
-- PostgreSQL: View active connections
SELECT pid, usename, application_name, state
FROM pg_stat_activity
WHERE datname = 'stammdatenverwaltung';

-- PostgreSQL: Terminate connection
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'stammdatenverwaltung' AND pid != pg_backend_pid();
```

---

## Troubleshooting

### Migration Failed

**Symptom**: Application won't start with migration error

**Solution**:

1. Check migration file syntax
2. Review Flyway logs: `./mvnw spring-boot:run | grep Flyway`
3. Verify database user permissions
4. Check if flyway schema table exists

### Connection Refused

**Symptom**: `Connection refused` error

**Solution**:

1. Verify PostgreSQL is running: `docker-compose ps`
2. Check connection string in configuration
3. Verify credentials are correct
4. Check firewall (port 5432 must be open)

### Duplicate Key Error

**Symptom**: Cannot insert record, duplicate key violation

**Solution**:

1. Check for duplicate values in UNIQUE columns
2. Reset sequence: `ALTER SEQUENCE person_id_seq RESTART WITH 1;`
3. Delete duplicate records if appropriate

---

## Query Examples

### Find Student by Email

```sql
SELECT p.*, s.*
FROM person p
LEFT JOIN student s ON p.id = s.id
WHERE p.email = 'student@example.com'
AND p.person_type = 'STUDENT';
```

### List All Groups with Member Count

```sql
SELECT g.id, g.name, g.description, COUNT(gm.person_id) as member_count
FROM "group" g
LEFT JOIN group_member gm ON g.id = gm.group_id
GROUP BY g.id, g.name, g.description
ORDER BY member_count DESC;
```

### Find Groups for a Student

```sql
SELECT g.*
FROM "group" g
JOIN group_member gm ON g.id = gm.group_id
JOIN person p ON gm.person_id = p.id
WHERE p.email = 'student@example.com';
```

### Count Users by Type

```sql
SELECT
    person_type,
    COUNT(*) as count
FROM person
GROUP BY person_type
ORDER BY count DESC;
```

---

## Performance Considerations

### Connection Pooling

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
```

### Query Performance

- Use specific SELECT columns instead of SELECT \*
- Add WHERE clauses to limit result set
- Use pagination for large datasets
- Add appropriate indexes

### Caching (Future)

Redis can be added for frequently accessed groups/users to reduce database load.
