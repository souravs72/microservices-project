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

# Function to clean Kafka volumes (fixes cluster ID mismatch issues)
cleanup_kafka_volumes() {
    print_status "Cleaning Kafka volumes to prevent cluster ID mismatch..."
    
    # Stop Kafka specifically if running
    docker stop kafka 2>/dev/null || true
    docker rm kafka 2>/dev/null || true
    
    # Remove Kafka volume to clear cluster metadata
    docker volume rm microservices-parent_kafka_data 2>/dev/null || true
    
    print_success "Kafka volumes cleaned up"
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
    
    # Wait for infrastructure services first
    print_status "Waiting for infrastructure services (databases, Redis, Kafka)..."
    sleep 15
    
    # Wait for core services in dependency order
    print_status "Waiting for Auth Service..."
    timeout 90 bash -c 'until curl -s http://localhost:8082/actuator/health > /dev/null; do sleep 3; done' || {
        print_warning "Auth service may not be fully ready yet"
    }
    
    print_status "Waiting for User Service..."
    timeout 90 bash -c 'until curl -s http://localhost:8081/actuator/health > /dev/null; do sleep 3; done' || {
        print_warning "User service may not be fully ready yet"
    }
    
    print_status "Waiting for Inventory Service..."
    timeout 90 bash -c 'until curl -s http://localhost:8084/actuator/health > /dev/null; do sleep 3; done' || {
        print_warning "Inventory service may not be fully ready yet"
    }
    
    print_status "Waiting for Order Service..."
    timeout 90 bash -c 'until curl -s http://localhost:8083/actuator/health > /dev/null; do sleep 3; done' || {
        print_warning "Order service may not be fully ready yet"
    }
    
    print_status "Waiting for Notification Service..."
    timeout 90 bash -c 'until curl -s http://localhost:8085/actuator/health > /dev/null; do sleep 3; done' || {
        print_warning "Notification service may not be fully ready yet"
    }
    
    print_status "Waiting for API Gateway..."
    timeout 90 bash -c 'until curl -s http://localhost:8080/actuator/health > /dev/null; do sleep 3; done' || {
        print_warning "API Gateway may not be fully ready yet"
    }
    
    # Wait for frontend and monitoring services
    print_status "Waiting for Frontend..."
    timeout 60 bash -c 'until curl -s http://localhost:5173 > /dev/null; do sleep 3; done' || {
        print_warning "Frontend may not be fully ready yet"
    }
    
    print_status "Waiting for Prometheus..."
    timeout 60 bash -c 'until curl -s http://localhost:9090 > /dev/null; do sleep 3; done' || {
        print_warning "Prometheus may not be fully ready yet"
    }
    
    print_status "Waiting for Grafana..."
    timeout 60 bash -c 'until curl -s http://localhost:3001 > /dev/null; do sleep 3; done' || {
        print_warning "Grafana may not be fully ready yet"
    }
    
    print_success "Services are ready"
}

# Function to check infrastructure services
check_infrastructure() {
    print_status "Checking infrastructure services..."
    
    # Check databases
    print_status "Checking databases..."
    for db_port in 5433 5434 5435 5436 5437; do
        if docker exec $(docker ps -q --filter "publish=$db_port") pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
            print_success "✅ Database on port $db_port - Running"
        else
            print_warning "⚠️  Database on port $db_port - Not responding"
        fi
    done
    
    # Check Redis
    if docker exec redis redis-cli ping > /dev/null 2>&1; then
        print_success "✅ Redis (6379) - Running"
    else
        print_warning "⚠️  Redis (6379) - Not responding"
    fi
    
    # Check Kafka
    if docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
        print_success "✅ Kafka (9092) - Running"
    else
        print_warning "⚠️  Kafka (9092) - Not responding"
    fi
    
    # Check Zookeeper
    if docker exec zookeeper /bin/bash -c "echo ruok | nc localhost 2181" > /dev/null 2>&1; then
        print_success "✅ Zookeeper (2181) - Running"
    else
        print_warning "⚠️  Zookeeper (2181) - Not responding"
    fi
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
    print_status "Infrastructure Services:"
    check_infrastructure
    
    echo ""
    print_status "Application Services:"
    
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
    
    # Check order service
    if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
        print_success "✅ Order Service (8083) - Running"
    else
        print_warning "⚠️  Order Service (8083) - Not responding (may be optional)"
    fi
    
    # Check inventory service
    if curl -s http://localhost:8084/actuator/health > /dev/null 2>&1; then
        print_success "✅ Inventory Service (8084) - Running"
    else
        print_warning "⚠️  Inventory Service (8084) - Not responding (may be optional)"
    fi
    
    # Check notification service
    if curl -s http://localhost:8085/actuator/health > /dev/null 2>&1; then
        print_success "✅ Notification Service (8085) - Running"
    else
        print_warning "⚠️  Notification Service (8085) - Not responding (may be optional)"
    fi
    
    # Check frontend
    if curl -s http://localhost:5173 > /dev/null 2>&1; then
        print_success "✅ Frontend (5173) - Running"
    else
        print_warning "⚠️  Frontend (5173) - Not responding (may be optional)"
    fi
    
    # Check monitoring services
    if curl -s http://localhost:9090 > /dev/null 2>&1; then
        print_success "✅ Prometheus (9090) - Running"
    else
        print_warning "⚠️  Prometheus (9090) - Not responding (may be optional)"
    fi
    
    if curl -s http://localhost:3001 > /dev/null 2>&1; then
        print_success "✅ Grafana (3001) - Running"
    else
        print_warning "⚠️  Grafana (3001) - Not responding (may be optional)"
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
    echo "  --infra          Check infrastructure services only"
    echo "  --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                    # Rebuild and restart all services"
    echo "  $0 --clean-images     # Rebuild with clean images"
    echo "  $0 --test            # Rebuild and test JWT validation"
    echo "  $0 --status          # Show current service status"
    echo "  $0 --infra           # Check infrastructure services only"
    echo ""
    echo "Kafka Issues:"
    echo "  If you encounter Kafka cluster ID mismatch errors, run:"
    echo "  ./reset-kafka.sh      # Quick Kafka reset"
    echo "  $0                    # Full rebuild (includes Kafka cleanup)"
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
    CHECK_INFRA_ONLY=false
    
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
            --infra)
                CHECK_INFRA_ONLY=true
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
    
    if [ "$CHECK_INFRA_ONLY" = true ]; then
        check_infrastructure
        exit 0
    fi
    
    # Run the rebuild process
    check_docker
    check_env_file
    cleanup_containers
    cleanup_kafka_volumes
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
    print_status "  • API Gateway: http://localhost:8080"
    print_status "  • Auth Service: http://localhost:8082"
    print_status "  • User Service: http://localhost:8081"
    print_status "  • Order Service: http://localhost:8083"
    print_status "  • Inventory Service: http://localhost:8084"
    print_status "  • Notification Service: http://localhost:8085"
    print_status "  • Frontend: http://localhost:5173"
    print_status "  • Prometheus: http://localhost:9090"
    print_status "  • Grafana: http://localhost:3001"
    print_status "  • Kafka: localhost:9092"
    print_status "  • Redis: localhost:6379"
    echo ""
    print_status "To view logs: docker compose logs -f (or docker-compose logs -f)"
    print_status "To stop services: docker compose down (or docker-compose down)"
}

# Run main function with all arguments
main "$@"
