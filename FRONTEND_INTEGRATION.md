# Frontend Integration Guide

This guide explains how to integrate the React frontend with the microservices backend.

## 🏗️ Architecture Overview

The frontend is a modern React application that communicates with the microservices backend through REST APIs:

```
┌─────────────────┐    HTTP/REST    ┌──────────────────┐
│                 │ ──────────────► │   API Gateway    │
│  React Frontend │                 │   (Port 8080)    │
│   (Port 3000)   │ ◄────────────── │                  │
└─────────────────┘                 └──────────────────┘
                                              │
                                              ▼
                                    ┌──────────────────┐
                                    │  Microservices   │
                                    │                  │
                                    │ • Auth Service   │
                                    │ • User Service   │
                                    │ • Order Service  │
                                    │ • Notification   │
                                    └──────────────────┘
```

## 🔗 API Integration Points

### Authentication Service (Port 8082)

- **Login**: `POST /api/auth/login`
- **Register**: `POST /api/auth/register`
- **Validate Token**: `GET /api/auth/validate`
- **Password Reset**: `POST /api/auth/forgot-password`

### User Service (Port 8081)

- **Get All Users**: `GET /api/users`
- **Get User by ID**: `GET /api/users/:id`
- **Update User**: `PUT /api/users/:id`
- **Delete User**: `DELETE /api/users/:id`

### API Gateway (Port 8080)

All requests are routed through the API Gateway, which handles:

- Authentication and authorization
- Rate limiting
- Circuit breaker patterns
- Request/response logging

## 🚀 Quick Start

### 1. Start Backend Services

```bash
# Start all microservices
docker-compose up -d

# Or start individual services
docker-compose up -d api-gateway auth-service user-service
```

### 2. Start Frontend

```bash
# Option 1: Use the startup script
./start-frontend.sh

# Option 2: Manual start
cd frontend
npm install
npm run dev
```

### 3. Access the Application

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Swagger Documentation**: http://localhost:8080/swagger-ui.html

## 🔧 Configuration

### Environment Variables

Create `frontend/.env.local`:

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080

# Development Configuration
VITE_NODE_ENV=development

# Feature Flags
VITE_ENABLE_DEBUG=true
VITE_ENABLE_ANALYTICS=false
```

### Backend Configuration

Ensure the following services are running:

- **API Gateway**: Port 8080
- **Auth Service**: Port 8082
- **User Service**: Port 8081
- **Redis**: Port 6379
- **PostgreSQL**: Port 5432

## 📡 API Communication

### Authentication Flow

```typescript
// Login request
const loginResponse = await axios.post("/api/auth/login", {
  username: "user@example.com",
  password: "password123",
});

// Token is automatically stored and used for subsequent requests
localStorage.setItem("token", loginResponse.data.token);
```

### Protected API Calls

```typescript
// Axios interceptor automatically adds Bearer token
const usersResponse = await axios.get("/api/users");
```

### Error Handling

```typescript
// Automatic token expiration handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);
```

## 🎯 Features Integration

### 1. User Authentication

- **Login Page**: `/login`
- **Registration Page**: `/register`
- **Protected Routes**: Automatic redirection for unauthenticated users
- **Token Management**: Automatic token refresh and expiration handling

### 2. Dashboard

- **Real-time Statistics**: Fetched from `/api/dashboard/stats`
- **Activity Feed**: Real-time updates from `/api/dashboard/activity`
- **Responsive Design**: Works on all device sizes

### 3. User Management

- **User Listing**: Paginated list with search and filtering
- **User Actions**: View, edit, delete, activate/deactivate
- **Bulk Operations**: Multi-select and batch operations

## 🔒 Security Features

### Authentication

- JWT token-based authentication
- Automatic token refresh
- Secure token storage in localStorage
- Protected route handling

### API Security

- HTTPS support (production)
- CORS configuration
- Rate limiting (backend)
- Input validation and sanitization

### Frontend Security

- XSS protection
- CSRF token handling
- Secure HTTP headers
- Content Security Policy

## 📊 Real-time Features

### Live Updates

The dashboard includes real-time features:

- **Statistics**: Auto-refresh every 30 seconds
- **Activity Feed**: Real-time notifications
- **User Status**: Live user activity indicators

### WebSocket Integration (Future)

Planned WebSocket integration for:

- Real-time notifications
- Live user presence
- Instant messaging
- Live updates

## 🧪 Testing Integration

### API Testing

```bash
# Test authentication
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Test user listing (with token)
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Frontend Testing

```bash
cd frontend
npm run test
```

## 🚀 Deployment

### Development

```bash
# Start all services
docker-compose up -d
./start-frontend.sh
```

### Production

```bash
# Build frontend
cd frontend
npm run build

# Deploy with Docker
docker-compose -f docker-compose.prod.yml up -d
```

## 🔧 Troubleshooting

### Common Issues

1. **CORS Errors**

   - Ensure API Gateway CORS is configured
   - Check VITE_API_BASE_URL is correct

2. **Authentication Issues**

   - Verify token is being sent in requests
   - Check token expiration
   - Ensure auth service is running

3. **API Connection Issues**
   - Verify all backend services are running
   - Check API Gateway health endpoint
   - Verify network connectivity

### Debug Mode

Enable debug mode in `.env.local`:

```env
VITE_ENABLE_DEBUG=true
```

This will show detailed API request/response logs in the browser console.

## 📈 Performance Optimization

### Frontend

- **Code Splitting**: Automatic route-based splitting
- **Lazy Loading**: Components loaded on demand
- **Asset Optimization**: Vite's built-in optimization
- **Caching**: Browser caching for static assets

### Backend Integration

- **Request Batching**: Multiple API calls in single request
- **Caching**: Redis caching for frequently accessed data
- **Pagination**: Large data sets loaded in chunks
- **Debouncing**: Search input debouncing

## 🔄 Data Flow

1. **User Action**: User interacts with UI component
2. **State Update**: React state updated locally
3. **API Call**: HTTP request sent to API Gateway
4. **Backend Processing**: Microservice processes request
5. **Response**: Data returned to frontend
6. **UI Update**: Component re-renders with new data

## 📝 API Documentation

Full API documentation is available at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## 🤝 Contributing

1. **Frontend Changes**: Make changes in `/frontend/src/`
2. **API Changes**: Update corresponding microservice
3. **Testing**: Test both frontend and backend integration
4. **Documentation**: Update this guide for new features

## 📞 Support

For integration issues:

1. Check service health endpoints
2. Review browser console for errors
3. Check backend service logs
4. Verify environment configuration
