# Stammdatenverwaltung - Profile Configuration

This application supports two profiles: `dev` and `prod`.

## Development Profile (dev)

**Default credentials:**

- Username: `dev-user`
- Password: `dev-password`

**Features:**

- H2 in-memory database
- Relaxed security (Swagger UI accessible without authentication)
- Detailed logging
- H2 console accessible

**Running locally:**

```bash
# Default (dev profile)
./mvnw spring-boot:run

# Explicitly specify dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Access URLs:**

- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html (no auth required)
- Health: http://localhost:8080/actuator/health (no auth required)
- H2 Console: http://localhost:8080/h2-console (no auth required)

## Production Profile (prod)

**Credentials via environment variables:**

- Username: `${ADMIN_USERNAME}` (required)
- Password: `${ADMIN_PASSWORD}` (required)

**Features:**

- PostgreSQL database
- Strict security (authentication required for most endpoints)
- Production logging levels
- Environment variable configuration

**Required Environment Variables:**

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/stammdatenverwaltung
DATABASE_USERNAME=your_db_user
DATABASE_PASSWORD=your_db_password

# Security
ADMIN_USERNAME=your_admin_user
ADMIN_PASSWORD=your_secure_password

# Optional
SERVER_PORT=8080
```

**Running locally with prod profile:**

```bash
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/stammdatenverwaltung
export DATABASE_USERNAME=db_user
export DATABASE_PASSWORD=db_password
export ADMIN_USERNAME=prod-admin
export ADMIN_PASSWORD=secure-password
./mvnw spring-boot:run
```

## Docker Deployment

### Option 1: Docker Run

```bash
docker build -t stammdatenverwaltung .

docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e ADMIN_USERNAME=admin \
  -e ADMIN_PASSWORD=your-secure-password \
  -e DATABASE_URL=jdbc:postgresql://your-db-host:5432/stammdatenverwaltung \
  -e DATABASE_USERNAME=db_user \
  -e DATABASE_PASSWORD=db_password \
  stammdatenverwaltung
```

### Option 2: Docker Compose

```bash
# Copy the example environment file and customize it
cp .env.example .env

# Edit .env with your production values
# Then run:
docker-compose up -d
```

### Option 3: Docker with Dev Profile

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  stammdatenverwaltung
```

## Security Differences

### Development (dev)

- Swagger UI: ✅ Public access
- H2 Console: ✅ Public access
- Health endpoint: ✅ Public access
- API endpoints: ✅ Public access
- Other actuator endpoints: 🔒 Requires authentication

### Production (prod)

- Swagger UI: 🔒 Requires authentication
- Health endpoint: ✅ Public access
- All other endpoints: 🔒 Requires authentication

## Environment Variable Reference

| Variable                 | Required  | Default    | Description             |
| ------------------------ | --------- | ---------- | ----------------------- |
| `SPRING_PROFILES_ACTIVE` | No        | `dev`      | Active Spring profile   |
| `DATABASE_URL`           | Prod only | -          | Database connection URL |
| `DATABASE_USERNAME`      | Prod only | -          | Database username       |
| `DATABASE_PASSWORD`      | Prod only | -          | Database password       |
| `ADMIN_USERNAME`         | Prod only | -          | Admin username          |
| `ADMIN_PASSWORD`         | Prod only | -          | Admin password          |
| `SERVER_PORT`            | No        | `8080`     | Server port             |
