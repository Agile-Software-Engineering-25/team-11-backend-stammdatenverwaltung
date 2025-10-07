# Development Quick Start Guide

> **No Setup Required! 🎉**
>
> This guide shows you how to get the Stammdatenverwaltung application running locally in under 5 minutes.

## 🚀 Quick Start

### Prerequisites
- ☕ **Java 21+** (already installed)
- 🛠️ **Maven** (included via `./mvnw`)

### Step 1: Clone and Run
```bash
# Clone the repository (if not already done)
git clone <repository-url>
cd team-11-backend-stammdatenverwaltung

# Run the application
./mvnw spring-boot:run
```

### Step 2: Verify It's Working
The application will start and show:
```
🔓 DEV MODE: Using Basic Authentication with in-memory users
   Users: dev-user/dev-password (USER), dev-admin/dev-password (ADMIN)
   No external dependencies required - perfect for local development!
Started StammdatenverwaltungApplication in 8.5 seconds
```

### Step 3: Test the API

#### Public Endpoints (No Auth Required)
```bash
curl http://localhost:8080/api/v1/public/hello
# Returns: {"message":"Hello from Stammdatenverwaltung!"}
```

#### Secured Endpoints (Basic Auth Required)
```bash
# Test with user credentials
curl -u dev-user:dev-password http://localhost:8080/api/v1/hello
# Returns: {"message":"Hello, dev-user! You have roles: [USER]"}

# Test with admin credentials
curl -u dev-admin:dev-password http://localhost:8080/api/v1/admin/users
# Returns: [] (empty array - no users in dev database)
```

#### Swagger UI
- Visit: http://localhost:8080/swagger-ui.html
- Click "Authorize" button
- Enter username: `dev-user`, password: `dev-password`
- Try any API endpoint from the Swagger interface

#### H2 Database Console
- Visit: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/devdb`
- Username: `sa`, Password: (leave empty)
- Click "Connect" to explore the database

## 🔐 Authentication Details

### Built-in Users
| Username  | Password      | Roles          | Description |
|-----------|---------------|----------------|-------------|
| dev-user  | dev-password  | USER           | Regular user for testing |
| dev-admin | dev-password  | ADMIN, USER    | Administrator for testing |

### API Access Patterns
- **Public APIs**: `/api/v1/public/**` - No authentication required
- **User APIs**: `/api/v1/user/**` - Requires USER or ADMIN role
- **Admin APIs**: `/api/v1/admin/**` - Requires ADMIN role

## 🛠️ Development Tools

### Code Quality
```bash
# Format code and check quality
./format-code.sh  # Linux/Mac
./format-code.cmd # Windows

# Individual commands
./mvnw spotless:apply    # Auto-format code
./mvnw checkstyle:check  # Logic/complexity checks
```

### Testing
```bash
# Run all tests
./mvnw test

# Run integration tests only
./mvnw integration-test

# Run with coverage
./mvnw verify
```

### Database
```bash
# Reset database (drops and recreates)
./mvnw flyway:clean flyway:migrate

# Check migration status
./mvnw flyway:info
```

## 🐛 Troubleshooting

### Application Won't Start
**Problem**: Port 8080 already in use
**Solution**:
```bash
# Find what's using port 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Kill the process or use a different port
./mvnw spring-boot:run -Dserver.port=8081
```

### Authentication Issues
**Problem**: Getting 401 Unauthorized
**Solution**: Check your credentials
```bash
# Correct format
curl -u dev-user:dev-password http://localhost:8080/api/v1/hello

# Wrong format (will fail)
curl -u "dev-user:dev-password" http://localhost:8080/api/v1/hello
```

### Database Issues
**Problem**: H2 database locked
**Solution**: Delete the database file
```bash
# Stop the application first
rm -rf data/devdb.mv.db
./mvnw spring-boot:run
```

## 📁 Project Structure

```
src/
├── main/java/com/ase/stammdatenverwaltung/
│   ├── controllers/     # REST API endpoints
│   ├── entities/        # JPA database entities
│   ├── repositories/    # Data access layer
│   ├── services/        # Business logic
│   └── config/          # Configuration classes
└── test/                # Unit and integration tests
```

## 🔄 Development Workflow

1. **Make Changes**: Edit code in your IDE
2. **Test Locally**: Run `./mvnw test`
3. **Check Quality**: Run `./format-code.sh`
4. **Restart App**: Spring Boot DevTools auto-restarts on changes
5. **Test API**: Use curl or Swagger UI

## 📚 Next Steps

- **API Documentation**: Full OpenAPI spec at `/v3/api-docs`
- **Health Checks**: Application health at `/actuator/health`
- **Metrics**: Spring Boot metrics at `/actuator/metrics`
- **Production Setup**: See `KEYCLOAK_SETUP.md` for production deployment

## ❓ Need Help?

- Check the logs in the terminal where you ran `./mvnw spring-boot:run`
- Look for error messages starting with `🔓 DEV MODE` or `ERROR`
- Verify you're using the correct credentials from the table above
- Check that no other application is using port 8080

**Happy coding! 🚀**