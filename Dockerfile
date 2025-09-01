# Use official OpenJDK 17 base image
FROM eclipse-temurin:17-jdk-jammy as builder

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the application
RUN apt-get update && apt-get install -y maven \
    && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/target/voice-shopping-backend-*.jar app.jar

# Expose the port the app runs on
EXPOSE 8082

# Run the application with environment variables
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.data.mongodb.uri=${MONGO_URI} --gemini.api.key=${GEMINI_API_KEY}"]
