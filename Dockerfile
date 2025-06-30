# Stage 1 - Build the app
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven project files first to leverage Docker cache
COPY pom.xml .
COPY src ./src

# Build the Spring Boot application
RUN mvn clean package -DskipTests

# Stage 2 - Create minimal runtime image
FROM eclipse-temurin:21-jre

# Create a non-root user
# RUN useradd -ms /bin/bash appuser

WORKDIR /app

# Copy the fat jar from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Make sure data directory exists
# RUN mkdir -p /data/files/output && chown -R appuser /data/files
RUN mkdir -p /data/files

# Use the non-root user
# USER appuser

# Expose port if needed (default Spring Boot)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]