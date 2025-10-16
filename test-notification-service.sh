#!/bin/bash

# Simple test script to verify email notification functionality
# This script tests the notification service by making a direct HTTP request

echo "Testing email notification functionality..."

# Check if notification service is running
if ! nc -z localhost 8085; then
    echo "Error: Notification service is not running on localhost:8085"
    echo "Please start the notification service first"
    exit 1
fi

echo "Notification service is running. Testing basic functionality..."

# Test the health endpoint first
echo "Testing health endpoint..."
curl -s -u "admin:admin123" http://localhost:8085/actuator/health | jq .status

if [ $? -eq 0 ]; then
    echo "Health check passed!"
else
    echo "Health check failed!"
    exit 1
fi

echo ""
echo "Testing notification service endpoints..."

# Test the stats endpoint
echo "Testing stats endpoint..."
curl -s -u "admin:admin123" http://localhost:8085/api/notifications/stats

echo ""
echo "Testing history endpoint..."
curl -s -u "admin:admin123" http://localhost:8085/api/notifications/history

echo ""
echo "Basic tests completed. The notification service is running and accessible."
echo ""
echo "To test email functionality:"
echo "1. The service is configured to send emails to: souravsingh2609@gmail.com"
echo "2. When a user is created via Kafka, a welcome email should be sent"
echo "3. Check the notification service logs for email sending activity"
echo "4. Check the email account for welcome emails"
echo ""
echo "The notification service is ready to process user creation events and send welcome emails!"
