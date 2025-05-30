# Use Eclipse Temurin Java 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Add metadata
LABEL maintainer="sarforajsshaikh098@gmail.com"

# Set working directory
WORKDIR /app

# Copy the built jar into the container
COPY target/crypto-web-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]

