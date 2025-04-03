FROM maven:3-eclipse-temurin-24-alpine AS build
COPY src /app/src
COPY pom.xml /app

RUN mvn -f /app/pom.xml clean install

FROM eclipse-temurin:24-jre-alpine-3.21
LABEL org.opencontainers.image.source="https://github.com/n0d4n/musicbot"

COPY --from=build /app/target/JMusicBot-Snapshot-All.jar /app/app.jar

CMD ["java", "-Dconfig=/app/config/config.txt", "-jar", "/app/app.jar"]