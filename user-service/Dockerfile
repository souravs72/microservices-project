# Multi-stage Dockerfile for User Service
FROM eclipse-temurin:17-jdk AS builder

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy parent pom.xml first
COPY pom.xml .

# Copy common module pom.xml and source
COPY common/pom.xml ./common/
COPY common/src ./common/src

# Copy user-service pom.xml
COPY user-service/pom.xml ./user-service/

# Build common module standalone
RUN cd common && mvn clean install -DskipTests

# Copy source code
COPY user-service/src ./user-service/src

# Build the application with local common module
RUN cd user-service && mvn clean package -DskipTests -Dmaven.repo.local=/root/.m2/repository

# Runtime stage
FROM eclipse-temurin:17-jre

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create user
RUN groupadd -r spring && useradd -r -g spring spring

# Set working directory
WORKDIR /app

# Copy jar file
COPY --from=builder /app/user-service/target/*.jar app.jar

# Create logs directory and set permissions
RUN mkdir -p /app/logs && chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]