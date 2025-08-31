# =======================
# 1. Build stage
# =======================
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy pom.xml and download dependencies first (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# =======================
# 2. Runtime stage
# =======================
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy built jar from builder
COPY --from=builder /build/target/issuetracker-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
