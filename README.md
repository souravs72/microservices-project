# Microservices Architecture Platform

A comprehensive microservices platform built with Spring Boot 3.5.6 and React 19, featuring robust authentication, user management, notification services, and a modern web frontend with dynamic dashboard and user management capabilities.

## 🚀 Quick Start

To get the platform running quickly:

```bash
# Clone the repository
git clone <repository-url>
cd microservices-parent

# Copy environment configuration
cp env.dev.example .env

# Start all services
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# Verify services are running
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Auth Service
curl http://localhost:8085/actuator/health  # Notification Service

# Start the frontend application
./start-frontend.sh

# Test admin user login (optional)
./test-admin-login.sh
```

**🌐 Frontend Access**: http://localhost:3000 (React application)

**🔐 Admin Login**: Username: `admin`, Password: `admin123`

For detailed setup instructions, see [DOCKER_STARTUP.md](DOCKER_STARTUP.md).

**🔒 Security**: See [GITHUB_SECRETS.md](GITHUB_SECRETS.md) for secure credential management.

**📧 Email Configuration**: See [AUTH_SERVICE_EMAIL_SETUP.md](AUTH_SERVICE_EMAIL_SETUP.md) for email functionality setup.

**🔐 Admin User Setup**: See [ADMIN_USER_SETUP.md](ADMIN_USER_SETUP.md) for admin user initialization and access.

**📮 API Testing**: See [postman/README.md](postman/README.md) for comprehensive Postman collections and automated testing.

## 🏗️ System Architecture

### Core Services

#### **Frontend Application** (Port 3000)

- **Purpose**: Modern React web application with dynamic UI
- **Technology**: React 19 + TypeScript + Vite + Tailwind CSS
- **Features**:
  - User authentication (login/registration)
  - Dynamic dashboard with real-time statistics
  - User management with advanced filtering and search
  - Responsive design for all device sizes
  - JWT token-based authentication
  - Protected routes and role-based access control
  - Real-time data updates and live activity feeds

#### **API Gateway** (Port 8080)

- **Purpose**: Central entry point for all client requests
- **Technology**: Spring Cloud Gateway with WebFlux
- **Features**:
  - Request routing and load balancing
  - Circuit breaker patterns (Resilience4j)
  - Rate limiting and security
  - Request/response transformation
  - Redis integration for caching

#### **Auth Service** (Port 8082)

- **Purpose**: Authentication and authorization management
- **Technology**: Spring Security + JWT + SMTP
- **Features**:
  - User registration and login
  - JWT token generation and validation
  - Password policies and account lockout
  - Password reset functionality with email notifications
  - Account locked notifications via email
  - Role-based access control
  - Resilience patterns (Circuit Breaker, Retry, Rate Limiter)
  - Kafka event publishing for user events

#### **User Service** (Port 8081)

- **Purpose**: User profile and data management
- **Technology**: Spring Boot + GraphQL + JPA
- **Features**:
  - User profile CRUD operations
  - GraphQL API with query depth limiting
  - Internal API key authentication
  - User data validation and security
  - Resilience patterns (Circuit Breaker, Retry, Rate Limiter)
  - Kafka event consumption for user events

#### **Notification Service** (Port 8085)

- **Purpose**: Email notifications and event processing
- **Technology**: Spring Boot + Kafka + Redis + SMTP + FreeMarker
- **Features**:
  - Welcome email automation with HTML templates
  - Event-driven notifications
  - Idempotency handling with Redis
  - Retry mechanisms and dead letter queues
  - Resilience patterns (Circuit Breaker, Retry, Bulkhead, Rate Limiter)
  - Asynchronous email processing

#### **Order Service** (Port 8083) - _Planned_

- **Purpose**: Order processing and management
- **Technology**: Spring Boot + Event Sourcing
- **Features**:
  - Order creation and tracking
  - Payment processing integration
  - Order status management
  - Event-driven architecture

#### **Inventory Service** (Port 8084) - _Planned_

- **Purpose**: Product inventory management
- **Technology**: Spring Boot + CQRS
- **Features**:
  - Product catalog management
  - Stock level tracking
  - Inventory reservations
  - Real-time stock updates

## 🔄 Data Flow Architecture

```
React Frontend → API Gateway → Service Router → Target Service
     ↓              ↓
  JWT Token    Authentication Check (Auth Service)
     ↓              ↓
  State Update  Authorization & Validation
     ↓              ↓
  UI Update     Business Logic Processing
                     ↓
              Event Publishing (Kafka)
                     ↓
              Notification Processing
                     ↓
              Real-time Updates (Frontend)
```

## 🛠️ Technology Stack

### **Frontend Framework**

