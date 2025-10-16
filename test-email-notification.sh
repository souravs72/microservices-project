#!/bin/bash

# Test script to verify email notification functionality
# This script simulates a user creation event by sending a message to Kafka

echo "Testing email notification functionality..."

# Check if Kafka is running
if ! nc -z localhost 9092; then
    echo "Error: Kafka is not running on localhost:9092"
    echo "Please start Kafka first using: docker-compose up -d kafka zookeeper"
    exit 1
fi

# Check if notification service is running
if ! nc -z localhost 8085; then
    echo "Error: Notification service is not running on localhost:8085"
    echo "Please start the notification service first"
    exit 1
fi

# Create a test user event message
USER_EVENT='{
  "eventType": "USER_CREATED",
  "userId": "test-user-123",
  "username": "testuser",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

echo "Sending test user creation event to Kafka..."
echo "Event: $USER_EVENT"

# Send the message to Kafka using kafka-console-producer
echo "$USER_EVENT" | docker exec -i $(docker ps -q --filter "name=kafka") kafka-console-producer.sh \
    --bootstrap-server localhost:9092 \
    --topic user-events-dev \
    --property "key.separator=:" \
    --property "parse.key=true" \
    --property "key.serializer=org.apache.kafka.common.serialization.StringSerializer" \
    --property "value.serializer=org.apache.kafka.common.serialization.StringSerializer"

echo ""
echo "Test event sent! Check the notification service logs to see if the email was processed."
echo "You can also check the notification service health endpoint: http://localhost:8085/actuator/health"
echo ""
echo "If you have access to the email account (souravsingh2609@gmail.com), check for a welcome email."
