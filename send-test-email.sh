#!/bin/bash

# Script to send a test email notification
# This simulates a user creation event that triggers email notification

echo "Sending test email to sourav@calpgrow.com..."

# Test the notification service directly via REST API
curl -s -u admin:admin123 -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sourav@calpgrow.com",
    "username": "sourav",
    "firstName": "Sourav",
    "lastName": "Singh"
  }' \
  http://localhost:8085/api/notifications/test-email

echo ""
echo "Test email request sent successfully!"
echo "Check the notification service logs for email sending details."
