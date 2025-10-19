#!/bin/bash

# Microservices Test Script
# This script tests all microservices to ensure they work together properly

set -e

echo "🚀 Starting Microservices Test Suite"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if a service is healthy
check_service_health() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    print_status "Checking health of $service_name on port $port..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            print_success "$service_name is healthy!"
            return 0
        fi
        
        print_status "Attempt $attempt/$max_attempts - $service_name not ready yet, waiting 10 seconds..."
        sleep 10
        ((attempt++))
    done
    
    print_error "$service_name failed to become healthy after $max_attempts attempts"
    return 1
}

# Function to test API endpoint
test_api_endpoint() {
    local service_name=$1
    local endpoint=$2
    local expected_status=$3
    local description=$4
    
    print_status "Testing $description..."
    
    local response_code=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:$endpoint")
    
    if [ "$response_code" -eq "$expected_status" ]; then
        print_success "$description - Status: $response_code"
        return 0
    else
        print_error "$description - Expected: $expected_status, Got: $response_code"
        return 1
    fi
}

# Function to test service connectivity
test_service_connectivity() {
    local service_name=$1
    local port=$2
    
    print_status "Testing connectivity to $service_name on port $port..."
    
    if nc -z localhost $port 2>/dev/null; then
        print_success "$service_name is reachable on port $port"
        return 0
    else
        print_error "$service_name is not reachable on port $port"
        return 1
    fi
}

# Main test execution
main() {
    print_status "Starting comprehensive microservices test..."
    
    # Test service connectivity
    print_status "Testing service connectivity..."
    
    test_service_connectivity "API Gateway" 8080
    test_service_connectivity "Auth Service" 8082
    test_service_connectivity "User Service" 8081
    test_service_connectivity "Order Service" 8083
    test_service_connectivity "Inventory Service" 8084
    test_service_connectivity "Notification Service" 8085
    
    # Test service health
    print_status "Testing service health endpoints..."
    
    check_service_health "API Gateway" 8080
    check_service_health "Auth Service" 8082
    check_service_health "User Service" 8081
    check_service_health "Order Service" 8083
    check_service_health "Inventory Service" 8084
    check_service_health "Notification Service" 8085
    
    # Test API endpoints
    print_status "Testing API endpoints..."
    
    # API Gateway endpoints
    test_api_endpoint "API Gateway" "8080/actuator/health" 200 "API Gateway Health Check"
    test_api_endpoint "API Gateway" "8080/swagger-ui.html" 200 "API Gateway Swagger UI"
    
    # Auth Service endpoints
    test_api_endpoint "Auth Service" "8082/actuator/health" 200 "Auth Service Health Check"
    test_api_endpoint "Auth Service" "8082/swagger-ui.html" 200 "Auth Service Swagger UI"
    
    # User Service endpoints
    test_api_endpoint "User Service" "8081/actuator/health" 200 "User Service Health Check"
    test_api_endpoint "User Service" "8081/swagger-ui.html" 200 "User Service Swagger UI"
    
    # Order Service endpoints
    test_api_endpoint "Order Service" "8083/actuator/health" 200 "Order Service Health Check"
    test_api_endpoint "Order Service" "8083/swagger-ui.html" 200 "Order Service Swagger UI"
    
    # Inventory Service endpoints
    test_api_endpoint "Inventory Service" "8084/actuator/health" 200 "Inventory Service Health Check"
    test_api_endpoint "Inventory Service" "8084/swagger-ui.html" 200 "Inventory Service Swagger UI"
    
    # Notification Service endpoints
    test_api_endpoint "Notification Service" "8085/actuator/health" 200 "Notification Service Health Check"
    test_api_endpoint "Notification Service" "8085/swagger-ui.html" 200 "Notification Service Swagger UI"
    
    # Test database connectivity
    print_status "Testing database connectivity..."
    
    # Test PostgreSQL databases
    test_service_connectivity "PostgreSQL Auth" 5435
    test_service_connectivity "PostgreSQL User" 5433
    test_service_connectivity "PostgreSQL Notification" 5434
    test_service_connectivity "PostgreSQL Order" 5436
    test_service_connectivity "PostgreSQL Inventory" 5437
    
    # Test Redis
    test_service_connectivity "Redis" 6379
    
    # Test Kafka
    test_service_connectivity "Kafka" 9092
    
    # Test monitoring services
    print_status "Testing monitoring services..."
    
    test_service_connectivity "Prometheus" 9090
    test_service_connectivity "Grafana" 3001
    
    # Test frontend
    print_status "Testing frontend..."
    
    test_service_connectivity "Frontend" 5173
    
    print_success "All tests completed successfully! 🎉"
    print_status "Microservices are running and healthy."
    print_status "You can access the services at:"
    print_status "  - API Gateway: http://localhost:8080"
    print_status "  - Auth Service: http://localhost:8082"
    print_status "  - User Service: http://localhost:8081"
    print_status "  - Order Service: http://localhost:8083"
    print_status "  - Inventory Service: http://localhost:8084"
    print_status "  - Notification Service: http://localhost:8085"
    print_status "  - Frontend: http://localhost:5173"
    print_status "  - Prometheus: http://localhost:9090"
    print_status "  - Grafana: http://localhost:3001"
}

# Run the main function
main "$@"

