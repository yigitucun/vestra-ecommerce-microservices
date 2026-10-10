# Universal multi-stage Dockerfile for Vestra Spring Boot microservices
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# 1. Copy gradle wrapper and configuration
COPY gradlew gradlew.bat settings.gradle.kts ./
COPY gradle ./gradle

# 2. Copy shared libraries
COPY common ./common
COPY common-web ./common-web

# 3. Copy target service source
ARG SERVICE_NAME
COPY ${SERVICE_NAME} ./${SERVICE_NAME}

RUN chmod +x gradlew
RUN ./gradlew :${SERVICE_NAME}:bootJar -x test --no-daemon

# 4. Minimal production runtime
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd -r -u 1001 appuser

ARG SERVICE_NAME
COPY --from=builder /app/${SERVICE_NAME}/build/libs/*.jar app.jar

USER appuser

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
