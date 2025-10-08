# Keycloak Authentication Setup Guide

This document provides comprehensive instructions for setting up Keycloak authentication with the Stammdatenverwaltung Spring Boot application.

## Overview

The application now supports OAuth2 JWT authentication via Keycloak, providing:
- JWT token validation with automatic signature verification
- Role-based access control (RBAC) using Keycloak roles from the `groups` claim
- Profile-specific configuration (dev/prod)
- Backward compatibility with Basic Auth in development

## Keycloak Configuration

### Step 1: Keycloak Server Setup

1. **Install and Start Keycloak**
   ```bash
   # Using Docker (recommended for development)
   docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
     quay.io/keycloak/keycloak:25.0 start-dev
   
   # Or download and run locally
   # https://www.keycloak.org/downloads
   ```

2. **Access Keycloak Admin Console**
   - URL: http://localhost:8080
   - Username: admin
   - Password: admin

### Step 2: Realm Configuration

1. **Create Realm**
   - Create a new realm named `stammdatenverwaltung`
   - Or use the default `master` realm (not recommended for production)

2. **Configure Realm Settings**
   - Go to Realm Settings → General
   - Set appropriate display name and description
   - Configure login settings as needed

### Step 3: Client Configuration

#### 3.1 Create API Client (Resource Server)

1. **Create Client for API**
   - Client ID: `stammdatenverwaltung-api`
   - Client Type: OpenID Connect
   - Client authentication: OFF (public client for API identification)
   - Authorization: OFF
   - Standard flow: OFF
   - Direct access grants: OFF
   - Service accounts roles: OFF

2. **Configure Client Roles**
   - Go to Clients → stammdatenverwaltung-api → Roles
   - Create roles: `USER`, `ADMIN`

#### 3.2 Create Frontend/SPA Client (Optional)

1. **Create Client for Frontend**
   - Client ID: `stammdatenverwaltung-frontend`
   - Client Type: OpenID Connect
   - Client authentication: OFF (public client for SPA)
   - Standard flow: ON (Authorization Code)
   - Direct access grants: OFF
   - Valid redirect URIs: `http://localhost:3000/*` (adjust for your frontend)
   - Web origins: `http://localhost:3000` (adjust for your frontend)

#### 3.3 Create Machine-to-Machine Client (Optional)

1. **Create Client for Backend Services**
   - Client ID: `stammdatenverwaltung-service`
   - Client Type: OpenID Connect
   - Client authentication: ON (confidential client)
   - Authorization: OFF
   - Standard flow: OFF
   - Direct access grants: OFF
   - Service accounts roles: ON (Client Credentials flow)

### Step 4: User and Role Configuration

#### 4.1 Create Realm Roles (Alternative to Client Roles)

1. **Create Realm Roles**
   - Go to Realm Roles
   - Create roles: `USER`, `ADMIN`

#### 4.2 Create Test Users

1. **Create Admin User**
   - Username: `admin`
   - Email: `admin@example.com`
   - First Name: `Admin`
   - Last Name: `User`
   - Email Verified: ON
   - Set password in Credentials tab (temporary: OFF)

2. **Create Regular User**
   - Username: `user`
   - Email: `user@example.com`
   - First Name: `Regular`
   - Last Name: `User`
   - Email Verified: ON
   - Set password in Credentials tab (temporary: OFF)

#### 4.3 Assign Roles to Users

1. **Assign Roles**
   - Go to Users → [username] → Role mapping
   - Assign realm roles: `ADMIN` to admin user, `USER` to regular user
   - Or assign client roles from `stammdatenverwaltung-api`

### Step 5: Groups Claim Configuration

The application expects roles to be included in the JWT token's `groups` claim. Configure Keycloak to include user groups in the JWT token:

#### 5.1 Groups Mapper

1. **Add Groups Mapper**
   - Go to Clients → stammdatenverwaltung-frontend → Client scopes → Dedicated scope
   - Add mapper:
     - Name: `groups`
     - Mapper Type: `Group Membership`
     - Token Claim Name: `groups`
     - Add to access token: ON
     - Full group path: OFF (use group name only)

#### 5.2 Alternative: Realm Roles to Groups

If you prefer to use realm roles instead of groups:

