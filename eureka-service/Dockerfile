FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY src src
COPY --chmod=777 mvnw mvnw
COPY pom.xml pom.xml
COPY  .mvn .mvn

RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package

FROM eclipse-temurin:21-jre-jammy AS runner

WORKDIR /app

ARG JAR_FILE_PATH=/app/target/*.jar
COPY --from=builder ${JAR_FILE_PATH} app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]