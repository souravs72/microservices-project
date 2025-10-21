#!/bin/bash

# Maven-based Rebuild Script
# Alternative to Docker for development

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Function to check if .env file exists
check_env_file() {
    if [ ! -f ".env" ]; then
        print_warning ".env file not found. Creating from env.dev.example..."
        if [ -f "env.dev.example" ]; then
            cp env.dev.example .env
            print_success ".env file created from env.dev.example"
        else
            print_error "env.dev.example file not found. Please create a .env file manually."
            exit 1
        fi
    else
        print_success ".env file found"
    fi
}

# Function to kill existing Java processes
kill_java_processes() {
    print_status "Stopping existing Java processes..."
    
    # Kill auth service
    pkill -f "auth-service" 2>/dev/null || true
    
    # Kill user service
    pkill -f "user-service" 2>/dev/null || true
    
    # Kill API gateway
    pkill -f "api-gateway" 2>/dev/null || true
    
    # Kill notification service
    pkill -f "notification-service" 2>/dev/null || true
    
    print_success "Java processes stopped"
}

# Function to build services
build_services() {
    print_status "Building services with Maven..."
    
    # Build auth service
    print_status "Building auth service..."
    cd auth-service
    mvn clean package -DskipTests
    cd ..
    
    # Build user service
    print_status "Building user service..."
    cd user-service
    mvn clean package -DskipTests
    cd ..
    
    # Build API gateway
    print_status "Building API gateway..."
    cd api-gateway
    mvn clean package -DskipTests
    cd ..
    
    # Build notification service
    print_status "Building notification service..."
    cd notification-service
    mvn clean package -DskipTests
    cd ..
    
    print_success "All services built successfully"
}

# Function to start services
start_services() {
    print_status "Starting services..."
    
    # Source environment variables
    source .env
    
    # Start auth service
    print_status "Starting auth service on port 8082..."
    cd auth-service
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/auth-service-maven.log 2>&1 &
    cd ..
    
    # Wait a bit for auth service to start
    sleep 10
    
    # Start user service
    print_status "Starting user service on port 8081..."
    cd user-service
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/user-service-maven.log 2>&1 &
    cd ..
    
    # Wait a bit for user service to start
    sleep 10
    
    # Start API gateway
    print_status "Starting API gateway on port 8080..."
    cd api-gateway
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/api-gateway-maven.log 2>&1 &
    cd ..
    
    # Wait a bit for API gateway to start
    sleep 10
    
    # Start notification service
    print_status "Starting notification service on port 8083..."
    cd notification-service
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/notification-service-maven.log 2>&1 &
    cd ..
    
    print_success "All services started"
}

# Function to wait for services
wait_for_services() {
    print_status "Waiting for services to be ready..."
    
    # Wait for auth service
    print_status "Waiting for auth service..."
    timeout 60 bash -c 'until curl -s http://localhost:8082/actuator/health > /dev/null; do sleep 2; done' || {
        print_warning "Auth service may not be fully ready yet"
    }
    
    # Wait for user service
    print_status "Waiting for user service..."
    timeout 60 bash -c 'until curl -s http://localhost:8081/actuator/health > /dev/null; do sleep 2; done' || {
        print_warning "User service may not be fully ready yet"
    }
    
    # Wait for API gateway
    print_status "Waiting for API gateway..."
    timeout 60 bash -c 'until curl -s http://localhost:8080/actuator/health > /dev/null; do sleep 2; done' || {
        print_warning "API gateway may not be fully ready yet"
    }
    
    print_success "Services are ready"
}

# Function to show service status
show_status() {
    print_status "Service Status:"
    echo ""
    
    # Check auth service
    if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
        print_success "✅ Auth Service (8082) - Running"
    else
        print_error "❌ Auth Service (8082) - Not responding"
    fi
    
    # Check user service
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        print_success "✅ User Service (8081) - Running"
    else
        print_error "❌ User Service (8081) - Not responding"
    fi
    
    # Check API Gateway
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_success "✅ API Gateway (8080) - Running"
    else
        print_error "❌ API Gateway (8080) - Not responding"
    fi
    
    # Check notification service
    if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
        print_success "✅ Notification Service (8083) - Running"
    else
        print_warning "⚠️  Notification Service (8083) - Not responding (may be optional)"
    fi
}

