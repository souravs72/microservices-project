# Microservices Architecture Setup Guide

This document provides a comprehensive guide for setting up and running the microservices architecture.

## 🏗️ Architecture Overview

The microservices architecture consists of the following services:

### Core Services

- **API Gateway** (Port 8080) - Central entry point for all client requests
- **Auth Service** (Port 8082) - Authentication and authorization
- **User Service** (Port 8081) - User management and profiles
- **Order Service** (Port 8083) - Order processing and management
- **Inventory Service** (Port 8084) - Product and inventory management
- **Notification Service** (Port 8085) - Email and notification handling

### Supporting Services

- **Frontend** (Port 5173) - React-based web application
- **Redis** (Port 6379) - Caching and session storage
- **Kafka** (Port 9092) - Event streaming and messaging
- **PostgreSQL** - Database for each service
- **Prometheus** (Port 9090) - Metrics collection
- **Grafana** (Port 3001) - Monitoring dashboards

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 17+
- Maven 3.6+
- Node.js 18+ (for frontend development)

### 1. Environment Setup

Copy the environment configuration:

```bash
cp env.dev.example .env
```

Edit `.env` file with your configuration:

```bash
nano .env
```

### 2. Start All Services

Start the entire microservices stack:

```bash
docker-compose up -d
```

### 3. Verify Services

Run the comprehensive test suite:

```bash
./test-microservices.sh
```

### 4. Access Services

- **API Gateway**: http://localhost:8080
- **Frontend**: http://localhost:5173
- **Swagger UI**:
  - Auth Service: http://localhost:8082/swagger-ui.html
  - User Service: http://localhost:8081/swagger-ui.html
  - Order Service: http://localhost:8083/swagger-ui.html
  - Inventory Service: http://localhost:8084/swagger-ui.html
  - Notification Service: http://localhost:8085/swagger-ui.html
- **Monitoring**:
  - Prometheus: http://localhost:9090
  - Grafana: http://localhost:3001 (admin/admin123)

## 🔧 Service Details

### API Gateway

- **Port**: 8080
- **Purpose**: Central entry point, routing, rate limiting, CORS
- **Features**: Circuit breaker, load balancing, request/response transformation

### Auth Service

- **Port**: 8082
- **Purpose**: Authentication, authorization, JWT token management
- **Features**: User registration, login, password reset, admin user creation
- **Database**: PostgreSQL (port 5435)

### User Service

- **Port**: 8081
- **Purpose**: User profile management, GraphQL API
- **Features**: User CRUD operations, profile management, admin sync
- **Database**: PostgreSQL (port 5433)

### Order Service

- **Port**: 8083
- **Purpose**: Order processing, order management
- **Features**: Order creation, status updates, inventory integration
- **Database**: PostgreSQL (port 5436)

### Inventory Service

- **Port**: 8084
- **Purpose**: Product and inventory management
- **Features**: Product CRUD, stock management, availability checks
- **Database**: PostgreSQL (port 5437)

### Notification Service

- **Port**: 8085
- **Purpose**: Email notifications, event processing
- **Features**: Email sending, event-driven notifications
- **Database**: PostgreSQL (port 5434)

## 🗄️ Database Configuration

Each service has its own PostgreSQL database:

| Service      | Database       | Port | User             |
| ------------ | -------------- | ---- | ---------------- |
| Auth         | authdb         | 5435 | authuser         |
| User         | userdb         | 5433 | useruser         |
| Order        | orderdb        | 5436 | orderuser        |
| Inventory    | inventorydb    | 5437 | inventoryuser    |
| Notification | notificationdb | 5434 | notificationuser |

## 🔄 Event Flow

The microservices communicate through events:

1. **User Registration**: Auth Service → User Service → Notification Service
2. **Order Creation**: Order Service → Inventory Service → Notification Service
3. **Stock Updates**: Inventory Service → Order Service (for availability checks)

## 🛠️ Development

### Running Individual Services

For development, you can run services individually:

```bash
# Auth Service
cd auth-service
mvn spring-boot:run

# User Service
cd user-service
mvn spring-boot:run

# Order Service
cd order-service
mvn spring-boot:run

# Inventory Service
cd inventory-service
mvn spring-boot:run

# Notification Service
cd notification-service
mvn spring-boot:run
```

### Frontend Development

```bash
cd frontend
npm install
npm run dev
```

## 📊 Monitoring

### Prometheus

- **URL**: http://localhost:9090
- **Purpose**: Metrics collection and querying
- **Configuration**: `monitoring/prometheus.yml`

### Grafana

- **URL**: http://localhost:3001
- **Username**: admin
- **Password**: admin123
- **Purpose**: Visualization and alerting

## 🔒 Security

### JWT Configuration

- **Secret**: Configured in environment variables
- **Expiration**: 15 minutes (access), 7 days (refresh)
- **Algorithm**: HS256

### CORS Configuration

- **Allowed Origins**: Configurable via environment variables
- **Default**: http://localhost:3000, http://localhost:5173

### Internal API Keys

- **Purpose**: Service-to-service communication
- **Configuration**: Set in environment variables

## 🐛 Troubleshooting

### Common Issues

1. **Port Conflicts**: Ensure all required ports are available
2. **Database Connection**: Check if PostgreSQL containers are running
3. **Service Dependencies**: Ensure dependent services are healthy before starting others
4. **Memory Issues**: Increase Docker memory allocation if needed

### Logs

View service logs:

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service
docker-compose logs -f user-service
docker-compose logs -f order-service
docker-compose logs -f inventory-service
docker-compose logs -f notification-service
```

### Health Checks

Check service health:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

## 📝 API Documentation

Each service provides Swagger UI documentation:

- Navigate to `http://localhost:<port>/swagger-ui.html`
- Interactive API testing available
- Complete API documentation with examples

## 🔄 CI/CD

The project includes:

- GitHub Actions workflows
- Docker containerization
- Automated testing
- Environment-specific configurations

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Docker Documentation](https://docs.docker.com/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

