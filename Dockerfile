# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -ntp --batch-mode -Dmaven.test.skip=true -Dskip.installnodenpm -Dskip.npm package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
EXPOSE 9500
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM runtime AS prebuilt
ARG JAR_FILE=target/surok.jar
COPY ${JAR_FILE} app.jar

FROM runtime AS application
COPY --from=build /build/target/surok.jar app.jar
