# ===============================
# Stage 1: Build (using Maven)
# ===============================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the entire multi-module project (parent + all submodules)
COPY . .

# Install parent POM only (to make modules resolvable)
RUN mvn -f pom.xml install -N -DskipTests

# Build only the api-gateway module (and dependencies)
RUN mvn -f pom.xml clean package -pl api-gateway -am -DskipTests


# ===============================
# Stage 2: Runtime
# ===============================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for better security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy built JAR from builder image
COPY --from=builder /app/api-gateway/target/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar
USER spring:spring

# Health check endpoint (adjust if needed)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose the gateway port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
