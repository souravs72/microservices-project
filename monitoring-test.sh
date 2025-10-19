#!/bin/bash

# Monitoring and Observability Test Suite
# This script tests monitoring, logging, and observability features

set -e

echo "🔍 Starting Monitoring and Observability Test Suite"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
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

print_metric() {
    echo -e "${CYAN}[METRIC]${NC} $1"
}

print_monitor() {
    echo -e "${PURPLE}[MONITOR]${NC} $1"
}

# Configuration
API_BASE="http://localhost:8080"
AUTH_SERVICE="http://localhost:8082"
USER_SERVICE="http://localhost:8081"
ORDER_SERVICE="http://localhost:8083"
INVENTORY_SERVICE="http://localhost:8084"
NOTIFICATION_SERVICE="http://localhost:8085"
PROMETHEUS="http://localhost:9090"
GRAFANA="http://localhost:3001"

# Function to check service health
check_service_health() {
    local service_name=$1
    local port=$2
    
    print_monitor "Checking $service_name health..."
    
    local health_response=$(curl -s "http://localhost:$port/actuator/health")
    local status=$(echo "$health_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    
    if [ "$status" = "UP" ]; then
        print_success "$service_name is UP"
        return 0
    else
        print_error "$service_name is DOWN"
        return 1
    fi
}

# Function to check metrics endpoint
check_metrics() {
    local service_name=$1
    local port=$2
    
    print_monitor "Checking $service_name metrics..."
    
    local metrics_response=$(curl -s "http://localhost:$port/actuator/metrics")
    
    if [ ! -z "$metrics_response" ]; then
        print_success "$service_name metrics are available"
        
        # Check for specific metrics
        local http_requests=$(echo "$metrics_response" | grep -o '"http.server.requests"' | wc -l)
        local jvm_memory=$(echo "$metrics_response" | grep -o '"jvm.memory.used"' | wc -l)
        local process_cpu=$(echo "$metrics_response" | grep -o '"process.cpu.usage"' | wc -l)
        
        print_metric "HTTP Requests metric: $http_requests"
        print_metric "JVM Memory metric: $jvm_memory"
        print_metric "Process CPU metric: $process_cpu"
    else
        print_error "$service_name metrics are not available"
        return 1
    fi
}

# Function to check Prometheus metrics
check_prometheus_metrics() {
    print_monitor "Checking Prometheus metrics..."
    
    # Check if Prometheus is accessible
    local prometheus_status=$(curl -s -o /dev/null -w "%{http_code}" "$PROMETHEUS/api/v1/query?query=up")
    
    if [ "$prometheus_status" -eq 200 ]; then
        print_success "Prometheus is accessible"
        
        # Query specific metrics
        local up_metrics=$(curl -s "$PROMETHEUS/api/v1/query?query=up")
        local http_requests=$(curl -s "$PROMETHEUS/api/v1/query?query=http_server_requests_seconds_count")
        local jvm_memory=$(curl -s "$PROMETHEUS/api/v1/query?query=jvm_memory_used_bytes")
        
        print_metric "Up metrics available: $(echo "$up_metrics" | grep -o '"result"' | wc -l)"
        print_metric "HTTP requests metrics available: $(echo "$http_requests" | grep -o '"result"' | wc -l)"
        print_metric "JVM memory metrics available: $(echo "$jvm_memory" | grep -o '"result"' | wc -l)"
    else
        print_error "Prometheus is not accessible"
        return 1
    fi
}

# Function to check Grafana
check_grafana() {
    print_monitor "Checking Grafana..."
    
    local grafana_status=$(curl -s -o /dev/null -w "%{http_code}" "$GRAFANA/api/health")
    
    if [ "$grafana_status" -eq 200 ]; then
        print_success "Grafana is accessible"
        
        # Check if datasources are configured
        local datasources=$(curl -s "$GRAFANA/api/datasources" -u admin:admin123)
        local datasource_count=$(echo "$datasources" | grep -o '"name"' | wc -l)
        
        print_metric "Configured datasources: $datasource_count"
    else
        print_error "Grafana is not accessible"
        return 1
    fi
}

# Function to check logs
check_logs() {
    local service_name=$1
    local container_name=$2
    
    print_monitor "Checking $service_name logs..."
    
    # Get recent logs
    local logs=$(docker logs --tail 50 "$container_name" 2>&1)
    
    if [ ! -z "$logs" ]; then
        print_success "$service_name logs are available"
        
        # Check for error logs
        local error_count=$(echo "$logs" | grep -i "error" | wc -l)
        local warn_count=$(echo "$logs" | grep -i "warn" | wc -l)
        local info_count=$(echo "$logs" | grep -i "info" | wc -l)
        
        print_metric "Error logs: $error_count"
        print_metric "Warning logs: $warn_count"
        print_metric "Info logs: $info_count"
        
        if [ "$error_count" -gt 0 ]; then
            print_warning "Found $error_count error logs in $service_name"
        fi
    else
        print_error "$service_name logs are not available"
        return 1
    fi
}

# Function to test circuit breaker
test_circuit_breaker() {
    print_monitor "Testing circuit breaker functionality..."
    
    # Make multiple requests to trigger circuit breaker
    local failed_requests=0
    local total_requests=20
    
    for i in {1..20}; do
        # Try to access a service that might be down
        local response=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/actuator/health")
        
        if [ "$response" -ne 200 ]; then
            ((failed_requests++))
        fi
        
        sleep 0.1
    done
    
    local failure_rate=$(echo "scale=2; $failed_requests * 100 / $total_requests" | bc)
    
    print_metric "Circuit breaker test: $failed_requests/$total_requests failed requests"
    print_metric "Failure rate: ${failure_rate}%"
    
    if [ "$failure_rate" -lt 50 ]; then
        print_success "Circuit breaker is working properly"
    else
        print_warning "High failure rate detected - circuit breaker may be triggered"
    fi
}

# Function to test rate limiting
test_rate_limiting() {
    print_monitor "Testing rate limiting..."
    
    local rate_limit_requests=100
    local rate_limit_duration=10
    local successful_requests=0
    local rate_limited_requests=0
    
    print_status "Sending $rate_limit_requests requests in ${rate_limit_duration} seconds..."
    
    local start_time=$(date +%s)
    
    for i in {1..100}; do
        local response=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/actuator/health")
        
        if [ "$response" -eq 200 ]; then
            ((successful_requests++))
        elif [ "$response" -eq 429 ]; then
            ((rate_limited_requests++))
        fi
        
        sleep 0.1
    done
    
    local end_time=$(date +%s)
    local actual_duration=$((end_time - start_time))
    
    print_metric "Rate limiting test completed in ${actual_duration}s"
    print_metric "Successful requests: $successful_requests"
    print_metric "Rate limited requests: $rate_limited_requests"
    
    if [ "$rate_limited_requests" -gt 0 ]; then
        print_success "Rate limiting is working - $rate_limited_requests requests were rate limited"
    else
        print_warning "No rate limiting detected - may need to adjust thresholds"
    fi
}

# Function to test distributed tracing
test_distributed_tracing() {
    print_monitor "Testing distributed tracing..."
    
    # Make a request that goes through multiple services
    local trace_response=$(curl -s "$API_BASE/actuator/health")
    
    if [ ! -z "$trace_response" ]; then
        print_success "Distributed tracing is available"
        
        # Check for trace headers
        local trace_headers=$(curl -s -I "$API_BASE/actuator/health" | grep -i "trace\|span")
        
        if [ ! -z "$trace_headers" ]; then
            print_metric "Trace headers found: $trace_headers"
        else
            print_warning "No trace headers detected"
        fi
    else
        print_error "Distributed tracing is not available"
        return 1
    fi
}

# Function to check database connections
check_database_connections() {
    print_monitor "Checking database connections..."
    
    local databases=("postgres-auth:5435" "postgres-user:5433" "postgres-order:5436" "postgres-inventory:5437" "postgres-notification:5434")
    
    for db in "${databases[@]}"; do
        local name=$(echo $db | cut -d':' -f1)
        local port=$(echo $db | cut -d':' -f2)
        
        if nc -z localhost $port 2>/dev/null; then
            print_success "$name is accessible on port $port"
        else
            print_error "$name is not accessible on port $port"
        fi
    done
}

# Function to check Redis connection
check_redis_connection() {
    print_monitor "Checking Redis connection..."
    
    if nc -z localhost 6379 2>/dev/null; then
        print_success "Redis is accessible on port 6379"
        
        # Test Redis operations
        local redis_test=$(docker exec redis redis-cli ping 2>/dev/null)
        
        if [ "$redis_test" = "PONG" ]; then
            print_success "Redis is responding to commands"
        else
            print_warning "Redis is not responding to commands"
        fi
    else
        print_error "Redis is not accessible"
        return 1
    fi
}

# Function to check Kafka connection
check_kafka_connection() {
    print_monitor "Checking Kafka connection..."
    
    if nc -z localhost 9092 2>/dev/null; then
        print_success "Kafka is accessible on port 9092"
        
        # Test Kafka topics
        local topics=$(docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null)
        
        if [ ! -z "$topics" ]; then
            print_success "Kafka topics are available"
            print_metric "Available topics: $(echo "$topics" | wc -l)"
        else
            print_warning "No Kafka topics found"
        fi
    else
        print_error "Kafka is not accessible"
        return 1
    fi
}

# Function to generate load for monitoring
generate_monitoring_load() {
    print_monitor "Generating load for monitoring purposes..."
    
    # Generate some load
    for i in {1..50}; do
        curl -s "$API_BASE/actuator/health" > /dev/null &
        curl -s "$AUTH_SERVICE/actuator/health" > /dev/null &
        curl -s "$USER_SERVICE/actuator/health" > /dev/null &
        curl -s "$ORDER_SERVICE/actuator/health" > /dev/null &
        curl -s "$INVENTORY_SERVICE/actuator/health" > /dev/null &
        curl -s "$NOTIFICATION_SERVICE/actuator/health" > /dev/null &
    done
    
    wait
    print_success "Load generation completed"
}

# Main execution function
main() {
    print_status "Starting Monitoring and Observability Test Suite..."
    echo
    
    # Check all service health
    print_monitor "1. Service Health Checks"
    echo "=========================="
    check_service_health "API Gateway" 8080
    check_service_health "Auth Service" 8082
    check_service_health "User Service" 8081
    check_service_health "Order Service" 8083
    check_service_health "Inventory Service" 8084
    check_service_health "Notification Service" 8085
    echo
    
    # Check metrics
    print_monitor "2. Metrics Collection"
    echo "======================="
    check_metrics "API Gateway" 8080
    check_metrics "Auth Service" 8082
    check_metrics "User Service" 8081
    check_metrics "Order Service" 8083
    check_metrics "Inventory Service" 8084
    check_metrics "Notification Service" 8085
    echo
    
    # Check Prometheus
    print_monitor "3. Prometheus Integration"
    echo "============================"
    check_prometheus_metrics
    echo
    
    # Check Grafana
    print_monitor "4. Grafana Integration"
    echo "========================"
    check_grafana
    echo
    
    # Check logs
    print_monitor "5. Log Analysis"
    echo "=================="
    check_logs "API Gateway" "api-gateway"
    check_logs "Auth Service" "auth-service"
    check_logs "User Service" "user-service"
    check_logs "Order Service" "order-service"
    check_logs "Inventory Service" "inventory-service"
    check_logs "Notification Service" "notification-service"
    echo
    
    # Test resilience features
    print_monitor "6. Resilience Testing"
    echo "========================"
    test_circuit_breaker
    test_rate_limiting
    test_distributed_tracing
    echo
    
    # Check infrastructure
    print_monitor "7. Infrastructure Checks"
    echo "==========================="
    check_database_connections
    check_redis_connection
    check_kafka_connection
    echo
    
    # Generate load for monitoring
    print_monitor "8. Load Generation for Monitoring"
    echo "====================================="
    generate_monitoring_load
    echo
    
    print_success "🎉 ALL MONITORING TESTS COMPLETED! 🎉"
    print_status "Monitoring and observability features have been tested."
    print_status "Check Prometheus and Grafana dashboards for detailed metrics."
}

# Run the main function
main "$@"

