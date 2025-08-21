FROM maven:3.9.1-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY security-lib/pom.xml ./security-lib/
COPY auth-service/pom.xml ./auth-service/
COPY file-service/pom.xml ./file-service/

RUN mvn install -pl :security-lib -am -DskipTests &&\
    mvn -B dependency:go-offline

COPY . .

RUN mvn -B -f pom.xml clean &&\
    mvn -B -f pom.xml package -DskipTests

FROM eclipse-temurin:17-jdk-jammy AS auth-service
WORKDIR /app
COPY --from=builder /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]

FROM eclipse-temurin:17-jdk-jammy AS file-service
WORKDIR /app
COPY --from=builder /app/file-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]