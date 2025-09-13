# Stage 1: Builder - build the multi-module project
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
# Copy the entire project into the container
COPY . .
# Build all modules, resolving dependencies and skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Runner - create images for each service
# Image for user-data-access
FROM openjdk:21-slim AS user-data-access-runner
WORKDIR /app
# Install curl for the health check
RUN apt-get update && apt-get install -y curl
# Explicitly copy the JAR file into the working directory
COPY --from=builder /app/user-data-access/target/user-data-access-0.0.1-SNAPSHOT.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]

# Image for bot-gateway
FROM openjdk:21-slim AS bot-gateway-runner
WORKDIR /app
# Explicitly copy the JAR file into the working directory
COPY --from=builder /app/bot-gateway/target/bot-gateway-0.0.1-SNAPSHOT.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]

# Image for analysis-service
FROM openjdk:21-slim AS analysis-service-runner
WORKDIR /app
# Explicitly copy the JAR file into the working directory
COPY --from=builder /app/analysis-service/target/analysis-service-0.0.1-SNAPSHOT.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]