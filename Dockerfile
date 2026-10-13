# Multi-stage Production-Ready Dockerfile for Book Worm eCommerce Backend
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Security: Add non-root system user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy pre-packaged Spring Boot executable JAR
ARG JAR_FILE=target/bookworm-backend-1.0.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Production Health Check against OpenAPI specification endpoint
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3   CMD wget --no-verbose --tries=1 --spider http://localhost:8080/v3/api-docs || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Djdk.net.unixdomain.tmpdir=/tmp", "-jar", "app.jar"]
