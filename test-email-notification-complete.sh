#!/bin/bash

# Complete test script for email notification functionality
# This script tests the full email notification flow

echo "=== Testing Email Notification Service ==="
echo ""

# Check if notification service is healthy
echo "1. Checking notification service health..."
HEALTH_RESPONSE=$(curl -s http://localhost:8085/actuator/health)
if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
    echo "✅ Notification service is healthy"
else
    echo "❌ Notification service is not healthy"
    echo "Response: $HEALTH_RESPONSE"
    exit 1
fi

echo ""

# Test the stats endpoint
echo "2. Checking notification statistics..."
STATS_RESPONSE=$(curl -s -u admin:admin123 http://localhost:8085/api/notifications/stats)
echo "Current stats: $STATS_RESPONSE"

echo ""

# Test the history endpoint
echo "3. Checking notification history..."
HISTORY_RESPONSE=$(curl -s -u admin:admin123 http://localhost:8085/api/notifications/history)
echo "Current history: $HISTORY_RESPONSE"

echo ""

# Test the test-email endpoint
echo "4. Testing email endpoint..."
EMAIL_RESPONSE=$(curl -s -u admin:admin123 -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sourav@calpgrow.com",
    "username": "sourav",
    "firstName": "Sourav",
    "lastName": "Singh"
  }' \
  http://localhost:8085/api/notifications/test-email)

echo "Email test response: $EMAIL_RESPONSE"

echo ""

# Check if we can access the Swagger UI
echo "5. Checking API documentation..."
SWAGGER_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/swagger-ui.html)
if [ "$SWAGGER_RESPONSE" = "200" ]; then
    echo "✅ Swagger UI is accessible at http://localhost:8085/swagger-ui.html"
else
    echo "⚠️  Swagger UI returned HTTP $SWAGGER_RESPONSE"
fi

echo ""

# Check Prometheus metrics
echo "6. Checking Prometheus metrics..."
METRICS_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/actuator/prometheus)
if [ "$METRICS_RESPONSE" = "200" ]; then
    echo "✅ Prometheus metrics are accessible at http://localhost:8085/actuator/prometheus"
else
    echo "⚠️  Prometheus metrics returned HTTP $METRICS_RESPONSE"
fi

echo ""
echo "=== Test Complete ==="
echo ""
echo "📧 Email Configuration:"
echo "   - SMTP Host: smtp.gmail.com:587"
echo "   - Username: souravsingh2609@gmail.com"
echo "   - Test Email: sourav@calpgrow.com"
echo ""
echo "🔗 Available Endpoints:"
echo "   - Health: http://localhost:8085/actuator/health"
echo "   - Stats: http://localhost:8085/api/notifications/stats (admin:admin123)"
echo "   - History: http://localhost:8085/api/notifications/history (admin:admin123)"
echo "   - Test Email: http://localhost:8085/api/notifications/test-email (admin:admin123)"
echo "   - Swagger UI: http://localhost:8085/swagger-ui.html"
echo "   - Prometheus: http://localhost:8085/actuator/prometheus"
