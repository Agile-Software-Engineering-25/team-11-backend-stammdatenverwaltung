# Database Management with Flyway and JPA/Hibernate

## Overview

This project uses **Flyway** for database schema versioning and migrations, combined with **JPA/Hibernate** for object-relational mapping. This setup provides a robust, version-controlled approach to database management while maintaining the convenience of JPA for data access.

## Architecture

### Flyway + JPA Integration

- **Flyway**: Handles database schema versioning, migrations, and structural changes
- **JPA/Hibernate**: Manages entity mapping, relationships, and data access
- **Profile-based Configuration**: Different behaviors for development and production environments

### Key Configuration Changes

The project has been configured to use `hibernate.ddl-auto: validate` in all profiles, meaning:
- ✅ Hibernate validates that the database schema matches the entity definitions
- ❌ Hibernate will NOT automatically create/update database structures
- 🔧 All schema changes must be done through Flyway migrations

## Directory Structure

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__Create_initial_schema.sql
        ├── V2__Add_user_table.sql
        ├── V3__Add_user_indexes.sql
        └── V4__Update_user_constraints.sql
```

## Migration File Naming Convention

Flyway follows a strict naming pattern:

```
V{version}__{description}.sql
```

### Examples:
- `V1__Create_initial_schema.sql` - Initial database setup
- `V2__Add_user_table.sql` - Add new table
- `V3__Add_user_indexes.sql` - Add database indexes
- `V4__Update_user_constraints.sql` - Modify constraints
- `V5__Insert_reference_data.sql` - Add reference/master data

### Naming Rules:
- **Version**: Sequential numbers (V1, V2, V3, etc.)
- **Separator**: Double underscore `__`
- **Description**: Descriptive name with underscores (no spaces)
- **Extension**: `.sql` for SQL migrations

## Development Workflow

### 1. Creating a New Entity

When you need to add a new JPA entity:

1. **Create the JPA Entity** (in `src/main/java/.../entities/`):

```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private String email;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

2. **Create the Flyway Migration** (in `src/main/resources/db/migration/`):

```sql
-- V2__Create_users_table.sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add constraints
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);

-- Add indexes for performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

3. **Run the Migration**:

```bash
# Apply migration
./mvnw flyway:migrate

# Or start the application (auto-migration on startup)
./mvnw spring-boot:run
```

### 2. Modifying Existing Entities

When you need to modify an existing entity:

1. **Update the JPA Entity**:

```java
@Entity
@Table(name = "users")
public class User {
    // ... existing fields
    
    @Column(length = 50)
    private String firstName;  // New field
    
    @Column(length = 50)
    private String lastName;   // New field
}
```

2. **Create a New Migration**:

```sql
-- V3__Add_user_names.sql
ALTER TABLE users 
ADD COLUMN first_name VARCHAR(50),
ADD COLUMN last_name VARCHAR(50);

-- Add index if needed
CREATE INDEX idx_users_full_name ON users(first_name, last_name);
```

### 3. Adding Relationships

For entity relationships, create both the entity changes and the migration:

**Entity with Relationship**:
```java
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // ... other fields
}
```

**Migration for Relationship**:
```sql
-- V4__Create_orders_table.sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2)
);

-- Add foreign key constraint
ALTER TABLE orders 
ADD CONSTRAINT fk_orders_user_id 
FOREIGN KEY (user_id) REFERENCES users(id);

-- Add index on foreign key
CREATE INDEX idx_orders_user_id ON orders(user_id);
```

### Flyway Commands

### Spring Boot Integration (Recommended)

Spring Boot provides seamless Flyway integration that runs automatically:

```bash
# Start application (runs migrations automatically)
./mvnw spring-boot:run

# Build and test (includes migration validation)
./mvnw clean install

# Run tests (migrations run before tests)
./mvnw test
```

### Maven Commands (Alternative)

**Note**: Due to version compatibility issues with Spring Boot 3.x, the Maven plugin commands might not work reliably. Use Spring Boot integration instead.

```bash
# Apply all pending migrations (if plugin works)
./mvnw flyway:migrate

# Show migration status (if plugin works)
./mvnw flyway:info

# Validate applied migrations (if plugin works)
./mvnw flyway:validate
```

**Recommended Approach**: Use Spring Boot's automatic migration on application startup rather than manual Maven commands.

## Profile-Specific Configuration

### Development Profile (`dev`)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Only validate, don't create/update
  flyway:
    enabled: true
    baseline-on-migrate: true  # Allow migration on existing database
    validate-on-migrate: true
```

