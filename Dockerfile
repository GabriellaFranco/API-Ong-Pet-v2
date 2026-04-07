# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S ongpet && adduser -S ongpet -G ongpet

# Copia apenas o jar gerado no stage anterior
COPY --from=build /app/target/*.jar app.jar

# Define o usuário não-root
USER ongpet

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]