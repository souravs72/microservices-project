# 📮 Postman API Documentation

This directory contains comprehensive Postman collections and environments for testing the Microservices Platform APIs.

## 📁 Files Overview

### Collections

- **`Microservices_API_Collection.json`** - Main collection with all API endpoints
- **`README.md`** - This documentation file

### Environments

- **`Development_Environment.json`** - Local development environment
- **`Docker_Environment.json`** - Docker containerized environment
- **`Production_Environment.json`** - Production environment template

## 🚀 Quick Start

### 1. Import Collection and Environment

1. **Open Postman**
2. **Import Collection**: Click Import → Select `Microservices_API_Collection.json`
3. **Import Environment**: Click Import → Select desired environment file
4. **Set Active Environment**: Select the imported environment from the dropdown

### 2. Start Services

```bash
# Start all microservices
docker-compose up -d

# Verify services are running
curl http://localhost:8080/actuator/health
```

### 3. Test Authentication

1. **Run "User Login"** request in the Authentication Service folder
2. **Check Variables**: The JWT token will be automatically saved to `{{jwt_token}}`
3. **Test Protected Endpoints**: Use the saved token for authenticated requests

## 🔧 Environment Configuration

### Development Environment

- **Base URL**: `http://localhost:8080`
- **Services**: All services running on localhost
- **Credentials**: Pre-configured test credentials

### Docker Environment

- **Base URL**: `http://localhost:8080`
- **Services**: Containerized services
- **Network**: Docker network configuration

### Production Environment

- **Base URL**: `https://api.microservices.com`
- **Services**: Production service URLs
- **Credentials**: Environment variables (to be configured)

## 📋 API Endpoints Overview

### 🔐 Authentication Service (`/api/auth/*`)

| Method | Endpoint                    | Description            | Auth Required |
| ------ | --------------------------- | ---------------------- | ------------- |
| POST   | `/api/auth/register`        | User registration      | No            |
| POST   | `/api/auth/login`           | User login             | No            |
| POST   | `/api/auth/refresh`         | Refresh token          | No            |
| POST   | `/api/auth/validate`        | Validate token         | No            |
| POST   | `/api/auth/forgot-password` | Password reset request | No            |
| POST   | `/api/auth/reset-password`  | Password reset         | No            |
| POST   | `/api/auth/logout`          | User logout            | No            |
| GET    | `/api/auth/health`          | Service health         | No            |

### 👥 User Service (`/api/users/*`)

| Method | Endpoint                         | Description          | Auth Required |
| ------ | -------------------------------- | -------------------- | ------------- |
| GET    | `/api/users`                     | Get all users        | Admin         |
| GET    | `/api/users/{id}`                | Get user by ID       | Yes           |
| GET    | `/api/users/username/{username}` | Get user by username | Yes           |
| POST   | `/api/users`                     | Create user          | Admin         |
| PUT    | `/api/users/{id}`                | Update user          | Yes           |
| DELETE | `/api/users/{id}`                | Delete user          | Admin         |
| POST   | `/api/users/sync`                | Sync user (internal) | API Key       |

### 📧 Notification Service (`/api/notifications/*`)

| Method | Endpoint                        | Description                 | Auth Required |
| ------ | ------------------------------- | --------------------------- | ------------- |
| GET    | `/api/notifications/history`    | Get notification history    | Basic Auth    |
| GET    | `/api/notifications/stats`      | Get notification statistics | Basic Auth    |
| GET    | `/api/notifications/{id}`       | Get notification by ID      | Basic Auth    |
| POST   | `/api/notifications/test-email` | Send test email             | Basic Auth    |
| GET    | `/health`                       | Service health              | No            |

### 🔍 GraphQL (`/graphql`)

| Type     | Query/Mutation             | Description          | Auth Required |
| -------- | -------------------------- | -------------------- | ------------- |
| Query    | `users`                    | Get all users        | Admin         |
| Query    | `user(id)`                 | Get user by ID       | Yes           |
| Query    | `userByUsername(username)` | Get user by username | Yes           |
| Mutation | `createUser(input)`        | Create user          | Admin         |
| Mutation | `updateUser(id, input)`    | Update user          | Yes           |

## 🔑 Authentication Methods

### 1. JWT Token Authentication

