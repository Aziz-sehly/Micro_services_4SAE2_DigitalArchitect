# Build: depuis ce dossier (payment/payment) : docker build -t votrehub/payment-service:latest .
FROM eclipse-temurin:17-jdk-alpine AS build
RUN apk add --no-cache maven
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/payment-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8096
ENTRYPOINT ["java", "-jar", "app.jar"]