# Function to test JWT token validation
test_jwt_validation() {
    print_status "Testing JWT token validation..."
    
    # Login to get a token
    LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8082/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username": "admin", "password": "'${ADMIN_PASSWORD:-admin123}'"}')
    
    if echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
        print_success "✅ Login successful"
        
        # Extract token
        TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
        
        # Test token validation with user service
        USER_RESPONSE=$(curl -s -X GET http://localhost:8081/api/users \
            -H "Authorization: Bearer $TOKEN")
        
        if echo "$USER_RESPONSE" | grep -q "username"; then
            print_success "✅ JWT token validation working correctly"
        else
            print_error "❌ JWT token validation failed"
            echo "User service response: $USER_RESPONSE"
        fi
    else
        print_error "❌ Login failed"
        echo "Login response: $LOGIN_RESPONSE"
    fi
}

# Function to show logs
show_logs() {
    print_status "Showing recent logs..."
    echo ""
    
    if [ -f "logs/auth-service-maven.log" ]; then
        echo "=== Auth Service Logs ==="
        tail -10 logs/auth-service-maven.log
        echo ""
    fi
    
    if [ -f "logs/user-service-maven.log" ]; then
        echo "=== User Service Logs ==="
        tail -10 logs/user-service-maven.log
        echo ""
    fi
    
    if [ -f "logs/api-gateway-maven.log" ]; then
        echo "=== API Gateway Logs ==="
        tail -10 logs/api-gateway-maven.log
        echo ""
    fi
}

# Function to show help
show_help() {
    echo "Maven-based Rebuild Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --logs           Show logs after starting"
    echo "  --test           Test JWT token validation after starting"
    echo "  --status         Show service status only"
    echo "  --stop           Stop all services"
    echo "  --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                    # Rebuild and restart all services"
    echo "  $0 --test            # Rebuild and test JWT validation"
    echo "  $0 --status          # Show current service status"
    echo "  $0 --stop            # Stop all services"
}

# Main execution
main() {
    echo "☕ Maven-based Rebuild Script"
    echo "============================"
    echo ""
    
    # Parse command line arguments
    SHOW_LOGS=false
    RUN_TEST=false
    SHOW_STATUS_ONLY=false
    STOP_SERVICES=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --logs)
                SHOW_LOGS=true
                shift
                ;;
            --test)
                RUN_TEST=true
                shift
                ;;
            --status)
                SHOW_STATUS_ONLY=true
                shift
                ;;
            --stop)
                STOP_SERVICES=true
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
    
    if [ "$STOP_SERVICES" = true ]; then
        kill_java_processes
        print_success "All services stopped"
        exit 0
    fi
    
    if [ "$SHOW_STATUS_ONLY" = true ]; then
        show_status
        exit 0
    fi
    
    # Run the rebuild process
    check_env_file
    kill_java_processes
    build_services
    start_services
    wait_for_services
    show_status
    
    if [ "$RUN_TEST" = true ]; then
        test_jwt_validation
    fi
    
    if [ "$SHOW_LOGS" = true ]; then
        show_logs
    fi
    
    echo ""
    print_success "🎉 Maven rebuild and restart completed successfully!"
    echo ""
    print_status "Services are now running:"
    print_status "  • Auth Service: http://localhost:8082"
    print_status "  • User Service: http://localhost:8081"
    print_status "  • API Gateway: http://localhost:8080"
    print_status "  • Notification Service: http://localhost:8083"
    print_status "  • H2 Console (Auth): http://localhost:8082/h2-console"
    print_status "  • H2 Console (User): http://localhost:8081/h2-console"
    echo ""
    print_status "To view logs: tail -f logs/*-maven.log"
    print_status "To stop services: $0 --stop"
}

# Run main function with all arguments
main "$@"
