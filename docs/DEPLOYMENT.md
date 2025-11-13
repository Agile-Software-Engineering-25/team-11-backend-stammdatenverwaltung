# Deployment & Operations Guide

Comprehensive guide for deploying, operating, and monitoring the Stammdatenverwaltung application.

---

## 📦 Deployment Environments

### Development (Local)

- **Database**: H2 in-memory
- **Port**: 8080
- **Authentication**: JWT (can be mocked)
- **Use Case**: Local development and testing

### Staging (Docker Compose)

- **Database**: PostgreSQL container
- **Port**: 8080
- **Authentication**: Keycloak integration
- **Use Case**: Pre-production testing

### Production (Kubernetes)

- **Database**: PostgreSQL (external/managed)
- **Port**: 8080
- **Authentication**: Keycloak production realm
- **Use Case**: Live deployment

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
# Build image with default tag
docker build -t stammdatenverwaltung:latest .

# Build with specific version tag
docker build -t stammdatenverwaltung:1.0.0 .

# Build with full registry path (for publishing)
docker build -t registry.example.com/stammdatenverwaltung:1.0.0 .
```

### Multi-Stage Build

The `Dockerfile` uses multi-stage builds to optimize image size:

```dockerfile
# Stage 1: Builder
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
# Copy pom and build application

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Run Docker Container

```bash
# Basic execution
docker run -d \
  --name stammdatenverwaltung \
  -p 8080:8080 \
  stammdatenverwaltung:latest

# With environment variables
docker run -d \
  --name stammdatenverwaltung \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/stammdatenverwaltung \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=secure_password \
  -e KEYCLOAK_CLIENT_SECRET=secret_key \
  stammdatenverwaltung:latest

# With volume mounting
docker run -d \
  --name stammdatenverwaltung \
  -p 8080:8080 \
  -v /data/logs:/app/logs \
  -v /data/config:/app/config \
  stammdatenverwaltung:latest
```

### View Logs

```bash
# Real-time logs
docker logs -f stammdatenverwaltung

# Last 100 lines
docker logs --tail=100 stammdatenverwaltung

# With timestamps
docker logs -f --timestamps stammdatenverwaltung
```

---

## 🐳 Docker Compose Deployment

### Quick Start

```bash
# Start all services
docker-compose up -d

# View service status
docker-compose ps

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Clean up volumes
docker-compose down -v
```

### Configuration

Create `.env` file in project root:

```bash
# Database
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_secure_password

# Keycloak
KEYCLOAK_CLIENT_SECRET=your_keycloak_secret

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

### Service Architecture

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:15-alpine
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/stammdatenverwaltung
```

---

## ☸️ Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (1.24+)
- `kubectl` CLI installed
- Persistent volume support
- ConfigMap and Secrets for sensitive data

### Deployment Files

Located in `k8s/` directory:

```
k8s/
├── deployment.yaml      # Application deployment
├── service.yaml         # Service configuration
├── ingress.yaml         # Ingress routing
├── local.yaml          # Local development overlay
└── kustomization.yaml  # Kustomize configuration
```

### Deploy to Kubernetes

```bash
# Deploy using kubectl
kubectl apply -f k8s/

# Deploy specific resource
kubectl apply -f k8s/deployment.yaml

# Deploy using Kustomize
kubectl apply -k k8s/

# Check deployment status
kubectl get deployments stammdatenverwaltung
kubectl get pods -l app=stammdatenverwaltung
```

### Deployment YAML Structure

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: stammdatenverwaltung
spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: app
          image: stammdatenverwaltung:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: DATABASE_URL
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: database-url
            - name: KEYCLOAK_CLIENT_SECRET
              valueFrom:
                secretKeyRef:
                  name: app-secrets
                  key: keycloak-secret
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 5
```

### Configure Secrets

The application requires several Kubernetes Secrets to be configured before deployment. Create them in your cluster:

```bash
# Create secret for database (PostgreSQL)
kubectl create secret generic postgres-secret \
  --from-literal=POSTGRES_USER=postgres \
  --from-literal=POSTGRES_PASSWORD=YOUR_DB_PASSWORD

# Create secret for Keycloak (OAuth2)
kubectl create secret generic keycloak-secret \
  --from-literal=KEYCLOAK_ISSUER_URI=https://keycloak.sau-portal.de/realms/sau \
  --from-literal=KEYCLOAK_JWK_SET_URI=https://keycloak.sau-portal.de/realms/sau/protocol/openid-connect/certs \
  --from-literal=KEYCLOAK_CLIENT_SECRET=YOUR_KEYCLOAK_CLIENT_SECRET

# Create secret for MinIO (object storage)
kubectl create secret generic minio-secret \
  --from-literal=MINIO_SECRET=YOUR_MINIO_SECRET_KEY

