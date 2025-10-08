# 🔧 Stammdatenverwaltung - Profile Configuration

> Comprehensive guide for configuring development and production environments with different security levels, databases, and access controls.

This application supports two distinct profiles optimized for different deployment scenarios:

## 🛠️ Development Profile (`dev`)

### 🔐 Default Credentials (Basic Auth for dev)

| Field        | Value          |
| ------------ | -------------- |
| **Username** | `dev-user`     |
| **Password** | `dev-password` |

Additional admin user for admin-only endpoints:

| Field        | Value          |
| ------------ | -------------- |
| **Username** | `dev-admin`    |
| **Password** | `dev-password` |

### ✨ Features

- 🗄️ **H2 File Database**: Persistent data stored in `./data/mydb.mv.db`
- 🔓 **Relaxed Security**: Swagger/H2 and `/api/v1/public/**` are public. All other `/api/**` require Basic (dev users) or JWT
- 📋 **Detailed Logging**: Enhanced debugging information
- 🛠️ **H2 Console**: Direct database access and inspection
- 🚀 **Hot Reload**: Spring Boot DevTools enabled

### 🚀 Running Locally

```bash
# 🎯 Default (dev profile)
./mvnw spring-boot:run

# 🔧 Explicitly specify dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 🌐 Access URLs

| Service             | URL                                   | Authentication |
| ------------------- | ------------------------------------- | -------------- |
| 🏠 **Application**  | http://localhost:8080                 | ❌ None        |
| 📖 **Swagger UI**   | http://localhost:8080/swagger-ui.html | ❌ None        |
| ❤️ **Health Check** | http://localhost:8080/actuator/health | ❌ None        |
| 🗄️ **H2 Console**   | http://localhost:8080/h2-console      | ❌ None        |

## 🏭 Production Profile (`prod`)

### 🔐 Authentication

Production uses Keycloak JWT authentication (OAuth2 Resource Server). No Basic Auth users are configured.

### 🔒 Features

- 🐘 **PostgreSQL Database**: Enterprise-grade relational database
- 🔐 **Strict Security**: JWT required for all `/api/**` except `/api/v1/public/**` and `/actuator/health`
- 📊 **Production Logging**: INFO-level logging for performance
- ⚙️ **Environment Configuration**: All settings via environment variables
- 🛡️ **Enhanced Security**: No default credentials, explicit authentication

### 🌍 Required Environment Variables

| Category     | Variable            | Description                | Example                                                 |
| ------------ | ------------------- | -------------------------- | ------------------------------------------------------- |
| **Database** | `DATABASE_URL`      | PostgreSQL connection URL  | `jdbc:postgresql://localhost:5432/stammdatenverwaltung` |
|              | `DATABASE_USERNAME` | Database user              | `stammdaten_user`                                       |
|              | `DATABASE_PASSWORD` | Database password          | `secure_db_password`                                    |
| **Security** | `KEYCLOAK_ISSUER_URI`   | Keycloak realm issuer URI    | `https://keycloak.sau-portal.de/realms/sau`             |
|              | `KEYCLOAK_JWK_SET_URI` | JWK set URI for JWT verification | `https://keycloak.sau-portal.de/realms/sau/protocol/openid-connect/certs` |
| **Optional** | `SERVER_PORT`       | Application port           | `8080`                                                  |

### 🚀 Running in Production Mode

```bash
# 🔧 Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/stammdatenverwaltung
export DATABASE_USERNAME=db_user
export DATABASE_PASSWORD=db_password
export KEYCLOAK_ISSUER_URI=https://your-keycloak/realms/stammdatenverwaltung
export KEYCLOAK_API_AUDIENCE=stammdatenverwaltung-api

# 🚀 Start application
./mvnw spring-boot:run
```

### 🌐 Access URLs

| Service             | URL                                   | Authentication |
| ------------------- | ------------------------------------- | -------------- |
| 🏠 **Application**  | http://localhost:8080                 | ✅ JWT (Keycloak) |
| 📖 **Swagger UI**   | http://localhost:8080/swagger-ui.html | ✅ JWT (Keycloak) |
| ❤️ **Health Check** | http://localhost:8080/actuator/health | ❌ None        |

## 🐳 Docker Deployment

### Option 1: Docker Run

```bash
# 🏗️ Build image
docker build -t stammdatenverwaltung .

docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://your-db-host:5432/stammdatenverwaltung \
  -e DATABASE_USERNAME=db_user \
  -e DATABASE_PASSWORD=db_password \
  -e KEYCLOAK_ISSUER_URI=https://your-keycloak/realms/stammdatenverwaltung \
  -e KEYCLOAK_API_AUDIENCE=stammdatenverwaltung-api \
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
- Public API: ✅ `/api/v1/public/**`
- Secured API: 🔒 Other `/api/**` require Basic (dev users) or JWT
- Other actuator endpoints: 🔒 Requires authentication

### Production (prod)

- Swagger UI: 🔒 Requires authentication
- Health endpoint: ✅ Public access
- Public API: ✅ `/api/v1/public/**`
- All other `/api/**`: 🔒 Requires JWT (Keycloak)

## Environment Variable Reference

| Variable                 | Required  | Default | Description             |
| ------------------------ | --------- | ------- | ----------------------- |
| `SPRING_PROFILES_ACTIVE` | No        | `dev`   | Active Spring profile   |
| `DATABASE_URL`           | Prod only | -       | Database connection URL |
| `DATABASE_USERNAME`      | Prod only | -       | Database username       |
| `DATABASE_PASSWORD`      | Prod only | -       | Database password       |
| `KEYCLOAK_ISSUER_URI`    | Prod only | -       | Keycloak issuer URI     |
| `KEYCLOAK_JWK_SET_URI`   | Prod only | -       | JWK set URI for JWT verification |
| `SERVER_PORT`            | No        | `8080`  | Server port             |
