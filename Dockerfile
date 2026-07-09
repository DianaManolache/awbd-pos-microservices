# Build stage - compileaza toate cele 7 module intr-o singura trecere Maven.
# Fiecare serviciu final de mai jos copiaza doar jarul lui din acest stage
# comun, ca sa nu se repete descarcarea dependintelor de 7 ori.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY sales-service/pom.xml sales-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

COPY eureka-server/src eureka-server/src
COPY config-server/src config-server/src
COPY api-gateway/src api-gateway/src
COPY catalog-service/src catalog-service/src
COPY sales-service/src sales-service/src
COPY user-service/src user-service/src
COPY notification-service/src notification-service/src

RUN mvn -B -DskipTests package

# ---- Runtime images, unul per serviciu (target separat din acelasi Dockerfile) ----

FROM eclipse-temurin:21-jre-alpine AS eureka-server
WORKDIR /app
COPY --from=build /workspace/eureka-server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS config-server
WORKDIR /app
COPY --from=build /workspace/config-server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS api-gateway
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS catalog-service
WORKDIR /app
COPY --from=build /workspace/catalog-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS sales-service
WORKDIR /app
COPY --from=build /workspace/sales-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS user-service
WORKDIR /app
COPY --from=build /workspace/user-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS notification-service
WORKDIR /app
COPY --from=build /workspace/notification-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
