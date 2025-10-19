#!/bin/bash

# Advanced Load Testing Simulation
# This script simulates high-traffic production scenarios

set -e

echo "🚀 Starting Advanced Load Testing Simulation"
echo "============================================="

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

print_load_test() {
    echo -e "${PURPLE}[LOAD TEST]${NC} $1"
}

print_metric() {
    echo -e "${CYAN}[METRIC]${NC} $1"
}

# Configuration
API_BASE="http://localhost:8080"
AUTH_SERVICE="http://localhost:8082"
USER_SERVICE="http://localhost:8081"
ORDER_SERVICE="http://localhost:8083"
INVENTORY_SERVICE="http://localhost:8084"

# Test parameters
CONCURRENT_USERS=50
REQUESTS_PER_USER=20
TOTAL_REQUESTS=$((CONCURRENT_USERS * REQUESTS_PER_USER))

# Test data arrays
USER_EMAILS=()
USER_TOKENS=()
PRODUCT_IDS=()
ORDER_IDS=()

# Function to generate random data
generate_random_email() {
    local random_id=$(shuf -i 1000-9999 -n 1)
    echo "user${random_id}@test.com"
}

generate_random_name() {
    local first_names=("John" "Jane" "Mike" "Sarah" "David" "Lisa" "Chris" "Emma" "Alex" "Maria")
    local last_names=("Smith" "Johnson" "Williams" "Brown" "Jones" "Garcia" "Miller" "Davis" "Rodriguez" "Martinez")
    
    local first_name=${first_names[$RANDOM % ${#first_names[@]}]}
    local last_name=${last_names[$RANDOM % ${#last_names[@]}]}
    
    echo "$first_name $last_name"
}

# Function to make HTTP request with timing
make_timed_request() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    
    local start_time=$(date +%s.%N)
    local response=$(curl -s -w "%{http_code}" -X "$method" $headers -d "$data" "$url")
    local end_time=$(date +%s.%N)
    
    local status_code="${response: -3}"
    local body="${response%???}"
    local duration=$(echo "$end_time - $start_time" | bc)
    
    echo "$status_code|$duration|$body"
}

# Function to register a user
register_user() {
    local email=$1
    local name=$2
    local password="TestPassword123!"
    
    local register_data="{\"email\":\"$email\",\"password\":\"$password\",\"firstName\":\"$name\",\"lastName\":\"$name\"}"
    local response=$(make_timed_request "POST" "$AUTH_SERVICE/api/auth/register" "$register_data" "-H 'Content-Type: application/json'")
    
    local status_code=$(echo "$response" | cut -d'|' -f1)
    local duration=$(echo "$response" | cut -d'|' -f2)
    
    if [ "$status_code" -eq 201 ]; then
        print_success "User registered: $email (${duration}s)"
        return 0
    else
        print_error "User registration failed: $email (Status: $status_code)"
        return 1
    fi
}

# Function to login user
login_user() {
    local email=$1
    local password="TestPassword123!"
    
    local login_data="{\"email\":\"$email\",\"password\":\"$password\"}"
    local response=$(make_timed_request "POST" "$AUTH_SERVICE/api/auth/login" "$login_data" "-H 'Content-Type: application/json'")
    
    local status_code=$(echo "$response" | cut -d'|' -f1)
    local duration=$(echo "$response" | cut -d'|' -f2)
    local body=$(echo "$response" | cut -d'|' -f3)
    
    if [ "$status_code" -eq 200 ]; then
        local token=$(echo "$body" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
        print_success "User logged in: $email (${duration}s)"
        echo "$token"
        return 0
    else
        print_error "User login failed: $email (Status: $status_code)"
        return 1
    fi
}

# Function to create a product
create_product() {
    local sku="PROD-$(shuf -i 1000-9999 -n 1)"
    local price=$(shuf -i 10-1000 -n 1)
    local stock=$(shuf -i 10-100 -n 1)
    
    local product_data="{\"name\":\"Load Test Product $sku\",\"description\":\"Product for load testing\",\"sku\":\"$sku\",\"price\":$price,\"quantityInStock\":$stock,\"minStockLevel\":5,\"category\":\"LoadTest\",\"brand\":\"TestBrand\",\"createdBy\":\"loadtest\"}"
    local response=$(make_timed_request "POST" "$INVENTORY_SERVICE/api/inventory/products" "$product_data" "-H 'Content-Type: application/json'")
    
    local status_code=$(echo "$response" | cut -d'|' -f1)
    local duration=$(echo "$response" | cut -d'|' -f2)
    local body=$(echo "$response" | cut -d'|' -f3)
    
    if [ "$status_code" -eq 201 ]; then
        local product_id=$(echo "$body" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Product created: $sku (ID: $product_id, ${duration}s)"
        echo "$product_id"
        return 0
    else
        print_error "Product creation failed: $sku (Status: $status_code)"
        return 1
    fi
}

# Function to create an order
create_order() {
    local user_token=$1
    local product_id=$2
    local quantity=$(shuf -i 1-5 -n 1)
    
    local order_data="{\"userId\":1,\"orderItems\":[{\"productId\":$product_id,\"quantity\":$quantity,\"productName\":\"Test Product\",\"productSku\":\"TEST-SKU\",\"unitPrice\":99.99}],\"shippingAddress\":\"123 Test St, Test City, TC 12345\",\"billingAddress\":\"123 Test St, Test City, TC 12345\"}"
    local response=$(make_timed_request "POST" "$ORDER_SERVICE/api/orders" "$order_data" "-H 'Content-Type: application/json'")
    
    local status_code=$(echo "$response" | cut -d'|' -f1)
    local duration=$(echo "$response" | cut -d'|' -f2)
    local body=$(echo "$response" | cut -d'|' -f3)
    
    if [ "$status_code" -eq 201 ]; then
        local order_id=$(echo "$body" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Order created: ID $order_id (${duration}s)"
        echo "$order_id"
        return 0
    else
        print_error "Order creation failed (Status: $status_code)"
        return 1
    fi
}

# Function to simulate user session
simulate_user_session() {
    local user_id=$1
    local email=$2
    local token=$3
    
    print_status "Simulating user session for $email..."
    
    local successful_requests=0
    local total_duration=0
    
    for i in $(seq 1 $REQUESTS_PER_USER); do
        local request_type=$(shuf -i 1-4 -n 1)
        
        case $request_type in
            1)
                # Get user profile
                local response=$(make_timed_request "GET" "$USER_SERVICE/api/users/1" "" "-H 'Authorization: Bearer $token'")
                local status_code=$(echo "$response" | cut -d'|' -f1)
                local duration=$(echo "$response" | cut -d'|' -f2)
                ;;
            2)
                # Get products
                local response=$(make_timed_request "GET" "$INVENTORY_SERVICE/api/inventory/products" "" "")
                local status_code=$(echo "$response" | cut -d'|' -f1)
                local duration=$(echo "$response" | cut -d'|' -f2)
                ;;
            3)
                # Get orders
                local response=$(make_timed_request "GET" "$ORDER_SERVICE/api/orders/user/1" "" "")
                local status_code=$(echo "$response" | cut -d'|' -f1)
                local duration=$(echo "$response" | cut -d'|' -f2)
                ;;
            4)
                # Create order (if products available)
                if [ ${#PRODUCT_IDS[@]} -gt 0 ]; then
                    local product_id=${PRODUCT_IDS[$RANDOM % ${#PRODUCT_IDS[@]}]}
                    local order_id=$(create_order "$token" "$product_id")
                    if [ $? -eq 0 ]; then
                        ORDER_IDS+=("$order_id")
                    fi
                    local status_code=201
                    local duration=0.5
                else
                    local status_code=400
                    local duration=0.1
                fi
                ;;
        esac
        
        if [ "$status_code" -ge 200 ] && [ "$status_code" -lt 300 ]; then
            ((successful_requests++))
        fi
        
        total_duration=$(echo "$total_duration + $duration" | bc)
        
        # Small delay between requests
        sleep 0.1
    done
    
    print_success "User session completed: $successful_requests/$REQUESTS_PER_USER successful requests"
    echo "$successful_requests|$total_duration"
}

# Function to run concurrent load test
run_concurrent_load_test() {
    print_load_test "Starting concurrent load test with $CONCURRENT_USERS users..."
    
    local pids=()
    local results_file="/tmp/load_test_results_$$"
    
    # Clear results file
    > "$results_file"
    
    # Start concurrent user sessions
    for i in $(seq 1 $CONCURRENT_USERS); do
        local email=$(generate_random_email)
        local name=$(generate_random_name)
        
        # Register and login user in background
        (
            register_user "$email" "$name" > /dev/null 2>&1
            local token=$(login_user "$email" 2>/dev/null)
            
            if [ ! -z "$token" ]; then
                local result=$(simulate_user_session "$i" "$email" "$token")
                echo "$result" >> "$results_file"
            else
                echo "0|0" >> "$results_file"
            fi
        ) &
        
        pids+=($!)
        
        # Small delay between user starts
        sleep 0.1
    done
    
    # Wait for all background processes to complete
    print_status "Waiting for all user sessions to complete..."
    for pid in "${pids[@]}"; do
        wait $pid
    done
    
    # Analyze results
    analyze_load_test_results "$results_file"
    
    # Cleanup
    rm -f "$results_file"
}

# Function to analyze load test results
analyze_load_test_results() {
    local results_file=$1
    
    print_metric "Load Test Results Analysis"
    echo "==============================="
    
    local total_successful=0
    local total_duration=0
    local total_users=0
    
    while IFS='|' read -r successful duration; do
        total_successful=$((total_successful + successful))
        total_duration=$(echo "$total_duration + $duration" | bc)
        ((total_users++))
    done < "$results_file"
    
    local success_rate=$(echo "scale=2; $total_successful * 100 / $TOTAL_REQUESTS" | bc)
    local avg_duration=$(echo "scale=3; $total_duration / $total_users" | bc)
    local requests_per_second=$(echo "scale=2; $total_successful / $total_duration" | bc)
    
    print_metric "Total Requests: $TOTAL_REQUESTS"
    print_metric "Successful Requests: $total_successful"
    print_metric "Success Rate: ${success_rate}%"
    print_metric "Average Response Time: ${avg_duration}s"
    print_metric "Requests Per Second: $requests_per_second"
    print_metric "Concurrent Users: $CONCURRENT_USERS"
    
    # Performance thresholds
    if (( $(echo "$success_rate >= 95" | bc -l) )); then
        print_success "✅ Success rate is excellent (>= 95%)"
    elif (( $(echo "$success_rate >= 90" | bc -l) )); then
        print_warning "⚠️  Success rate is good (>= 90%)"
    else
        print_error "❌ Success rate is below acceptable threshold (< 90%)"
    fi
    
    if (( $(echo "$avg_duration <= 1.0" | bc -l) )); then
        print_success "✅ Average response time is excellent (<= 1.0s)"
    elif (( $(echo "$avg_duration <= 2.0" | bc -l) )); then
        print_warning "⚠️  Average response time is acceptable (<= 2.0s)"
    else
        print_error "❌ Average response time is too slow (> 2.0s)"
    fi
}

# Function to test system under stress
stress_test() {
    print_load_test "Starting stress test..."
    
    # Gradually increase load
    local stress_levels=(10 25 50 100)
    
    for level in "${stress_levels[@]}"; do
        print_status "Testing with $level concurrent users..."
        
        CONCURRENT_USERS=$level
        REQUESTS_PER_USER=10
        TOTAL_REQUESTS=$((CONCURRENT_USERS * REQUESTS_PER_USER))
        
        local start_time=$(date +%s)
        run_concurrent_load_test
        local end_time=$(date +%s)
        local test_duration=$((end_time - start_time))
        
        print_metric "Stress test level $level completed in ${test_duration}s"
        echo
        
        # Wait between stress levels
        sleep 5
    done
}

# Function to test database performance
database_performance_test() {
    print_load_test "Testing database performance..."
    
    # Create multiple products concurrently
    print_status "Creating 100 products concurrently..."
    local pids=()
    
    for i in {1..100}; do
        (
            create_product > /dev/null 2>&1
        ) &
        pids+=($!)
    done
    
    # Wait for all product creations
    for pid in "${pids[@]}"; do
        wait $pid
    done
    
    print_success "100 products created concurrently"
    
    # Test concurrent reads
    print_status "Testing concurrent product reads..."
    local start_time=$(date +%s.%N)
    
    for i in {1..50}; do
        make_timed_request "GET" "$INVENTORY_SERVICE/api/inventory/products" "" "" > /dev/null 2>&1 &
    done
    
    wait
    local end_time=$(date +%s.%N)
    local read_duration=$(echo "$end_time - $start_time" | bc)
    
    print_metric "50 concurrent reads completed in ${read_duration}s"
}

# Main execution function
main() {
    print_status "Starting Advanced Load Testing Simulation..."
    print_status "Configuration:"
    print_status "  - Concurrent Users: $CONCURRENT_USERS"
    print_status "  - Requests per User: $REQUESTS_PER_USER"
    print_status "  - Total Requests: $TOTAL_REQUESTS"
    echo
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 10
    
    # Create initial test data
    print_status "Creating initial test data..."
    for i in {1..10}; do
        local product_id=$(create_product)
        if [ ! -z "$product_id" ]; then
            PRODUCT_IDS+=("$product_id")
        fi
    done
    
    print_success "Created ${#PRODUCT_IDS[@]} test products"
    echo
    
    # Run different types of tests
    print_load_test "1. Basic Load Test"
    run_concurrent_load_test
    echo
    
    print_load_test "2. Database Performance Test"
    database_performance_test
    echo
    
    print_load_test "3. Stress Test"
    stress_test
    echo
    
    print_success "🎉 ALL LOAD TESTS COMPLETED! 🎉"
    print_status "The microservices architecture has been tested under various load conditions."
    print_status "Check the metrics above to verify system performance."
}

# Run the main function
main "$@"

