# Multi-stage Dockerfile for Notification Service
FROM openjdk:17-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy parent pom.xml first
COPY pom.xml .

# Copy notification-service pom.xml
COPY notification-service/pom.xml ./notification-service/

# Download dependencies for notification-service
RUN cd notification-service && mvn dependency:go-offline -B

# Copy source code
COPY notification-service/src ./notification-service/src

# Build the application
RUN cd notification-service && mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jdk-slim

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/notification-service/target/notification-service-*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]