#!/bin/bash

# Master Production Test Runner
# This script orchestrates all production simulation tests

set -e

echo "🚀 Master Production Test Runner"
echo "==============================="

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

print_test() {
    echo -e "${PURPLE}[TEST]${NC} $1"
}

print_phase() {
    echo -e "${CYAN}[PHASE]${NC} $1"
}

# Test results tracking
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to run a test and track results
run_test() {
    local test_name=$1
    local test_script=$2
    local test_args=$3
    
    print_test "Running: $test_name"
    echo "=========================================="
    
    ((TOTAL_TESTS++))
    
    if [ -f "$test_script" ]; then
        if bash "$test_script" $test_args; then
            print_success "$test_name - PASSED"
            ((PASSED_TESTS++))
        else
            print_error "$test_name - FAILED"
            ((FAILED_TESTS++))
        fi
    else
        print_error "Test script not found: $test_script"
        ((FAILED_TESTS++))
    fi
    
    echo
}

# Function to check prerequisites
check_prerequisites() {
    print_phase "Checking Prerequisites"
    echo "========================="
    
    # Check if Docker is running
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    print_success "Docker is running"
    
    # Check if Docker Compose is available
    if ! command -v docker-compose > /dev/null 2>&1; then
        print_error "Docker Compose is not installed."
        exit 1
    fi
    print_success "Docker Compose is available"
    
    # Check if required tools are available
    local required_tools=("curl" "jq" "bc" "nc")
    for tool in "${required_tools[@]}"; do
        if ! command -v $tool > /dev/null 2>&1; then
            print_error "Required tool not found: $tool"
            exit 1
        fi
        print_success "$tool is available"
    done
    
    print_success "All prerequisites are met"
    echo
}

# Function to start services
start_services() {
    print_phase "Starting Microservices"
    echo "========================="
    
    print_status "Starting all microservices with Docker Compose..."
    
    if docker-compose up -d; then
        print_success "All services started successfully"
    else
        print_error "Failed to start services"
        exit 1
    fi
    
    print_status "Waiting for services to be ready..."
    sleep 30
    
    print_success "Services are ready for testing"
    echo
}

# Function to run basic health checks
run_health_checks() {
    print_phase "Basic Health Checks"
    echo "====================="
    
    local services=("API Gateway:8080" "Auth Service:8082" "User Service:8081" "Order Service:8083" "Inventory Service:8084" "Notification Service:8085")
    
    for service in "${services[@]}"; do
        local name=$(echo $service | cut -d':' -f1)
        local port=$(echo $service | cut -d':' -f2)
        
        print_status "Checking $name..."
        
        local max_attempts=30
        local attempt=1
        
        while [ $attempt -le $max_attempts ]; do
            if curl -s -f "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
                print_success "$name is healthy"
                break
            fi
            
            if [ $attempt -eq $max_attempts ]; then
                print_error "$name failed to become healthy"
                return 1
            fi
            
            print_status "Attempt $attempt/$max_attempts - $name not ready yet..."
            sleep 10
            ((attempt++))
        done
    done
    
    print_success "All services are healthy"
    echo
}

# Function to run all test suites
run_all_tests() {
    print_phase "Running All Test Suites"
    echo "========================="
    
    # Test 1: Basic functionality test
    run_test "Basic Functionality Test" "./test-microservices.sh"
    
    # Test 2: Production simulation test
    run_test "Production Simulation Test" "./production-simulation-test.sh"
    
    # Test 3: Monitoring and observability test
    run_test "Monitoring and Observability Test" "./monitoring-test.sh"
    
    # Test 4: Load testing (optional - can be skipped if too intensive)
    if [ "$1" = "--include-load-tests" ]; then
        run_test "Load Testing Simulation" "./load-test-simulation.sh"
    else
        print_warning "Skipping load tests (use --include-load-tests to enable)"
    fi
}

# Function to generate test report
generate_test_report() {
    print_phase "Test Report"
    echo "==========="
    
    local success_rate=$(echo "scale=2; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc)
    
    print_status "Test Summary:"
    print_status "  Total Tests: $TOTAL_TESTS"
    print_status "  Passed: $PASSED_TESTS"
    print_status "  Failed: $FAILED_TESTS"
    print_status "  Success Rate: ${success_rate}%"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        print_success "🎉 ALL TESTS PASSED! 🎉"
        print_success "The microservices architecture is production-ready!"
    else
        print_warning "⚠️  Some tests failed. Please review the output above."
        print_warning "The system may need attention before production deployment."
    fi
    
    echo
    print_status "Service URLs:"
    print_status "  - API Gateway: http://localhost:8080"
    print_status "  - Frontend: http://localhost:5173"
    print_status "  - Prometheus: http://localhost:9090"
    print_status "  - Grafana: http://localhost:3001"
    echo
    print_status "Swagger Documentation:"
    print_status "  - Auth Service: http://localhost:8082/swagger-ui.html"
    print_status "  - User Service: http://localhost:8081/swagger-ui.html"
    print_status "  - Order Service: http://localhost:8083/swagger-ui.html"
    print_status "  - Inventory Service: http://localhost:8084/swagger-ui.html"
    print_status "  - Notification Service: http://localhost:8085/swagger-ui.html"
}

# Function to cleanup
cleanup() {
    print_phase "Cleanup"
    echo "======="
    
    if [ "$1" = "--cleanup" ]; then
        print_status "Stopping all services..."
        docker-compose down
        print_success "All services stopped"
    else
        print_status "Services are still running. Use --cleanup to stop them."
    fi
}

# Function to show help
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  --include-load-tests    Include intensive load testing"
    echo "  --cleanup              Stop services after testing"
    echo "  --help                 Show this help message"
    echo
    echo "Examples:"
    echo "  $0                                    # Run basic tests"
    echo "  $0 --include-load-tests              # Run all tests including load tests"
    echo "  $0 --include-load-tests --cleanup    # Run all tests and cleanup"
}

# Main execution function
main() {
    local include_load_tests=false
    local cleanup_after=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --include-load-tests)
                include_load_tests=true
                shift
                ;;
            --cleanup)
                cleanup_after=true
                shift
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    print_status "Starting Master Production Test Runner..."
    print_status "Include load tests: $include_load_tests"
    print_status "Cleanup after tests: $cleanup_after"
    echo
    
    # Run all phases
    check_prerequisites
    start_services
    run_health_checks
    run_all_tests $([ "$include_load_tests" = true ] && echo "--include-load-tests")
    generate_test_report
    cleanup $([ "$cleanup_after" = true ] && echo "--cleanup")
    
    print_status "Master Production Test Runner completed!"
}

# Run the main function
main "$@"

