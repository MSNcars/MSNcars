FROM eclipse-temurin:21
COPY ./target/MSNcars-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8080