- React 19 with TypeScript
- Vite 7 (Build tool)
- Tailwind CSS 4 (Styling)
- React Router DOM 7 (Routing)
- Axios (HTTP client)
- Lucide React (Icons)

### **Backend Framework**

- Spring Boot 3.5.6
- Spring Cloud 2025.0.0
- Spring Security 6.5.5
- Spring Data JPA
- Spring Kafka

### **Data & Messaging**

- **Databases**: PostgreSQL (Production), H2 (Development)
- **Message Broker**: Apache Kafka
- **Cache**: Redis
- **Search**: Elasticsearch (Planned)

### **Resilience & Monitoring**

- **Circuit Breaker**: Resilience4j
- **Metrics**: Micrometer + Prometheus
- **Monitoring**: Grafana
- **Health Checks**: Spring Actuator

### **Development & Deployment**

- **Build Tool**: Maven
- **Containerization**: Docker + Docker Compose
- **API Documentation**: OpenAPI/Swagger
- **Testing**: JUnit 5 + TestContainers
- **Email Templates**: FreeMarker
- **Monitoring**: Prometheus + Grafana

## 🔐 Security Architecture

### **Authentication Flow**

1. User credentials validated by Auth Service
2. JWT token generated with user claims
3. Token validated on each request
4. Role-based access control enforced

### **Service-to-Service Communication**

- Internal API keys for service authentication
- JWT tokens for user context propagation
- Encrypted communication channels
- Rate limiting and circuit breakers

## 📊 Event-Driven Architecture

### **Event Types**

- **User Events**: Registration, profile updates, authentication
- **Order Events**: Order creation, status changes, payments
- **Inventory Events**: Stock updates, reservations, alerts
- **Notification Events**: Email triggers, delivery status

### **Event Processing**

- Asynchronous event processing via Kafka
- Idempotency handling with Redis
- Dead letter queues for failed events
- Event sourcing for audit trails

## 🚀 Scalability Features

### **Horizontal Scaling**

- Stateless service design
- Load balancer ready
- Database connection pooling
- Redis clustering support

### **Performance Optimization**

- Connection pooling (HikariCP)
- Redis caching
- Async processing
- Circuit breaker patterns

## 🔧 Configuration Management

### **Environment Profiles**

- **Development**: H2 database, relaxed security, verbose logging, mail disabled
- **Staging**: PostgreSQL, moderate security, balanced logging, staging SMTP
- **Production**: PostgreSQL, strict security, optimized logging, production SMTP

### **Externalized Configuration**

- Environment-specific YAML files
- Docker secrets management
- Configurable via environment variables

## 📈 Monitoring & Observability

### **Health Checks**

- Service health endpoints
- Database connectivity checks
- External service dependencies
- Custom health indicators

### **Metrics & Logging**

- Prometheus metrics collection
- Structured logging with correlation IDs
- Grafana dashboards
- Centralized log aggregation (Planned)

## 🏭 Production Readiness

### **Resilience Patterns**

- Circuit breakers for external calls
- Retry mechanisms with exponential backoff
- Bulkhead isolation
- Rate limiting

### **Security Features**

- Password policies
- Account lockout protection
- JWT token expiration
- Input validation and sanitization

### **Operational Excellence**

- Comprehensive health checks
- Graceful shutdown handling
- Database migration support
- Backup and recovery procedures

## 📋 Current Status

### ✅ **Implemented & Working**

- **Frontend Application**: Modern React app with authentication, dashboard, and user management
- **API Gateway**: Fully operational with circuit breakers and routing
- **User Service**: GraphQL API with user management
- **Auth Service**: JWT authentication and user registration
- **Notification Service**: Event-driven email processing (templates ready)
- **Infrastructure**: Redis, Kafka, PostgreSQL, Prometheus, Grafana
- **Monitoring**: Health checks, metrics, and dashboards

### ⚠️ **Development Mode Notes**

- **Mail Service**: Disabled in development mode to avoid authentication issues
- **Databases**: Using H2 in-memory databases for development
- **Security**: Relaxed settings for development testing

### 🚧 **Planned Features**

- **Order Service**: Order processing and management
- **Inventory Service**: Product catalog and stock management
- **Service Discovery**: Eureka or Consul integration
- **API Gateway**: Load balancing and service discovery

## 🔗 **Quick Links**

- **Frontend Application**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **User Service GraphQL**: http://localhost:8081/graphql
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001

## 📮 **API Testing**

- **Postman Collections**: Import from `postman/Microservices_API_Collection.json`
- **Test Scenarios**: Import from `postman/API_Testing_Scenarios.json`
- **Environments**: Development, Docker, and Production configurations available
- **Automated Testing**: Newman CLI integration with CI/CD pipeline

---

_This microservices platform is designed for enterprise-scale applications with high availability, security, and maintainability requirements._
