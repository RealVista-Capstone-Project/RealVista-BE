# Dockerfile for RealVista Backend

# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and build config files needed by Maven plugins
COPY pom.xml checkstyle.xml spotbugs-exclude.xml ./
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.gitcommitid.skip=true

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user and logs directory
RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/logs && chown spring:spring /app/logs

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Default Spring profile (overridden at runtime via -e SPRING_PROFILES_ACTIVE=dev|prod)
ENV SPRING_PROFILES_ACTIVE=prod

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application (Spring Boot reads SPRING_PROFILES_ACTIVE from the environment automatically)
ENTRYPOINT ["java", "-jar", "app.jar"]

