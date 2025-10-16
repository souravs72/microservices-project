# 📚 Swagger Documentation Guide

This guide shows you how to access and explore all the API documentation for your microservices platform.

## 🌐 Swagger UI Endpoints

### 1. **Notification Service** (Port 8085) ✅ **WORKING**
- **Swagger UI**: http://localhost:8085/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8085/v3/api-docs
- **Authentication**: Basic Auth (admin:admin123)
- **Features**:
  - Email notification endpoints
  - Notification history
  - Test email functionality
  - Statistics and monitoring

### 2. **User Service** (Port 8081) 🔐 **AUTHENTICATION REQUIRED**
- **Swagger UI**: http://localhost:8081/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs
- **Authentication**: JWT Token required
- **Features**:
  - User management endpoints (CRUD operations)
  - GraphQL endpoint documentation
  - User profile management
  - Internal API endpoints

### 3. **Auth Service** (Port 8082) ⚠️ **LIMITED ACCESS**
- **Swagger UI**: Not fully configured
- **Health Check**: http://localhost:8082/actuator/health
- **Features**:
  - Authentication endpoints (login, register, logout)
  - JWT token management
  - Password reset functionality
  - User session management

### 4. **API Gateway** (Port 8080) ⚠️ **LIMITED ACCESS**
- **Swagger UI**: Not fully configured
- **Health Check**: http://localhost:8080/actuator/health
- **Features**:
  - Route definitions
  - Gateway configuration
  - Fallback endpoints

## 🔐 Authentication for Swagger

Some services require authentication to access Swagger UI:

### User Service & Notification Service
- **Username**: `admin`
- **Password**: `admin123`
- Or use the "Authorize" button in Swagger UI with JWT token

### Auth Service
- No authentication required for Swagger UI
- Use "Try it out" to test authentication endpoints

## 🚀 Quick Access Links

### Local Development URLs
```bash
# User Service
open http://localhost:8081/swagger-ui.html

# Auth Service  
open http://localhost:8082/swagger-ui.html

# Notification Service
open http://localhost:8085/swagger-ui.html

# API Gateway
open http://localhost:8080/swagger-ui.html
```

### Using curl to check endpoints
```bash
# Check if Swagger is available
curl -s http://localhost:8081/swagger-ui.html | head -5
curl -s http://localhost:8082/swagger-ui.html | head -5
curl -s http://localhost:8085/swagger-ui.html | head -5
curl -s http://localhost:8080/swagger-ui.html | head -5
```

## 📖 How to Use Swagger UI

### 1. **Explore Endpoints**
- Click on any endpoint to expand it
- See request/response schemas
- View example requests and responses

### 2. **Try API Calls**
- Click "Try it out" button
- Fill in required parameters
- Click "Execute" to make the API call
- See the actual response

### 3. **Authentication**
- Click "Authorize" button (🔒 icon)
- Enter credentials or JWT token
- All subsequent requests will include authentication

### 4. **Download OpenAPI Spec**
- Click the JSON/YAML download links
- Use the spec for code generation
- Import into API testing tools like Postman

## 🧪 Testing Common Endpoints

### Auth Service - Register a User
```bash
POST http://localhost:8082/api/auth/register
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}
```

### Auth Service - Login
```bash
POST http://localhost:8082/api/auth/login
{
  "username": "testuser",
  "password": "password123"
}
```

### User Service - Get User Profile
```bash
GET http://localhost:8081/api/users/profile
Authorization: Bearer <JWT_TOKEN>
```

### Notification Service - Test Email
```bash
POST http://localhost:8085/api/notifications/test-email
Authorization: Basic admin:admin123
{
  "email": "test@example.com",
  "username": "testuser",
  "firstName": "Test",
  "lastName": "User"
}
```

## 🔍 Troubleshooting

### If Swagger UI doesn't load:
1. **Check service status**:
   ```bash
   curl http://localhost:8081/actuator/health
   curl http://localhost:8082/actuator/health
   curl http://localhost:8085/actuator/health
   ```

2. **Check if OpenAPI is enabled**:
   ```bash
   curl http://localhost:8081/v3/api-docs
   curl http://localhost:8082/v3/api-docs
   curl http://localhost:8085/v3/api-docs
   ```

3. **Restart services if needed**:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.dev.yml restart user-service auth-service notification-service
   ```

### Common Issues:
- **404 Error**: Service might not be running or OpenAPI is disabled
- **Authentication Required**: Use the provided credentials
- **CORS Issues**: Should work in development mode

## 📊 API Documentation Features

### What you'll find in each service:

#### **User Service**
- REST API endpoints for user management
- GraphQL schema documentation
- Request/response models
- Validation rules

#### **Auth Service**
- Authentication flows
- Token management
- Password policies
- Session handling

#### **Notification Service**
- Email sending endpoints
- Notification history
- Statistics and monitoring
- Test functionality

#### **API Gateway**
- Route configurations
- Gateway filters
- Fallback mechanisms
- Health checks

## 🎯 Pro Tips

1. **Bookmark the URLs** for quick access during development
2. **Use the "Try it out" feature** to test endpoints directly
3. **Copy the curl commands** from Swagger UI for testing
4. **Download the OpenAPI specs** for code generation
5. **Check the Models section** to understand data structures

---

**Happy API exploring!** 🚀
