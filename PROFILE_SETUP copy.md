# 🔧 Stammdatenverwaltung - Profile Configuration

> Comprehensive guide for configuring development and production environments with different security levels, databases, and access controls.

This application supports two distinct profiles optimized for different deployment scenarios:

## 🛠️ Development Profile (`dev`)

### 🔐 Default Credentials

| Field        | Value          |
| ------------ | -------------- |
| **Username** | `dev-user`     |
| **Password** | `dev-password` |

### ✨ Features

- 🗄️ **H2 File Database**: Persistent data stored in `./data/mydb.mv.db`
- 🔓 **Relaxed Security**: Swagger UI accessible without authentication
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

### 🔐 Environment-Based Credentials

| Variable         | Purpose                    | Required |
| ---------------- | -------------------------- | -------- |
| `ADMIN_USERNAME` | Application admin username | ✅ Yes   |
| `ADMIN_PASSWORD` | Application admin password | ✅ Yes   |

### 🔒 Features

- 🐘 **PostgreSQL Database**: Enterprise-grade relational database
- 🔐 **Strict Security**: Authentication required for all endpoints except health
- 📊 **Production Logging**: INFO-level logging for performance
- ⚙️ **Environment Configuration**: All settings via environment variables
- 🛡️ **Enhanced Security**: No default credentials, explicit authentication

### 🌍 Required Environment Variables

| Category     | Variable            | Description                | Example                                                 |
| ------------ | ------------------- | -------------------------- | ------------------------------------------------------- |
| **Database** | `DATABASE_URL`      | PostgreSQL connection URL  | `jdbc:postgresql://localhost:5432/stammdatenverwaltung` |
|              | `DATABASE_USERNAME` | Database user              | `stammdaten_user`                                       |
|              | `DATABASE_PASSWORD` | Database password          | `secure_db_password`                                    |
| **Security** | `ADMIN_USERNAME`    | Application admin username | `admin`                                                 |
|              | `ADMIN_PASSWORD`    | Application admin password | `your_secure_password`                                  |
| **Optional** | `SERVER_PORT`       | Application port           | `8080`                                                  |

### 🚀 Running in Production Mode

```bash
# 🔧 Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/stammdatenverwaltung
export DATABASE_USERNAME=db_user
export DATABASE_PASSWORD=db_password
export ADMIN_USERNAME=prod-admin
export ADMIN_PASSWORD=secure-password

# 🚀 Start application
./mvnw spring-boot:run
```

### 🌐 Access URLs

| Service             | URL                                   | Authentication |
| ------------------- | ------------------------------------- | -------------- |
| 🏠 **Application**  | http://localhost:8080                 | ✅ Required    |
| 📖 **Swagger UI**   | http://localhost:8080/swagger-ui.html | ✅ Required    |
| ❤️ **Health Check** | http://localhost:8080/actuator/health | ❌ None        |

## 🐳 Docker Deployment

### Option 1: Docker Run

```bash
# 🏗️ Build image
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

| Variable                 | Required  | Default | Description             |
| ------------------------ | --------- | ------- | ----------------------- |
| `SPRING_PROFILES_ACTIVE` | No        | `dev`   | Active Spring profile   |
| `DATABASE_URL`           | Prod only | -       | Database connection URL |
| `DATABASE_USERNAME`      | Prod only | -       | Database username       |
| `DATABASE_PASSWORD`      | Prod only | -       | Database password       |
| `ADMIN_USERNAME`         | Prod only | -       | Admin username          |
| `ADMIN_PASSWORD`         | Prod only | -       | Admin password          |
| `SERVER_PORT`            | No        | `8080`  | Server port             |
