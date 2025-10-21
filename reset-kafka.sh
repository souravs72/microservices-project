#!/bin/bash

# Kafka Reset Script
# This script fixes Kafka cluster ID mismatch issues by cleaning volumes and restarting Kafka

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print functions
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

print_status "Starting Kafka reset process..."

# Stop Kafka container
print_status "Stopping Kafka container..."
docker stop kafka 2>/dev/null || print_warning "Kafka container was not running"

# Remove Kafka container
print_status "Removing Kafka container..."
docker rm kafka 2>/dev/null || print_warning "Kafka container was not found"

# Remove Kafka volume
print_status "Removing Kafka volume to clear cluster metadata..."
docker volume rm microservices-parent_kafka_data 2>/dev/null || print_warning "Kafka volume was not found"

# Also try alternative volume name format
docker volume rm microservices-parent-kafka_data 2>/dev/null || true

print_success "Kafka cleanup completed"

# Start Kafka with docker-compose
print_status "Starting Kafka with fresh configuration..."

# Try docker compose first, fallback to docker-compose
if command -v docker > /dev/null 2>&1 && docker compose version > /dev/null 2>&1; then
    docker compose up -d kafka zookeeper
elif command -v docker-compose > /dev/null 2>&1; then
    docker-compose up -d kafka zookeeper
else
    print_error "Neither 'docker compose' nor 'docker-compose' is available"
    exit 1
fi

# Wait for Kafka to be ready
print_status "Waiting for Kafka to be ready..."
sleep 10

# Check if Kafka is running
if docker ps | grep -q kafka; then
    print_success "Kafka is running successfully!"
    
    # Test Kafka connectivity
    print_status "Testing Kafka connectivity..."
    sleep 5
    
    if docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
        print_success "Kafka is responding to API calls"
    else
        print_warning "Kafka may still be starting up. Please wait a moment and check manually."
    fi
else
    print_error "Failed to start Kafka. Check the logs with: docker logs kafka"
    exit 1
fi

print_success "Kafka reset completed successfully!"
print_status "You can now start your other services with: docker compose up -d"