# Create secret for Bitfrost (message broker)
# This secret is required for user deletion notifications
kubectl create secret generic bitfrost-secret \
  --from-literal=BITFROST_PROJECT_SECRET=YOUR_BITFROST_PROJECT_SECRET

# View all secrets
kubectl get secrets

# Verify specific secrets
kubectl describe secret postgres-secret
kubectl describe secret keycloak-secret
kubectl describe secret minio-secret
kubectl describe secret bitfrost-secret
```

#### Bitfrost Configuration Details

The Bitfrost integration is optional but recommended for notifying other services about user deletions:

- **Purpose**: Publishes user deletion events to the Bitfrost message broker
- **Secret Resource**: `bitfrost-secret`
- **Secret Key**: `BITFROST_PROJECT_SECRET`
- **Graceful Degradation**: If the secret is not configured or blank, notifications are skipped with debug logging, and the application continues operating normally
- **Configuration Property**: `bitfrost.project-secret` (set from `BITFROST_PROJECT_SECRET` environment variable)

If you do not have access to a Bitfrost instance, simply omit the `bitfrost-secret` creation, and the application will operate without this integration.

### Service Exposure

```bash
# Port forward for local testing
kubectl port-forward svc/stammdatenverwaltung 8080:8080

# Access via port forward
curl http://localhost:8080/api/v1/users

# Get service details
kubectl describe svc stammdatenverwaltung
```

### Scaling

```bash
# Scale to 5 replicas
kubectl scale deployment stammdatenverwaltung --replicas=5

# Auto-scaling (requires metrics-server)
kubectl autoscale deployment stammdatenverwaltung --min=2 --max=10 --cpu-percent=80
```

---

## 🔍 Monitoring & Observability

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health status
curl http://localhost:8080/actuator/health/details
```

**Response**:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### Metrics

```bash
# List available metrics
curl http://localhost:8080/actuator/metrics

# Get specific metric (JVM memory)
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Prometheus format
curl http://localhost:8080/actuator/prometheus
```

### Logging

**Location**: Depends on configuration

- Docker: `docker logs container_id`
- Docker Compose: `docker-compose logs service_name`
- Kubernetes: `kubectl logs pod_name`
- File: Check `application-prod.yaml` for `logging.file`

**Log Levels** (in `application-prod.yaml`):

```yaml
logging:
  level:
    root: INFO
    com.ase.stammdatenverwaltung: DEBUG
    org.springframework.security: INFO
```

---

## 🔄 Continuous Integration

### GitHub Actions (Recommended)

Create `.github/workflows/build.yml`:

```yaml
name: Build and Test

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up Java 21
        uses: actions/setup-java@v3
        with:
          java-version: "21"
          distribution: "temurin"

      - name: Build with Maven
        run: ./mvnw clean package

      - name: Run Tests
        run: ./mvnw test

      - name: Check Code Style
        run: ./mvnw checkstyle:check
```

### GitLab CI (Alternative)

Create `.gitlab-ci.yml`:

```yaml
stages:
  - build
  - test
  - deploy

build:
  stage: build
  script:
    - ./mvnw clean package
  artifacts:
    paths:
      - target/

test:
  stage: test
  script:
    - ./mvnw test
    - ./mvnw checkstyle:check
```

---

## 🚀 Blue-Green Deployment

### Strategy

1. Deploy new version to "Green" environment
2. Test green environment
3. Switch load balancer to green
4. Keep blue as rollback

### Implementation

```bash
# Deploy to green
kubectl set image deployment/app-green \
  app=stammdatenverwaltung:v2

# Wait for rollout
kubectl rollout status deployment/app-green

# Switch traffic to green (via service selector)
kubectl patch service stammdatenverwaltung -p \
  '{"spec":{"selector":{"version":"green"}}}'

# If rollback needed
kubectl patch service stammdatenverwaltung -p \
  '{"spec":{"selector":{"version":"blue"}}}'
```

---

## 🔄 Rolling Update

```bash
# Kubernetes automatically does rolling updates
kubectl set image deployment/stammdatenverwaltung \
  app=stammdatenverwaltung:v2.0.0 \
  --record

# Check rollout history
kubectl rollout history deployment/stammdatenverwaltung

# Rollback to previous version
kubectl rollout undo deployment/stammdatenverwaltung
```

---

## 📋 Backup & Disaster Recovery

### Database Backup

```bash
# PostgreSQL backup
pg_dump -U postgres -h host.example.com \
  stammdatenverwaltung > backup_$(date +%Y%m%d).sql

# Compressed backup
pg_dump -U postgres -h host.example.com \
  stammdatenverwaltung | gzip > backup_$(date +%Y%m%d).sql.gz
```

### Restore Database