**Characteristics**:
- Uses H2 file database
- Baseline-on-migrate allows starting Flyway on existing databases
- Validation ensures schema matches entities

### Production Profile (`prod`)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Strict validation only
  flyway:
    enabled: true
    baseline-on-migrate: true
    validate-on-migrate: true
```

**Characteristics**:
- Uses PostgreSQL
- Strict validation prevents unauthorized schema changes
- All changes must go through Flyway migrations

## Best Practices

### Migration Best Practices

1. **Always Use Transactions**: Flyway automatically wraps each migration in a transaction
2. **Test Migrations**: Test migrations on a copy of production data
3. **Incremental Changes**: Make small, incremental changes rather than large modifications
4. **Rollback Strategy**: Plan for rollback scenarios (Flyway Pro feature or manual scripts)
5. **Backup Before Migration**: Always backup production databases before running migrations

### Naming Conventions

1. **Migration Files**: `V{number}__{description}.sql`
2. **Table Names**: Use snake_case (e.g., `user_profiles`)
3. **Column Names**: Use snake_case (e.g., `created_at`)
4. **Index Names**: Use descriptive prefixes (e.g., `idx_users_email`, `uk_users_username`)
5. **Constraint Names**: Use descriptive prefixes (e.g., `fk_orders_user_id`, `ck_users_age`)

### JPA Entity Guidelines

1. **Use Explicit Column Names**: `@Column(name = "created_at")`
2. **Specify Table Names**: `@Table(name = "users")`
3. **Use Appropriate Fetch Types**: `FetchType.LAZY` for relationships
4. **Add Validation Annotations**: `@NotNull`, `@Size`, etc.
5. **Use Audit Fields**: `@CreationTimestamp`, `@UpdateTimestamp`

## Common Scenarios

### Scenario 1: Adding a New Table

1. Create JPA entity
2. Create migration with CREATE TABLE
3. Add constraints and indexes
4. Run `./mvnw flyway:migrate`

### Scenario 2: Modifying Column

1. Update JPA entity
2. Create migration with ALTER TABLE
3. Consider data migration if needed
4. Run migration

### Scenario 3: Adding Reference Data

```sql
-- V5__Insert_reference_data.sql
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrator role'),
('USER', 'Standard user role'),
('MODERATOR', 'Moderator role');
```

### Scenario 4: Dropping a Column

1. Remove field from JPA entity
2. Create migration to drop column
3. Consider data backup first

```sql
-- V6__Remove_deprecated_field.sql
ALTER TABLE users DROP COLUMN old_field;
```

## Troubleshooting

### Common Issues

1. **Migration Failed**:
   ```bash
   ./mvnw flyway:repair  # Fix metadata
   ./mvnw flyway:migrate # Retry
   ```

2. **Schema Validation Error**:
   - Check if entity matches database schema
   - Verify migration was applied correctly
   - Use `./mvnw flyway:info` to check status

3. **Baseline Issues**:
   ```bash
   ./mvnw flyway:baseline  # Set starting point
   ./mvnw flyway:migrate   # Apply new migrations
   ```

### Development Tips

1. **H2 Console Access**: `http://localhost:8080/h2-console` (dev profile)
2. **Database Inspection**: Use SQL tools or H2 console to verify schema
3. **Migration Testing**: Test migrations on separate database first
4. **Rollback Planning**: Always plan how to undo changes

## Integration with CI/CD

### Build Pipeline Integration

```bash
# In CI/CD pipeline
./mvnw flyway:validate  # Validate migrations
./mvnw clean install   # Build and test
./mvnw flyway:info     # Show migration status
```

### Environment Promotion

1. **Development**: Test migrations locally
2. **Staging**: Apply and test migrations
3. **Production**: Apply validated migrations

## Security Considerations

1. **Database Credentials**: Use environment variables in production
2. **Migration Access**: Restrict migration execution to authorized users
3. **Sensitive Data**: Avoid sensitive data in migration files
4. **Backup Strategy**: Regular backups before migrations

## Summary

This setup provides:

✅ **Version-controlled database schema**  
✅ **Automated migration on application startup**  
✅ **Schema validation against JPA entities**  
✅ **Profile-specific configurations**  
✅ **Development and production database support**  
✅ **Rollback and repair capabilities**  

The combination of Flyway and JPA/Hibernate ensures that your database schema is both version-controlled and properly mapped to your Java entities, providing a robust foundation for data persistence in your Spring Boot application.
