FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app


COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon


COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd -r -u 1001 appuser

COPY --from=builder /app/build/libs/app.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
