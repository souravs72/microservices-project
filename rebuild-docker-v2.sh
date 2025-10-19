#!/bin/bash

# Docker Rebuild and Restart Script v2
# Uses modern 'docker compose' command instead of 'docker-compose'

set -e  # Exit on any error

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

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
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

# Function to stop and remove existing containers
cleanup_containers() {
    print_status "Stopping and removing existing containers..."
    
    # Try docker compose first, fallback to docker-compose
    if command -v docker > /dev/null 2>&1 && docker compose version > /dev/null 2>&1; then
        docker compose down --remove-orphans 2>/dev/null || true
    elif command -v docker-compose > /dev/null 2>&1; then
        docker-compose down --remove-orphans 2>/dev/null || true
    else
        print_error "Neither 'docker compose' nor 'docker-compose' is available"
        exit 1
    fi
    
    # Remove any dangling containers
    docker container prune -f 2>/dev/null || true
    
    print_success "Containers cleaned up"
}

# Function to remove old images (optional)
cleanup_images() {
    if [ "$1" = "--clean-images" ]; then
        print_status "Removing old images..."
        
        # Remove dangling images
        docker image prune -f 2>/dev/null || true
        
        # Remove images for our services
        docker rmi $(docker images -q "microservices*") 2>/dev/null || true
        
        print_success "Old images cleaned up"
    fi
}

# Function to build and start services
build_and_start() {
    print_status "Building and starting services..."
    
    # Source environment variables
    source .env
    
    # Try docker compose first, fallback to docker-compose
    if command -v docker > /dev/null 2>&1 && docker compose version > /dev/null 2>&1; then
        print_status "Using 'docker compose' command..."
        docker compose up --build -d
    elif command -v docker-compose > /dev/null 2>&1; then
        print_status "Using 'docker-compose' command..."
        docker-compose up --build -d
    else
        print_error "Neither 'docker compose' nor 'docker-compose' is available"
        exit 1
    fi
    
    print_success "Services built and started"
}

# Function to wait for services to be healthy
wait_for_services() {
    print_status "Waiting for services to be healthy..."
    
    # Wait for databases
    print_status "Waiting for databases..."
    sleep 10
    
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
    
    # Wait for API Gateway
    print_status "Waiting for API Gateway..."
    timeout 60 bash -c 'until curl -s http://localhost:8080/actuator/health > /dev/null; do sleep 2; done' || {
        print_warning "API Gateway may not be fully ready yet"
    }
    
    print_success "Services are ready"
}

# Function to show service status
show_status() {
    print_status "Service Status:"
    echo ""
    
    # Show running containers
    if command -v docker > /dev/null 2>&1 && docker compose version > /dev/null 2>&1; then
        docker compose ps
    else
        docker-compose ps
    fi
    
    echo ""
    print_status "Service Health Checks:"
    
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
    
    # Check frontend
    if curl -s http://localhost:3000 > /dev/null 2>&1; then
        print_success "✅ Frontend (3000) - Running"
    else
        print_warning "⚠️  Frontend (3000) - Not responding (may be optional)"
    fi
}

# Function to test JWT token validation
test_jwt_validation() {
    print_status "Testing JWT token validation..."
    
    # Login to get a token
    LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8082/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username": "admin", "password": "admin123"}')
    
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
    if command -v docker > /dev/null 2>&1 && docker compose version > /dev/null 2>&1; then
        docker compose logs --tail=20
    else
        docker-compose logs --tail=20
    fi
}

# Function to show help
show_help() {
    echo "Docker Rebuild and Restart Script v2"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --clean-images    Also remove old Docker images"
    echo "  --logs           Show logs after starting"
    echo "  --test           Test JWT token validation after starting"
    echo "  --status         Show service status only"
    echo "  --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                    # Rebuild and restart all services"
    echo "  $0 --clean-images     # Rebuild with clean images"
    echo "  $0 --test            # Rebuild and test JWT validation"
    echo "  $0 --status          # Show current service status"
}

# Main execution
main() {
    echo "🐳 Docker Rebuild and Restart Script v2"
    echo "======================================"
    echo ""
    
    # Parse command line arguments
    CLEAN_IMAGES=false
    SHOW_LOGS=false
    RUN_TEST=false
    SHOW_STATUS_ONLY=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --clean-images)
                CLEAN_IMAGES=true
                shift
                ;;
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
    
    if [ "$SHOW_STATUS_ONLY" = true ]; then
        show_status
        exit 0
    fi
    
    # Run the rebuild process
    check_docker
    check_env_file
    cleanup_containers
    cleanup_images $([ "$CLEAN_IMAGES" = true ] && echo "--clean-images")
    build_and_start
    wait_for_services
    show_status
    
    if [ "$RUN_TEST" = true ]; then
        test_jwt_validation
    fi
    
    if [ "$SHOW_LOGS" = true ]; then
        show_logs
    fi
    
    echo ""
    print_success "🎉 Docker rebuild and restart completed successfully!"
    echo ""
    print_status "Services are now running:"
    print_status "  • Auth Service: http://localhost:8082"
    print_status "  • User Service: http://localhost:8081"
    print_status "  • API Gateway: http://localhost:8080"
    print_status "  • Notification Service: http://localhost:8083"
    print_status "  • Frontend: http://localhost:3000"
    print_status "  • H2 Console (Auth): http://localhost:8082/h2-console"
    print_status "  • H2 Console (User): http://localhost:8081/h2-console"
    echo ""
    print_status "To view logs: docker compose logs -f (or docker-compose logs -f)"
    print_status "To stop services: docker compose down (or docker-compose down)"
}

# Run main function with all arguments
main "$@"
