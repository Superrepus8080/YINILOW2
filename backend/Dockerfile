FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/yinilow-clothing-os-api-0.1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]
