#!/bin/bash

# Consolidated Test Script for Microservices
# Combines functionality from multiple test scripts

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

# Function to test admin login
test_admin_login() {
    print_status "Testing admin login..."
    
    LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8082/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username": "admin", "password": "admin123"}')
    
    if echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
        print_success "✅ Admin login successful"
        TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
        echo "Token: ${TOKEN:0:50}..."
        return 0
    else
        print_error "❌ Admin login failed"
        echo "Response: $LOGIN_RESPONSE"
        return 1
    fi
}

# Function to test JWT token validation
test_jwt_validation() {
    print_status "Testing JWT token validation..."
    
    # Get token from admin login
    LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8082/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username": "admin", "password": "admin123"}')
    
    if ! echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
        print_error "❌ Cannot get token for validation test"
        return 1
    fi
    
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
    
    # Test token validation with user service
    USER_RESPONSE=$(curl -s -X GET http://localhost:8081/api/users \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$USER_RESPONSE" | grep -q "username"; then
        print_success "✅ JWT token validation working correctly"
        return 0
    else
        print_error "❌ JWT token validation failed"
        echo "User service response: $USER_RESPONSE"
        return 1
    fi
}

# Function to test notification service
test_notification_service() {
    print_status "Testing notification service..."
    
    # Check if notification service is running
    if ! curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
        print_warning "⚠️  Notification service not running (port 8083)"
        return 1
    fi
    
    # Test notification service health
    HEALTH_RESPONSE=$(curl -s http://localhost:8083/actuator/health)
    
    if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
        print_success "✅ Notification service is healthy"
        return 0
    else
        print_error "❌ Notification service health check failed"
        echo "Health response: $HEALTH_RESPONSE"
        return 1
    fi
}

# Function to test email functionality
test_email_functionality() {
    print_status "Testing email functionality..."
    
    # Test forgot password endpoint (triggers email)
    EMAIL_RESPONSE=$(curl -s -X POST http://localhost:8082/api/auth/forgot-password \
        -H "Content-Type: application/json" \
        -d '{"email": "admin@example.com"}')
    
    if echo "$EMAIL_RESPONSE" | grep -q "success\|message"; then
        print_success "✅ Email functionality working"
        echo "Response: $EMAIL_RESPONSE"
        return 0
    else
        print_warning "⚠️  Email functionality may not be configured"
        echo "Response: $EMAIL_RESPONSE"
        return 1
    fi
}

# Function to test all services health
test_all_services() {
    print_status "Testing all services health..."
    
    local all_healthy=true
    
    # Test auth service
    if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
        print_success "✅ Auth Service (8082) - Healthy"
    else
        print_error "❌ Auth Service (8082) - Not responding"
        all_healthy=false
    fi
    
    # Test user service
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        print_success "✅ User Service (8081) - Healthy"
    else
        print_error "❌ User Service (8081) - Not responding"
        all_healthy=false
    fi
    
    # Test API Gateway
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_success "✅ API Gateway (8080) - Healthy"
    else
        print_error "❌ API Gateway (8080) - Not responding"
        all_healthy=false
    fi
    
    # Test notification service (optional)
    if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
        print_success "✅ Notification Service (8083) - Healthy"
    else
        print_warning "⚠️  Notification Service (8083) - Not responding (optional)"
    fi
    
    if [ "$all_healthy" = true ]; then
        print_success "🎉 All core services are healthy!"
        return 0
    else
        print_error "❌ Some services are not healthy"
        return 1
    fi
}

# Function to run comprehensive test suite
run_comprehensive_test() {
    print_status "Running comprehensive test suite..."
    echo ""
    
    local tests_passed=0
    local total_tests=5
    
    # Test 1: Service Health
    if test_all_services; then
        ((tests_passed++))
    fi
    echo ""
    
    # Test 2: Admin Login
    if test_admin_login; then
        ((tests_passed++))
    fi
    echo ""
    
    # Test 3: JWT Validation
    if test_jwt_validation; then
        ((tests_passed++))
    fi
    echo ""
    
    # Test 4: Notification Service
    if test_notification_service; then
        ((tests_passed++))
    fi
    echo ""
    
    # Test 5: Email Functionality
    if test_email_functionality; then
        ((tests_passed++))
    fi
    echo ""
    
    # Summary
    print_status "Test Results: $tests_passed/$total_tests tests passed"
    
    if [ $tests_passed -eq $total_tests ]; then
        print_success "🎉 All tests passed! System is working correctly."
        return 0
    elif [ $tests_passed -ge 3 ]; then
        print_warning "⚠️  Most tests passed. Some optional features may not be configured."
        return 1
    else
        print_error "❌ Multiple tests failed. Please check your configuration."
        return 2
    fi
}

# Function to show help
show_help() {
    echo "Consolidated Test Script for Microservices"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --health         Test all services health"
    echo "  --login          Test admin login"
    echo "  --jwt            Test JWT token validation"
    echo "  --notification   Test notification service"
    echo "  --email          Test email functionality"
    echo "  --all            Run comprehensive test suite (default)"
    echo "  --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                    # Run all tests"
    echo "  $0 --health          # Test service health only"
    echo "  $0 --jwt            # Test JWT validation only"
    echo "  $0 --login --jwt    # Test login and JWT validation"
}

# Main execution
main() {
    echo "🧪 Consolidated Test Script for Microservices"
    echo "============================================="
    echo ""
    
    # Parse command line arguments
    RUN_HEALTH=false
    RUN_LOGIN=false
    RUN_JWT=false
    RUN_NOTIFICATION=false
    RUN_EMAIL=false
    RUN_ALL=true
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --health)
                RUN_HEALTH=true
                RUN_ALL=false
                shift
                ;;
            --login)
                RUN_LOGIN=true
                RUN_ALL=false
                shift
                ;;
            --jwt)
                RUN_JWT=true
                RUN_ALL=false
                shift
                ;;
            --notification)
                RUN_NOTIFICATION=true
                RUN_ALL=false
                shift
                ;;
            --email)
                RUN_EMAIL=true
                RUN_ALL=false
                shift
                ;;
            --all)
                RUN_ALL=true
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
    
    # Run selected tests
    if [ "$RUN_ALL" = true ]; then
        run_comprehensive_test
    else
        if [ "$RUN_HEALTH" = true ]; then
            test_all_services
            echo ""
        fi
        
        if [ "$RUN_LOGIN" = true ]; then
            test_admin_login
            echo ""
        fi
        
        if [ "$RUN_JWT" = true ]; then
            test_jwt_validation
            echo ""
        fi
        
        if [ "$RUN_NOTIFICATION" = true ]; then
            test_notification_service
            echo ""
        fi
        
        if [ "$RUN_EMAIL" = true ]; then
            test_email_functionality
            echo ""
        fi
    fi
}

# Run main function with all arguments
main "$@"
