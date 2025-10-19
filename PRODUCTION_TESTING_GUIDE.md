# Production Testing Guide

This guide provides comprehensive instructions for testing the microservices architecture in a production-like environment.

## 🧪 Test Suite Overview

The testing suite consists of multiple test scripts that simulate real-world production scenarios:

### 1. **Basic Functionality Test** (`test-microservices.sh`)

- Service health checks
- API endpoint validation
- Database connectivity
- Basic integration testing

### 2. **Production Simulation Test** (`production-simulation-test.sh`)

- End-to-end user workflows
- Real-world business scenarios
- Data consistency testing
- Error handling validation

### 3. **Load Testing Simulation** (`load-test-simulation.sh`)

- Concurrent user simulation
- Performance benchmarking
- Stress testing
- Database performance testing

### 4. **Monitoring and Observability Test** (`monitoring-test.sh`)

- Metrics collection validation
- Log analysis
- Circuit breaker testing
- Rate limiting validation

### 5. **Master Test Runner** (`run-production-tests.sh`)

- Orchestrates all test suites
- Provides comprehensive reporting
- Manages test execution lifecycle

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Bash shell
- curl, jq, bc, nc utilities
- At least 4GB RAM available

### Running All Tests

```bash
# Run basic tests only
./run-production-tests.sh

# Run all tests including load testing
./run-production-tests.sh --include-load-tests

# Run tests and cleanup afterwards
./run-production-tests.sh --include-load-tests --cleanup
```

### Running Individual Tests

```bash
# Basic functionality test
./test-microservices.sh

# Production simulation test
./production-simulation-test.sh

# Load testing (intensive)
./load-test-simulation.sh

# Monitoring test
./monitoring-test.sh
```

## 📊 Test Scenarios

### Scenario 1: System Health Check

- **Purpose**: Verify all services are running and healthy
- **Duration**: ~2 minutes
- **Tests**: Service health endpoints, database connectivity, Redis, Kafka

### Scenario 2: Admin Authentication

- **Purpose**: Test admin user setup and authentication flow
- **Duration**: ~1 minute
- **Tests**: Admin login, JWT token generation, token validation

### Scenario 3: User Registration and Profile Creation

- **Purpose**: Test complete user onboarding process
- **Duration**: ~2 minutes
- **Tests**: User registration, profile creation, data consistency

### Scenario 4: Product Management

- **Purpose**: Test inventory management functionality
- **Duration**: ~3 minutes
- **Tests**: Product creation, stock management, availability checks

### Scenario 5: Order Processing (End-to-End)

- **Purpose**: Test complete order lifecycle
- **Duration**: ~5 minutes
- **Tests**: Order creation, status updates, inventory integration

### Scenario 6: Notification Testing

- **Purpose**: Test notification system functionality
- **Duration**: ~1 minute
- **Tests**: Email notifications, event processing

### Scenario 7: API Gateway Integration

- **Purpose**: Test central routing and load balancing
- **Duration**: ~2 minutes
- **Tests**: Service discovery, request routing, CORS

### Scenario 8: Error Handling and Resilience

- **Purpose**: Test system resilience and error handling
- **Duration**: ~3 minutes
- **Tests**: Invalid requests, error responses, circuit breakers

### Scenario 9: Performance and Load Testing

- **Purpose**: Test system performance under load
- **Duration**: ~10 minutes
- **Tests**: Concurrent requests, response times, throughput

### Scenario 10: Data Consistency and Transactions

- **Purpose**: Test data integrity across services
- **Duration**: ~3 minutes
- **Tests**: Order cancellation, stock restoration, transaction consistency

## 🔍 Load Testing Details

### Load Test Configuration

- **Concurrent Users**: 50 (configurable)
- **Requests per User**: 20
- **Total Requests**: 1,000
- **Test Duration**: ~15 minutes

### Performance Metrics Tracked

- **Success Rate**: Percentage of successful requests
- **Response Time**: Average response time per request
- **Throughput**: Requests per second
- **Error Rate**: Percentage of failed requests

### Performance Thresholds

- **Success Rate**: ≥ 95% (Excellent), ≥ 90% (Good), < 90% (Poor)
- **Response Time**: ≤ 1.0s (Excellent), ≤ 2.0s (Acceptable), > 2.0s (Poor)
- **Throughput**: ≥ 100 RPS (Good), < 100 RPS (Needs optimization)

## 📈 Monitoring and Observability