- **Usage**: Most API endpoints
- **Header**: `Authorization: Bearer {{jwt_token}}`
- **Auto-save**: Token automatically saved after login

### 2. Basic Authentication

- **Usage**: Notification Service endpoints
- **Credentials**: `admin/admin123` (development)
- **Header**: Automatically handled by Postman

### 3. Internal API Key

- **Usage**: Service-to-service communication
- **Header**: `X-Internal-API-Key: {{internal_api_key}}`

## 📊 Testing Workflows

### Complete User Flow

1. **Register User** → Get JWT token
2. **Login User** → Verify authentication
3. **Get User Profile** → Test protected endpoint
4. **Update Profile** → Test user modification
5. **Get All Users** → Test admin endpoint (if admin)

### Admin Flow

1. **Login as Admin** → Get admin JWT token
2. **Create User** → Test user creation
3. **Get All Users** → Test user listing
4. **Delete User** → Test user deletion

### Notification Flow

1. **Send Test Email** → Test email functionality
2. **Get Notification History** → Check email logs
3. **Get Notification Stats** → View statistics

## 🔧 Pre-request Scripts

The collection includes automatic scripts that:

- **Set Content-Type headers** for all requests
- **Add timestamps** for request tracking
- **Auto-save JWT tokens** from authentication responses
- **Handle token refresh** automatically

## 🧪 Test Scripts

Each request includes test scripts that:

- **Validate response status** codes
- **Check response structure** and required fields
- **Auto-save tokens** and variables
- **Log important information** to console

## 📈 Monitoring and Health Checks

### Health Check Endpoints

- **API Gateway**: `GET /health`
- **Auth Service**: `GET /api/auth/health`
- **User Service**: `GET /actuator/health`
- **Notification Service**: `GET /health`

### Monitoring

- **Response times** automatically logged
- **Error rates** tracked in Postman console
- **Service availability** monitored via health checks

## 🚨 Error Handling

### Common Error Responses

#### 401 Unauthorized

```json
{
  "timestamp": "2025-01-01T00:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is invalid or expired",
  "path": "/api/users"
}
```

#### 403 Forbidden

```json
{
  "timestamp": "2025-01-01T00:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Admin role required",
  "path": "/api/users"
}
```

#### 400 Bad Request

```json
{
  "timestamp": "2025-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "details": [
    {
      "field": "email",
      "message": "Email format is invalid"
    }
  ]
}
```

## 🔄 Rate Limiting

The API Gateway implements rate limiting:

- **Rate**: 10 requests per second
- **Burst**: 20 requests
- **Key**: Based on username or IP address

## 📝 Request Examples

### User Registration

```json
POST /api/auth/register
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "Password123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

### User Login

```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

### Create User (Admin)

```json
POST /api/users
Authorization: Bearer {{jwt_token}}
{
  "username": "newuser",
  "email": "newuser@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "Password123!"
}
```

### GraphQL Query

```json
POST /graphql
Authorization: Bearer {{jwt_token}}
{
  "query": "query { users { id username email firstName lastName } }"
}
```

## 🛠️ Troubleshooting

### Common Issues

1. **Connection Refused**

   - Verify services are running: `docker-compose ps`
   - Check service health: Use health check endpoints

2. **401 Unauthorized**

   - Verify JWT token is valid: Use token validation endpoint
   - Check token expiration: Re-login if expired

3. **403 Forbidden**

   - Verify user has required role (Admin for user management)
   - Check endpoint permissions

4. **500 Internal Server Error**
   - Check service logs: `docker-compose logs <service-name>`
   - Verify database connectivity
   - Check Redis/Kafka connectivity

### Debug Mode

Enable debug logging in Postman:

1. Go to Postman Console (View → Show Postman Console)
2. Run requests to see detailed logs
3. Check request/response headers and body

## 📚 Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Gateway Docs**: http://localhost:8080/v3/api-docs
- **User Service GraphQL**: http://localhost:8081/graphiql
- **Frontend Application**: http://localhost:3000

## 🤝 Contributing

To add new endpoints:

1. Update the main collection JSON file
2. Add appropriate test scripts
3. Update this documentation
4. Test with all environments

## 📄 License

This Postman collection is part of the Microservices Platform and follows the same licensing terms.
