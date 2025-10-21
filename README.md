# Microservices Architecture Platform

A comprehensive microservices platform built with Spring Boot 3.5.6 and React 19, featuring robust authentication, user management, notification services, and a modern web frontend.

## 🚀 Quick Start

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+
- 8GB+ RAM available
- Ports 8080-8085, 6379, 9092, 5173, 9090 available

### 1. Setup Environment
```bash
# Clone the repository
git clone <repository-url>
cd microservices-parent

# Copy environment configuration
cp env.dev.example .env

# Edit .env file with your configuration
nano .env
```

### 2. Start Services
```bash
# Start all services
docker compose up -d

# Or use the rebuild script for a clean start
./rebuild-docker-v2.sh
```

### 3. Verify Services
```bash
# Check service status
./rebuild-docker-v2.sh --status

# Test API Gateway
curl http://localhost:8080/actuator/health
```

## 🏗️ Architecture

### Core Services
- **API Gateway** (8080) - Central entry point
- **Auth Service** (8082) - Authentication & authorization
- **User Service** (8081) - User management
- **Order Service** (8083) - Order processing
- **Inventory Service** (8084) - Product management
- **Notification Service** (8085) - Notifications
- **Frontend** (5173) - React web interface

### Infrastructure
- **PostgreSQL** - Database
- **Redis** - Caching & sessions
- **Kafka** - Event streaming
- **Prometheus** - Metrics
- **Grafana** - Monitoring

## 🛠️ Essential Scripts

### Main Scripts
- **`rebuild-docker-v2.sh`** - Main Docker management script
- **`reset-kafka.sh`** - Quick Kafka reset for cluster issues

### Usage Examples
```bash
# Full rebuild
./rebuild-docker-v2.sh

# Clean rebuild with image cleanup
./rebuild-docker-v2.sh --clean-images

# Show service status
./rebuild-docker-v2.sh --status

# Test JWT validation
./rebuild-docker-v2.sh --test

# Check infrastructure only
./rebuild-docker-v2.sh --infra
```

## 👤 Admin Setup

### Default Admin User
- **Username:** admin
- **Password:** [Set in ADMIN_PASSWORD environment variable]
- **Email:** admin@example.com

### Creating Admin Users
```bash
# Via API
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "[YOUR_ADMIN_PASSWORD]",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

## 🔧 Development

### Environment Variables
Key variables in `.env`:
- `DB_PASSWORD` - Database password
- `REDIS_PASSWORD` - Redis password
- `JWT_SECRET` - JWT signing secret
- `INTERNAL_API_KEY` - Service-to-service communication
- `MAIL_USERNAME` - Email service username
- `MAIL_PASSWORD` - Email service password

### Service URLs
- **API Gateway:** http://localhost:8080
- **Frontend:** http://localhost:5173
- **Grafana:** http://localhost:9090 ([Set in GRAFANA_PASSWORD environment variable])
- **Prometheus:** http://localhost:9091

## 📚 Additional Documentation

- **Production Testing:** [PRODUCTION_TESTING_GUIDE.md](./PRODUCTION_TESTING_GUIDE.md)
- **API Documentation:** [postman/README.md](./postman/README.md)
- **Security Setup:** [GITHUB_SECRETS.md](./GITHUB_SECRETS.md)
- **Frontend Guide:** [frontend/README.md](./frontend/README.md)

## 🐛 Troubleshooting

### Common Issues
1. **Kafka cluster ID mismatch:** Run `./reset-kafka.sh`
2. **Port conflicts:** Check if ports are available
3. **Memory issues:** Ensure 8GB+ RAM available
4. **Service startup failures:** Check logs with `docker logs <service-name>`

### Health Checks
```bash
# Check all services
./rebuild-docker-v2.sh --status

# Check specific service
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## 📄 License

This project is licensed under the MIT License.