### Metrics Collected

- **HTTP Requests**: Request count, duration, status codes
- **JVM Metrics**: Memory usage, CPU usage, garbage collection
- **Database Metrics**: Connection pool, query performance
- **Custom Metrics**: Business logic metrics, error rates

### Monitoring Tools

- **Prometheus**: Metrics collection and storage
- **Grafana**: Visualization and alerting
- **Application Logs**: Structured logging with correlation IDs

### Key Dashboards

- **Service Health**: Overall system health status
- **Performance**: Response times and throughput
- **Errors**: Error rates and types
- **Infrastructure**: Resource utilization

## 🐛 Troubleshooting

### Common Issues

#### Services Not Starting

```bash
# Check Docker status
docker ps

# Check service logs
docker-compose logs -f [service-name]

# Restart services
docker-compose restart
```

#### Database Connection Issues

```bash
# Check database containers
docker ps | grep postgres

# Test database connectivity
nc -z localhost 5433  # User DB
nc -z localhost 5435  # Auth DB
nc -z localhost 5436  # Order DB
nc -z localhost 5437  # Inventory DB
nc -z localhost 5434  # Notification DB
```

#### High Memory Usage

```bash
# Check memory usage
docker stats

# Increase Docker memory limit
# Docker Desktop -> Settings -> Resources -> Memory
```

#### Test Failures

```bash
# Check specific service health
curl http://localhost:8080/actuator/health

# Check service logs
docker-compose logs [service-name]

# Run individual test for debugging
./test-microservices.sh
```

### Performance Issues

#### Slow Response Times

1. Check database performance
2. Verify Redis connectivity
3. Monitor JVM memory usage
4. Check network latency

#### High Error Rates

1. Check service dependencies
2. Verify configuration
3. Monitor resource usage
4. Check for memory leaks

## 📋 Test Results Interpretation

### Success Criteria

- **All Services Healthy**: All microservices must be UP
- **API Endpoints Working**: All REST endpoints must respond correctly
- **Database Connectivity**: All databases must be accessible
- **Performance Metrics**: Response times within acceptable limits
- **Error Handling**: Proper error responses for invalid requests

### Failure Scenarios

- **Service Unavailable**: Service not responding to health checks
- **Database Issues**: Connection failures or query timeouts
- **Performance Degradation**: Response times exceeding thresholds
- **Data Inconsistency**: Data not synchronized across services

## 🔧 Customization

### Modifying Test Parameters

Edit the test scripts to customize:

- **Concurrent Users**: Change `CONCURRENT_USERS` variable
- **Request Count**: Modify `REQUESTS_PER_USER` variable
- **Test Duration**: Adjust sleep intervals
- **Performance Thresholds**: Update success criteria

### Adding New Test Scenarios

1. Create new test function
2. Add to main execution flow
3. Update test reporting
4. Document new scenario

### Environment-Specific Testing

- **Development**: Use `env.dev.example`
- **Staging**: Create `env.staging`
- **Production**: Use production environment variables

## 📚 Best Practices

### Before Running Tests

1. Ensure all services are running
2. Check available system resources
3. Verify network connectivity
4. Review test configuration

### During Test Execution

1. Monitor system resources
2. Watch for error messages
3. Check service logs
4. Monitor performance metrics

### After Test Completion

1. Review test results
2. Analyze performance metrics
3. Check for any errors
4. Document findings

## 🚨 Production Readiness Checklist

- [ ] All services are healthy and responding
- [ ] Database connections are stable
- [ ] Performance metrics meet requirements
- [ ] Error handling is working correctly
- [ ] Monitoring and alerting are configured
- [ ] Logging is properly structured
- [ ] Security configurations are in place
- [ ] Backup and recovery procedures are tested
- [ ] Load testing shows acceptable performance
- [ ] All integration tests pass

## 📞 Support

For issues or questions:

1. Check the troubleshooting section
2. Review service logs
3. Check Docker and system resources
4. Verify network connectivity
5. Review configuration files

## 🔄 Continuous Testing

### Automated Testing

- Set up CI/CD pipeline
- Run tests on every deployment
- Monitor test results over time
- Alert on test failures

### Regular Testing Schedule

- **Daily**: Basic health checks
- **Weekly**: Full test suite
- **Before Releases**: Complete production simulation
- **After Changes**: Targeted testing

This comprehensive testing suite ensures your microservices architecture is production-ready and performs reliably under real-world conditions.
