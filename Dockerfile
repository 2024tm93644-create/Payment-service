# Use official Eclipse Temurin JDK 17 as base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven build output (JAR file)
COPY target/payment-service-1.0.0.jar app.jar

# Expose the application port
EXPOSE 8080

# Environment variables (optional defaults)
ENV SPRING_PROFILES_ACTIVE=default
ENV SERVER_PORT=8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
