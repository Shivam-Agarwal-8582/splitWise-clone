# Multi-stage Dockerfile for Spring Boot 3 + Java 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper & POM
COPY pom.xml ./
COPY src ./src

# Build JAR package skipping tests (tests verified during CI)
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

RUN addgroup -S splitwise && adduser -S splitwise -G splitwise && \
    mkdir -p /app/uploads && chown -R splitwise:splitwise /app

USER splitwise

COPY --from=builder /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

