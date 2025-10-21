FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml files
COPY pom.xml .
COPY notification-service/pom.xml notification-service/

# Download dependencies
RUN mvn dependency:go-offline -f notification-service/pom.xml -B

# Copy source code
COPY notification-service/src notification-service/src

# Build the application
RUN cd notification-service && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN groupadd --system spring && useradd --system --gid spring spring

# Set working directory
WORKDIR /app

# Copy the JAR file from builder stage
COPY --from=builder /app/notification-service/target/notification-service-*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to spring user
USER spring:spring

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
