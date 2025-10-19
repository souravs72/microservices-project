# Admin User Setup Guide

## Overview

This guide explains how the admin user is automatically initialized in the microservices platform and how to access it.

## Admin User Details

### Credentials

- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@example.com`
- **Role**: `ADMIN`

### Automatic Initialization

The admin user is automatically created when the services start up through the following components:

#### 1. Auth Service (`auth-service/src/main/java/com/microservices/auth/config/DataInitializer.java`)

- Creates the admin user in the authentication database
- Encrypts the password using BCrypt
- Assigns both `ROLE_ADMIN` and `ROLE_USER` roles
- Only creates the user if it doesn't already exist

#### 2. User Service (`user-service/src/main/java/com/microservices/userservice/config/DataInitializer.java`)

- Creates the admin user profile in the user management database
- Syncs with the auth service to maintain consistency
- Only creates the profile if it doesn't already exist

### Configuration

The admin user creation can be controlled via environment variables in `docker-compose.yml`:

```yaml
environment:
  # Admin user configuration
  - APP_ADMIN_USERNAME=admin
  - APP_ADMIN_PASSWORD=admin123
  - APP_ADMIN_EMAIL=admin@example.com
  - APP_ADMIN_ENABLED=true
```

## Access Points

### Frontend Application

- **URL**: http://localhost:3000
- **Login Page**: http://localhost:3000/login
- Use the admin credentials to access the dashboard and user management features

### API Endpoints

#### Direct Auth Service Access

```bash
# Login
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

#### API Gateway Access

```bash
# Login through API Gateway
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

## Testing

Use the provided test script to verify admin functionality:

```bash
./test-admin-login.sh
```

This script will:

1. Check service health
2. Test admin login
3. Verify protected endpoint access
4. Test frontend accessibility
5. Validate API Gateway routing

## Security Considerations

### Development vs Production

#### Development Mode

- Admin user is automatically created for convenience
- Default credentials are used
- Relaxed security settings for testing

#### Production Mode

- **Change default credentials immediately**
- Disable automatic admin creation if not needed
- Use strong, unique passwords
- Implement proper user management workflows

### Recommended Production Setup

1. **Change Default Credentials**:

   ```yaml
   environment:
     - APP_ADMIN_PASSWORD=your_strong_password_here
   ```

2. **Disable Auto-Creation** (Optional):

   ```yaml
   environment:
     - APP_ADMIN_ENABLED=false
   ```

3. **Use External User Management**:
   - Implement proper user registration flows
   - Use external identity providers (OAuth, LDAP, etc.)
   - Implement role-based access control

## Troubleshooting

### Admin User Not Created

1. Check service logs:

   ```bash
   docker compose logs auth-service | grep -i admin
   docker compose logs user-service | grep -i admin
   ```

2. Verify configuration:

   ```bash
   docker compose exec auth-service env | grep APP_ADMIN
   docker compose exec user-service env | grep APP_ADMIN
   ```

3. Check database:
   ```bash
   # For H2 (development)
   docker compose exec auth-service java -cp /app/app.jar org.h2.tools.Shell -url jdbc:h2:mem:authdb -user sa
   ```

### Login Issues

1. Verify service health:

   ```bash
   curl http://localhost:8082/actuator/health
   curl http://localhost:8081/actuator/health
   ```

2. Check JWT token validity:

   ```bash
   # Decode JWT token at jwt.io to verify claims
   ```

3. Test direct service access vs API Gateway:

   ```bash
   # Direct auth service
   curl -X POST http://localhost:8082/api/auth/login -H "Content-Type: application/json" -d '{"username": "admin", "password": "admin123"}'

   # Through API Gateway
   curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username": "admin", "password": "admin123"}'
   ```

## Next Steps

After successful admin setup:

1. **Access the Frontend**: Open http://localhost:3000 in your browser
2. **Login**: Use admin/admin123 credentials
3. **Explore Features**:
   - Dashboard with system overview
   - User management interface
   - API documentation via Swagger UI
4. **Create Additional Users**: Use the admin interface to create regular users
5. **Configure Production**: Follow security recommendations for production deployment

## Support

For issues or questions:

1. Check the service logs
2. Run the test script: `./test-admin-login.sh`
3. Verify all services are running: `docker compose ps`
4. Review the configuration in `docker-compose.yml`
