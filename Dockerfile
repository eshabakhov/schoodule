FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG JAR_FILE=target/schoodule-0.0.1.jar
COPY ${JAR_FILE} app.jar
EXPOSE 9500
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
