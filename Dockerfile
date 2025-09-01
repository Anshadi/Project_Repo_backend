# Use official OpenJDK 17 base image
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy the built JAR file into the container
COPY target/voice-shopping-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8082

# Set environment variables
ENV SPRING_DATA_MONGODB_URI=${MONGO_URI}
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/credentials.json

# Copy any required files (like service account credentials)
# COPY path/to/credentials.json /app/credentials.json

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
