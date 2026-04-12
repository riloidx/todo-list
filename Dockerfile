FROM gradle:jdk21-alpine AS build
WORKDIR /app
COPY --chown=gradle:gradle . /app
RUN gradle clean bootJar

FROM eclipse-temurin:21-jdk-ubi10-minimal
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]