```bash
# From uncompressed backup
psql -U postgres -h host.example.com \
  stammdatenverwaltung < backup_20251105.sql

# From compressed backup
gunzip -c backup_20251105.sql.gz | \
  psql -U postgres -h host.example.com stammdatenverwaltung
```

### Automated Backups (Cron)

```bash
# Add to crontab
0 2 * * * pg_dump -U postgres -h localhost stammdatenverwaltung | \
  gzip > /backups/db_backup_$(date +\%Y\%m\%d).sql.gz

# Keep only last 7 days
0 3 * * * find /backups -name "db_backup_*.sql.gz" -mtime +7 -delete
```

---

## 🔐 Security Considerations

### Environment Variables

**Never commit secrets to version control!**

```bash
# ✅ Good - Use environment variables
export DATABASE_PASSWORD="secure_password"
export KEYCLOAK_CLIENT_SECRET="secret_key"
./mvnw spring-boot:run

# ✅ Good - Use .env file (git ignored)
cat > .env << EOF
DATABASE_PASSWORD=secure_password
KEYCLOAK_CLIENT_SECRET=secret_key
EOF

# ❌ Bad - Hardcoded in code
public static final String PASSWORD = "secure_password"; // ✗ NEVER!
```

### Kubernetes Secrets

```bash
# Create sealed secrets (GitOps friendly)
echo -n 'secure_password' | kubectl create secret generic \
  app-secrets --dry-run=client --from-file=password=/dev/stdin \
  -o yaml | kubeseal -o yaml > sealed-secret.yaml
```

### HTTPS/TLS

```yaml
# Enable HTTPS in Spring Boot
server:
  port: 443
  ssl:
    key-store: classpath:keystore.jks
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: JKS
    key-alias: tomcat
```

---

## 📊 Performance Tuning

### JVM Heap Size

```bash
# In docker-compose.yml or deployment
environment:
  - JAVA_OPTS=-Xms512m -Xmx2g

# In docker run
docker run -e JAVA_OPTS="-Xms512m -Xmx2g" stammdatenverwaltung:latest
```

### Database Connection Pool

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Caching (Future Enhancement)

```yaml
spring:
  cache:
    type: redis
    redis:
      host: localhost
      port: 6379
```

---

## 🆘 Troubleshooting

### Application Won't Start

```bash
# Check logs for errors
docker logs -f container_id

# Common issues:
# 1. Port already in use: Change port in docker-compose
# 2. Database not reachable: Check postgres container is running
# 3. Migration failed: Check Flyway logs

# Solution: Run in debug mode
docker run -e JAVA_OPTS="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=y" \
  stammdatenverwaltung:latest
```

### Database Connection Failed

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check database exists
docker exec postgres_container psql -U postgres -l

# Check credentials
docker-compose logs postgres

# Test connection
docker exec postgres_container psql -U postgres -d stammdatenverwaltung -c "SELECT 1"
```

### High Memory Usage

```bash
# Check JVM heap settings
docker stats stammdatenverwaltung

# Increase heap size if needed
# In docker-compose.yml:
environment:
  JAVA_OPTS: "-Xmx4g"

# Restart application
docker restart stammdatenverwaltung
```

### Slow Requests

```bash
# Check database slow logs
docker exec postgres_container psql -U postgres stammdatenverwaltung -c \
  "SELECT query, mean_time FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10"

# Check application metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Enable detailed logging
# In application.yaml:
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

## 📈 Scaling Considerations

### Horizontal Scaling

- Add more instances behind load balancer
- Ensure stateless design (no local files/memory)
- Use external session storage if needed

### Vertical Scaling

- Increase JVM heap size
- Increase database connection pool
- Use faster hardware/CPU

### Database Scaling

- Read replicas for read-heavy workloads
- Connection pooling/pgBouncer
- Query optimization and indexing

---

## 🚨 Incident Response

### Critical Issue Process

1. **Identify** - Review logs and metrics
2. **Isolate** - Take affected instances out of rotation
3. **Mitigate** - Deploy hotfix or rollback previous version
4. **Restore** - Bring services back online gradually
5. **Analyze** - Review root cause and implement preventative measures

### Quick Rollback

```bash
# Kubernetes rollback
kubectl rollout undo deployment/stammdatenverwaltung

# Docker Compose rollback
docker-compose down
git checkout previous_version
docker-compose up -d

# Manual restart
docker restart stammdatenverwaltung
```

---

## 📞 Support & Escalation

- **Minor Issues**: Create GitHub issue
- **Production Issues**: Page on-call engineer
- **Security Issues**: Contact security team immediately
- **Questions**: Contact team lead or senior developer

---

**Last Updated**: November 5, 2025  
**Document Version**: 1.0  
**Status**: Active
