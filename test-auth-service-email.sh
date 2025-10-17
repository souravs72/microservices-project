#!/bin/bash

# Test script for Auth Service Email Functionality
# This script tests password reset email functionality in the auth service

echo "=== Testing Auth Service Email Functionality ==="
echo ""

# Check if auth service is running
if ! nc -z localhost 8082; then
    echo "❌ Error: Auth service is not running on localhost:8082"
    echo "Please start the auth service first using: docker-compose up -d auth-service"
    exit 1
fi

echo "✅ Auth service is running on localhost:8082"
echo ""

# Test 1: Check auth service health
echo "1. Checking auth service health..."
HEALTH_RESPONSE=$(curl -s http://localhost:8082/actuator/health)
echo "Health response: $HEALTH_RESPONSE"
echo ""

# Test 2: Test password reset email endpoint
echo "2. Testing password reset email functionality..."
echo "Sending password reset request for test@example.com..."

PASSWORD_RESET_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }' \
  http://localhost:8082/api/auth/forgot-password)

echo "Password reset response: $PASSWORD_RESET_RESPONSE"
echo ""

# Test 3: Test with a real email (if provided)
if [ ! -z "$TEST_EMAIL" ]; then
    echo "3. Testing with provided email: $TEST_EMAIL"
    REAL_EMAIL_RESPONSE=$(curl -s -X POST \
      -H "Content-Type: application/json" \
      -d "{
        \"email\": \"$TEST_EMAIL\"
      }" \
      http://localhost:8082/api/auth/forgot-password)
    
    echo "Real email test response: $REAL_EMAIL_RESPONSE"
    echo ""
fi

# Test 4: Check mail configuration in actuator
echo "4. Checking mail configuration via actuator..."
MAIL_CONFIG=$(curl -s -u admin:admin123 http://localhost:8082/actuator/env | grep -A 10 -B 2 "mail")
if [ ! -z "$MAIL_CONFIG" ]; then
    echo "Mail configuration found:"
    echo "$MAIL_CONFIG"
else
    echo "No mail configuration found in actuator (this is normal if actuator security is enabled)"
fi
echo ""

echo "=== Test Summary ==="
echo "✅ Auth service email functionality test completed"
echo ""
echo "📧 Email Configuration:"
echo "   - SMTP Host: smtp.gmail.com:587"
echo "   - Username: Set via MAIL_USERNAME environment variable"
echo "   - Password: Set via MAIL_PASSWORD environment variable"
echo ""
echo "🔧 To configure email:"
echo "   1. Set MAIL_USERNAME environment variable to your Gmail address"
echo "   2. Set MAIL_PASSWORD environment variable to your Gmail app password"
echo "   3. Restart the auth service"
echo ""
echo "📝 To test with your email:"
echo "   export TEST_EMAIL=your_email@gmail.com"
echo "   ./test-auth-service-email.sh"
echo ""
echo "📋 Check logs for email sending details:"
echo "   docker-compose logs auth-service"
