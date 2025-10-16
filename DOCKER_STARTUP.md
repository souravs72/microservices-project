# Docker Startup Guide

Quick start guide for running the microservices platform with Docker.

## 🚀 Quick Start

### Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- 8GB+ RAM available
- Ports 8080-8085, 6379, 9092, 3000, 9090 available

### 1. Clone and Setup

```bash
git clone <repository-url>
cd microservices-parent
```

### 2. Environment Configuration

```bash
# Copy environment template
cp env.dev.example .env

# Edit with your values
nano .env
```

**🔒 Security**: See [GITHUB_SECRETS.md](GITHUB_SECRETS.md) for secure credential management.

**Required Environment Variables:**

```bash
# Database
POSTGRES_PASSWORD=your_secure_password
REDIS_PASSWORD=your_redis_password

# JWT Secret (generate a secure random string)
JWT_SECRET=your_jwt_secret_key

# Email Configuration
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Internal API Key
INTERNAL_API_KEY=your_internal_api_key
```

### 3. Start All Services

```bash
# Development environment (recommended)
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# Production environment
docker compose -f docker-compose.yml up -d
```

### 4. Verify Services

```bash
# Check all services are running
docker compose ps

# Check service health
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Auth Service
curl http://localhost:8085/actuator/health  # Notification Service
```

## 📋 Service Ports

| Service                 | Port | Description                    |
| ----------------------- | ---- | ------------------------------ |
| API Gateway             | 8080 | Main entry point               |
| User Service            | 8081 | User management & GraphQL API  |
| Auth Service            | 8082 | Authentication & JWT tokens    |
| Order Service           | 8083 | Order processing (planned)     |
| Inventory Service       | 8084 | Inventory management (planned) |
| Notification Service    | 8085 | Email notifications            |
| Redis                   | 6379 | Cache and session store        |
| Kafka                   | 9092 | Message broker                 |
| Zookeeper               | 2181 | Kafka coordination             |
| PostgreSQL Auth         | 5436 | Auth service database          |
| PostgreSQL User         | 5433 | User service database          |
| PostgreSQL Notification | 5434 | Notification service database  |
| Prometheus              | 9090 | Metrics collection             |
| Grafana                 | 3000 | Monitoring dashboard           |

## 🔧 Development Commands

### Start Individual Services

```bash
# Start only core services
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d redis kafka zookeeper

# Start specific service
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d auth-service
```

### View Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f notification-service

# Last 100 lines
docker compose logs --tail=100 notification-service
```

### Restart Services

```bash
# Restart all
docker compose restart

# Restart specific service
docker compose restart auth-service
```

### Stop Services

```bash
# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v
```

## 🧪 Testing the Platform

### 1. Test API Gateway

```bash
curl http://localhost:8080/actuator/health
```

### 2. Test Authentication

```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### 3. Test Email Notifications

```bash
# Send test email (Note: Mail service is disabled in dev mode)
curl -X POST http://localhost:8085/api/notifications/test-email \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"email":"your-email@example.com","username":"testuser","firstName":"Test","lastName":"User"}'
```

**Note**: In development mode, mail sending is disabled to avoid authentication issues. The service will log the email attempt but won't actually send emails.

### 4. Access Monitoring

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **API Docs**: http://localhost:8080/swagger-ui.html

## 🐛 Troubleshooting

### Common Issues

#### Services Not Starting

```bash
# Check logs
docker compose logs service-name

# Check port conflicts
netstat -tulpn | grep :8080

# Restart Docker
sudo systemctl restart docker
```

#### Database Connection Issues

```bash
# Check PostgreSQL logs
docker compose logs postgres-auth-dev

# Verify environment variables
docker compose config
```

#### Email Not Working

```bash
# Check notification service logs
docker compose logs notification-service

# Verify SMTP configuration
curl -u admin:admin123 http://localhost:8085/actuator/health
```

### Health Check Commands

```bash
# All services health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8085/actuator/health

# Database connectivity
docker compose exec postgres-auth-dev psql -U authuser -d authdb -c "SELECT 1;"

# Redis connectivity
docker compose exec redis-dev redis-cli ping

# Kafka connectivity
docker compose exec kafka-dev kafka-topics --bootstrap-server localhost:9092 --list
```

## 🔄 Environment Management

### Development Environment

- Uses H2 in-memory databases
- Relaxed security settings
- Verbose logging enabled
- All management endpoints exposed

### Production Environment

- Uses PostgreSQL databases
- Strict security settings
- Optimized logging
- Limited management endpoint exposure

### Switching Environments

```bash
# Stop current environment
docker compose down

# Start different environment
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d    # Development
docker compose -f docker-compose.yml up -d                              # Production
```

## 📊 Monitoring Setup

### Prometheus Configuration

- Metrics collection from all services
- Service discovery enabled
- Retention: 15 days

### Grafana Dashboards

- Service health monitoring
- Performance metrics
- Error rate tracking
- Resource utilization

### Log Aggregation

- Structured JSON logging
- Correlation ID tracking
- Centralized log collection (planned)

## 🔒 Security Notes

### Production Deployment

1. Change all default passwords
2. Use strong JWT secrets
3. Enable HTTPS/TLS
4. Configure firewall rules
5. Regular security updates

### Environment Variables

- Never commit `.env` files
- Use Docker secrets for sensitive data
- Rotate credentials regularly
- Monitor access logs

---

**Need Help?** Check the service logs or refer to the main README.md for architecture details.
