# =====  compila o projeto =====
FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# =====  roda o projeto =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S autobots && adduser -S autobots -G autobots
COPY --from=build --chown=autobots:autobots /build/target/automanager-*.jar app.jar
USER autobots
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
