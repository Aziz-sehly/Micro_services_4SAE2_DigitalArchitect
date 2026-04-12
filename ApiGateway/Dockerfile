FROM eclipse-temurin:17-jdk
ADD target/ApiGateway-0.0.1-SNAPSHOT.jar ApiGateway.jar
EXPOSE 8765
ENTRYPOINT ["java", "-jar", "ApiGateway.jar"]