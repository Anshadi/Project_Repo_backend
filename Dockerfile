# Use official OpenJDK 17 base image
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven \
    && mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /app/target/voice-shopping-backend-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
