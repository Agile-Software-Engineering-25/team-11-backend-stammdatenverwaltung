# Rancher Deployment Guide

This document describes how to deploy the Stammdatenverwaltung service to the SoSoS Rancher cluster.

## Prerequisites

- Access to the Rancher dashboard at https://rancher.sau-portal.de/dashboard/c/local/
- Team 11 namespace: `ase-11`
- Database credentials provided by Team 15
- Keycloak configuration details

## Deployment Architecture

The application is deployed using:

- **Container Registry**: GitHub Container Registry (ghcr.io)
- **Orchestration**: Kubernetes via Rancher
- **Ingress Controller**: Traefik
- **Database**: PostgreSQL (shared cluster database)
- **Authentication**: Keycloak (OAuth2 Resource Server)

## Setup Steps

### 1. GitHub Secrets and Variables

Configure the following in your repository settings (Settings → Secrets and variables → Actions):

#### Secrets

- **KUBECONFIG**: Your team's kubeconfig from Rancher
  - Go to https://rancher.sau-portal.de/dashboard/c/local/
  - Copy your team's kubeconfig
  - Add as repository secret `KUBECONFIG`

#### Variables

- **K8S_NAMESPACE**: `ase-11`

### 2. Create Kubernetes Secrets

Create the required secrets in your namespace. You can do this via Rancher UI or kubectl:

#### PostgreSQL Credentials Secret

```bash
kubectl -n ase-11 create secret generic postgres-secret \
  --from-literal=POSTGRES_USER=ase-11 \
  --from-literal=POSTGRES_PASSWORD='<your-password>'
```

**Note**: Initial credentials are provided by Team 15. Store them securely.

#### Keycloak Configuration Secret

```bash
kubectl -n ase-11 create secret generic keycloak-secret \
  --from-literal=KEYCLOAK_ISSUER_URI='<your-keycloak-issuer-uri>' \
  --from-literal=KEYCLOAK_JWK_SET_URI='<your-keycloak-jwk-set-uri>'
```

**Example values** (adjust for your environment):

- `KEYCLOAK_ISSUER_URI`: `https://keycloak.example.com/realms/your-realm`
- `KEYCLOAK_JWK_SET_URI`: `https://keycloak.example.com/realms/your-realm/protocol/openid-connect/certs`

### 3. Verify Kubernetes Configuration

The k8s directory contains the deployment manifests:

- **deployment.yaml**: Defines the Pod with resource limits, health checks, and environment variables
- **service.yaml**: Exposes the application internally on port 8080
- **ingress.yaml**: Routes external traffic from `sau-portal.de/team-11-api` to the service
- **kustomization.yaml**: Orchestrates the deployment with proper labels and namespace

Key configuration details:

- **Namespace**: `ase-11`
- **Replicas**: 1
- **Image**: `ghcr.io/agile-software-engineering-25/team-11-backend-stammdatenverwaltung:latest`
- **Port**: 8080
- **Health Check**: `/actuator/health`
- **Ingress Path**: `/team-11-api` (rewritten to `/` by Traefik)

### 4. Database Connection

The application connects to the shared PostgreSQL database:

- **Host**: `postgres.db` (internal cluster DNS)
- **Port**: 5432
- **Database**: `appdb`
- **Schema**: `ase-11_schema` (automatically used by your credentials)

Connection details are injected via the `postgres-secret` Kubernetes secret.

### 5. Automatic Deployment

The GitHub Actions workflow (`.github/workflows/build-publish-deploy.yml`) automatically:

1. **On every push to main**:

   - Builds the Docker image
   - Publishes to GitHub Container Registry
   - Deploys to the Kubernetes cluster

2. **On pull requests**:
   - Builds the Docker image (not published)
   - Skips deployment

## Deployment Workflow

```
Push to main
    ↓
GitHub Actions triggers
    ↓
Build Docker image
    ↓
Publish to ghcr.io
    ↓
Deploy to ase-11 namespace
    ↓
Traefik routes traffic from sau-portal.de/team-11-api
```

## Accessing the Application

Once deployed, the application is accessible at:

```
https://sau-portal.de/team-11-api
```

### Health Check

```
https://sau-portal.de/team-11-api/actuator/health
```

### Swagger UI (if enabled in prod profile)

```
https://sau-portal.de/team-11-api/swagger-ui.html
```

## Monitoring and Troubleshooting

### View Deployment Status

```bash
kubectl -n ase-11 get deployments
kubectl -n ase-11 get pods
kubectl -n ase-11 describe pod <pod-name>
```

### View Logs

```bash
kubectl -n ase-11 logs -f deployment/ase-stammdaten
```

### Check Secrets

```bash
kubectl -n ase-11 get secrets
kubectl -n ase-11 describe secret postgres-secret
```

### Verify Ingress

```bash
kubectl -n ase-11 get ingress
kubectl -n ase-11 describe ingress ase-stammdaten
```

## Environment Variables

The application uses the following environment variables in production:

| Variable                 | Source          | Purpose                      |
| ------------------------ | --------------- | ---------------------------- |
| `SPRING_PROFILES_ACTIVE` | deployment.yaml | Activates `prod` profile     |
| `DATABASE_URL`           | deployment.yaml | PostgreSQL connection string |
| `DATABASE_USERNAME`      | postgres-secret | Database user (ase-11)       |
| `DATABASE_PASSWORD`      | postgres-secret | Database password            |
| `KEYCLOAK_ISSUER_URI`    | keycloak-secret | Keycloak token issuer        |
| `KEYCLOAK_JWK_SET_URI`   | keycloak-secret | Keycloak public key endpoint |

## Important Notes

### Context Path Rewriting

The Ingress uses Traefik's `rewrite-target: /` annotation, which rewrites requests from `/team-11-api` to `/`. The application receives requests at the root path and does NOT set `server.servlet.context-path`.

If you need to change this behavior:

1. Remove the `rewrite-target` annotation from `ingress.yaml`
2. Set `SERVER_SERVLET_CONTEXT_PATH=/team-11-api` in deployment.yaml

### Database Migrations

- Flyway automatically applies migrations on startup
- Migrations are located in `src/main/resources/db/migration/`
- The prod profile uses `hibernate.ddl-auto: validate` (no auto-schema updates)

### Resource Limits

Current resource allocation:

- **Requests**: 256Mi memory, 100m CPU
- **Limits**: 1Gi memory, 300m CPU

Contact Team 15 if you need more resources.

## Troubleshooting Common Issues

### Pod fails to start

1. Check logs: `kubectl -n ase-11 logs <pod-name>`
2. Verify secrets exist: `kubectl -n ase-11 get secrets`
3. Check resource availability: `kubectl -n ase-11 describe pod <pod-name>`

### Database connection errors

1. Verify `postgres-secret` exists and has correct credentials
2. Test connection from a shell pod in the cluster
3. Check that your schema `ase-11_schema` exists in the database

### Keycloak authentication fails

1. Verify `keycloak-secret` has correct URIs
2. Check that the Keycloak instance is accessible from the cluster
3. Verify JWT token configuration in the application

### Ingress not working

1. Check ingress status: `kubectl -n ase-11 describe ingress ase-stammdaten`
2. Verify Traefik is running: `kubectl -n kube-system get pods | grep traefik`
3. Check DNS resolution for `sau-portal.de`

## Support

For issues related to:

- **Rancher/Kubernetes**: Contact Team 15
- **Database access**: Contact Team 15
- **Application code**: Contact Team 11