1. **Add Realm Roles Mapper**
   - Go to Client scopes → Create client scope: `realm-roles`
   - Add mapper:
     - Name: `realm-roles`
     - Mapper Type: `User Realm Role`
     - Token Claim Name: `groups`
     - Add to access token: ON

2. **Assign Client Scope**
   - Go to Clients → stammdatenverwaltung-frontend → Client scopes
   - Add the `realm-roles` scope as Optional

## Spring Boot Application Configuration

### Environment Variables

#### Development (application-dev.yaml)
```yaml
KEYCLOAK_ISSUER_URI: https://keycloak.sau-portal.de/realms/sau
KEYCLOAK_JWK_SET_URI: https://keycloak.sau-portal.de/realms/sau/protocol/openid-connect/certs
```

#### Production (application-prod.yaml)
```yaml
KEYCLOAK_ISSUER_URI: https://your-keycloak-server.com/realms/stammdatenverwaltung
KEYCLOAK_JWK_SET_URI: https://your-keycloak-server.com/realms/stammdatenverwaltung/protocol/openid-connect/certs
```

### Testing Authentication

#### 1. Get Access Token via REST Client

**Authorization Code Flow (for SPAs):**
```bash
# Step 1: Get authorization code (browser)
https://localhost:8080/realms/stammdatenverwaltung/protocol/openid-connect/auth?client_id=stammdatenverwaltung-frontend&redirect_uri=http://localhost:3000/callback&response_type=code&scope=openid

# Step 2: Exchange code for token
curl -X POST "http://localhost:8080/realms/stammdatenverwaltung/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "client_id=stammdatenverwaltung-frontend" \
  -d "code=YOUR_AUTHORIZATION_CODE" \
  -d "redirect_uri=http://localhost:3000/callback"
```

**Direct Access (for testing only):**
```bash
curl -X POST "http://localhost:8080/realms/stammdatenverwaltung/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=stammdatenverwaltung-frontend" \
  -d "username=admin" \
  -d "password=admin"
```

#### 2. Test API Endpoints

```bash
# Test public endpoint (no token required)
curl http://localhost:8081/api/v1/public/hello

# Test protected endpoint
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  http://localhost:8081/api/v1/hello

# Test admin endpoint
curl -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  http://localhost:8081/api/v1/admin/users

# Test user endpoint
curl -H "Authorization: Bearer YOUR_USER_TOKEN" \
  http://localhost:8081/api/v1/user/profile

# Debug token information
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  http://localhost:8081/api/v1/token-info
```

## Troubleshooting

### Common Issues

1. **Token Validation Fails**
   - Check issuer URL is correct and accessible
   - Verify token is not expired
   - Ensure token includes correct audience claim

2. **Role Authorization Fails**
   - Verify roles/groups are correctly assigned to users
   - Check groups mapper is configured properly
   - Ensure groups appear in token `groups` claim

3. **Token Validation Fails**
   - Check issuer URL is correct and accessible
   - Verify JWK set URI is correct and accessible
   - Ensure token is not expired

### Debug Information

1. **Token Claims Structure**
   ```json
   {
     "exp": 1640995200,
     "iat": 1640994600,
     "iss": "https://keycloak.sau-portal.de/realms/sau",
     "sub": "user-uuid",
     "preferred_username": "admin",
     "email": "admin@example.com",
     "groups": ["admin", "user"],
     "scope": "openid profile email"
   }
   ```

2. **Spring Security Debug Logging**
   - Add to application-dev.yaml:
     ```yaml
     logging:
       level:
         org.springframework.security: DEBUG
         org.springframework.security.oauth2: DEBUG
     ```

## Security Considerations

1. **Production Deployment**
   - Use HTTPS for all Keycloak communications
   - Configure proper CORS settings
   - Use strong passwords and enable MFA
   - Regularly rotate signing keys

2. **Token Security**
   - Set appropriate token expiration times
   - Implement token refresh mechanism
   - Store tokens securely on client side

3. **Role Management**
   - Follow principle of least privilege
   - Regularly audit user roles and permissions
   - Implement proper role hierarchy if needed

## Integration with Frontend

For frontend integration, consider using:
- **React**: `@react-keycloak/web`
- **Angular**: `keycloak-angular`
- **Vue.js**: `@dsb-norge/vue-keycloak-js`

These libraries handle token management, automatic refresh, and route protection automatically.