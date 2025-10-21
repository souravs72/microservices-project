#!/bin/bash

# 📚 Open Swagger Documentation Script
# This script helps you access all available Swagger documentation

echo "📚 Microservices Swagger Documentation"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if services are running
echo "🔍 Checking service status..."
echo ""

services=("user-service:8081" "auth-service:8082" "notification-service:8085" "api-gateway:8080")
running_services=()

for service in "${services[@]}"; do
    name=$(echo $service | cut -d: -f1)
    port=$(echo $service | cut -d: -f2)
    
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health | grep -q "200\|503"; then
        echo -e "${GREEN}✅ $name${NC} (Port $port) - Running"
        running_services+=("$service")
    else
        echo -e "${RED}❌ $name${NC} (Port $port) - Not running"
    fi
done

echo ""
echo "🌐 Available Swagger Documentation:"
echo ""

# Notification Service (Working)
echo -e "${GREEN}1. Notification Service - FULLY WORKING${NC}"
echo "   📖 Swagger UI: http://localhost:8085/swagger-ui/index.html"
echo "   📋 OpenAPI: http://localhost:8085/v3/api-docs"
echo "   🔐 Auth: Basic (admin:${GRAFANA_PASSWORD:-admin123})"
echo ""

# User Service (Auth Required)
echo -e "${YELLOW}2. User Service - AUTHENTICATION REQUIRED${NC}"
echo "   📖 Swagger UI: http://localhost:8081/swagger-ui/index.html"
echo "   📋 OpenAPI: http://localhost:8081/v3/api-docs"
echo "   🔐 Auth: JWT Token required"
echo ""

# Auth Service (Limited)
echo -e "${YELLOW}3. Auth Service - LIMITED ACCESS${NC}"
echo "   📖 Swagger UI: Not configured"
echo "   📋 Health: http://localhost:8082/actuator/health"
echo "   🔐 Auth: None required"
echo ""

# API Gateway (Limited)
echo -e "${YELLOW}4. API Gateway - LIMITED ACCESS${NC}"
echo "   📖 Swagger UI: Not configured"
echo "   📋 Health: http://localhost:8080/actuator/health"
echo "   🔐 Auth: None required"
echo ""

echo "🚀 Quick Actions:"
echo ""
echo "1. Open Notification Service Swagger UI (recommended):"
echo "   ${BLUE}xdg-open http://localhost:8085/swagger-ui/index.html${NC}"
echo ""

echo "2. Test Notification Service API:"
echo "   ${BLUE}curl -u admin:${GRAFANA_PASSWORD:-admin123} http://localhost:8085/api/notifications/stats${NC}"
echo ""

echo "3. Get JWT Token from Auth Service (for User Service):"
echo "   ${BLUE}curl -X POST http://localhost:8082/api/auth/login \\${NC}"
echo "   ${BLUE}  -H 'Content-Type: application/json' \\${NC}"
echo "   ${BLUE}  -d '{\"username\":\"admin\",\"password\":\"${ADMIN_PASSWORD:-admin123}\"}'${NC}"
echo ""

echo "4. Check service health:"
echo "   ${BLUE}curl http://localhost:8081/actuator/health${NC}"
echo "   ${BLUE}curl http://localhost:8082/actuator/health${NC}"
echo "   ${BLUE}curl http://localhost:8085/actuator/health${NC}"
echo ""

# Interactive menu
echo "🎯 What would you like to do?"
echo ""
echo "1) Open Notification Service Swagger UI"
echo "2) Test Notification Service API"
echo "3) Get JWT Token for User Service"
echo "4) Check all service health"
echo "5) Exit"
echo ""

read -p "Enter your choice (1-5): " choice

case $choice in
    1)
        echo "Opening Notification Service Swagger UI..."
        xdg-open http://localhost:8085/swagger-ui/index.html 2>/dev/null || open http://localhost:8085/swagger-ui/index.html 2>/dev/null || echo "Please open: http://localhost:8085/swagger-ui/index.html"
        ;;
    2)
        echo "Testing Notification Service API..."
        curl -u admin:${GRAFANA_PASSWORD:-admin123} http://localhost:8085/api/notifications/stats
        ;;
    3)
        echo "Getting JWT Token..."
        curl -X POST http://localhost:8082/api/auth/login \
          -H 'Content-Type: application/json' \
          -d '{"username":"admin","password":"'${ADMIN_PASSWORD:-admin123}'"}'
        ;;
    4)
        echo "Checking all service health..."
        echo "User Service:"
        curl -s http://localhost:8081/actuator/health | jq .status 2>/dev/null || echo "Not accessible"
        echo "Auth Service:"
        curl -s http://localhost:8082/actuator/health | jq .status 2>/dev/null || echo "Not accessible"
        echo "Notification Service:"
        curl -s http://localhost:8085/actuator/health | jq .status 2>/dev/null || echo "Not accessible"
        ;;
    5)
        echo "Goodbye! 👋"
        ;;
    *)
        echo "Invalid choice. Please run the script again."
        ;;
esac

echo ""
echo "📖 For more details, see: SWAGGER_DOCUMENTATION.md"
