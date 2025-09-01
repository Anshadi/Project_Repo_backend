# Use official OpenJDK 17 base image
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy the built JAR file into the container
COPY target/voice-shopping-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8082

# Run the application with environment variables
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.data.mongodb.uri=${MONGO_URI} --gemini.api.key=${GEMINI_API_KEY}"]
