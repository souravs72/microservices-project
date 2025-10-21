# Frontend - React Microservices Client

Modern React 19 application with TypeScript, providing a comprehensive dashboard and user management interface for the microservices platform.

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- npm or yarn
- Backend services running

### Development Setup
```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build
```

## 🏗️ Architecture

The frontend communicates with microservices through the API Gateway:

```
React Frontend (5173) → API Gateway (8080) → Microservices
```

## 🔗 API Integration

### Authentication
- **Login:** `POST /api/auth/login`
- **Register:** `POST /api/auth/register`
- **Token Refresh:** `POST /api/auth/refresh`

### User Management
- **Get Users:** `GET /api/users`
- **Create User:** `POST /api/users`
- **Update User:** `PUT /api/users/:id`
- **Delete User:** `DELETE /api/users/:id`

### Orders
- **Get Orders:** `GET /api/orders`
- **Create Order:** `POST /api/orders`
- **Update Order:** `PUT /api/orders/:id`

## 🎨 Features

### Dashboard
- Real-time metrics and statistics
- Service health monitoring
- User activity overview
- System performance charts

### User Management
- User listing with pagination
- User creation and editing
- Role management (USER/ADMIN)
- Profile management

### Authentication
- JWT-based authentication
- Automatic token refresh
- Role-based access control
- Secure logout

## 🛠️ Development

### Environment Variables
```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_NODE_ENV=development
VITE_ENABLE_DEBUG=true
```

### Key Components
- **Dashboard:** Main overview page
- **AllUsers:** User management interface
- **Orders:** Order management interface
- **Login:** Authentication page
- **Layout:** Common layout components

### State Management
- React Context for global state
- Local state for component-specific data
- API service layer for backend communication

## 📦 Build & Deploy

### Development
```bash
npm run dev
```

### Production Build
```bash
npm run build
npm run preview
```

### Docker
```bash
# Build Docker image
docker build -t microservices-frontend .

# Run container
docker run -p 5173:5173 microservices-frontend
```

## 🔧 Configuration

### API Configuration
The frontend uses environment variables for API configuration:
- `VITE_API_BASE_URL` - Backend API base URL
- `VITE_NODE_ENV` - Environment (development/production)
- `VITE_ENABLE_DEBUG` - Debug mode toggle

### Authentication
- JWT tokens stored in localStorage
- Automatic token refresh on API calls
- Role-based route protection

## 🐛 Troubleshooting

### Common Issues
1. **CORS errors:** Ensure API Gateway CORS is configured
2. **Authentication failures:** Check JWT secret configuration
3. **API connection issues:** Verify backend services are running

### Debug Mode
Enable debug mode in `.env`:
```bash
VITE_ENABLE_DEBUG=true
```

## 📚 API Documentation

For complete API documentation, see:
- [Postman Collections](../postman/README.md)
- [Backend API Docs](http://localhost:8080/swagger-ui.html)