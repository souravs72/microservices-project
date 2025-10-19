# Multi-stage build for Inventory Service
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy parent pom.xml first for better caching
COPY pom.xml .
COPY inventory-service/pom.xml inventory-service/

# Download dependencies
RUN mvn dependency:go-offline -f inventory-service/pom.xml -B

# Copy source code
COPY inventory-service/src inventory-service/src

# Build the application
RUN mvn clean package -f inventory-service/pom.xml -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/inventory-service/target/inventory-service-*.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
