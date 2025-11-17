FROM openjdk:24-ea-14-jdk-slim
WORKDIR /app
COPY target/4LabOOP-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]