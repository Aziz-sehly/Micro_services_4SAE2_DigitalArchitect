package com.esprit.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("forum-service", r -> r
                        .path("/api/posts/**")
                        .uri("http://forum-service:8082"))
                .route("reviews-service", r -> r
                        .path("/api/reviews/**")
                        .uri("http://forum-service:8082"))
                .route("microservice_project", r -> r
                        .path("/project/**")
                        .filters(f -> f.rewritePath(
                                "/project/(?<segment>.*)",
                                "/freelance/project/${segment}"
                        ))
                        .uri("http://project-service:8091"))
                .route("microservice-proposal", r -> r
                        .path("/proposal/**")
                        .filters(f -> f.rewritePath(
                                "/proposal/(?<segment>.*)",
                                "/freelance1/proposal/${segment}"
                        ))
                        .uri("http://proposal-service:8092"))
                .route("microservice_payment", r -> r
                        .path("/payment/**")
                        .filters(f -> f.rewritePath(
                                "/payment/(?<segment>.*)",
                                "/payment/${segment}"
                        ))
                        .uri("http://payment-service:8096"))
                .route("microservice_milestone", r -> r
                        .path("/milestone/**")
                        .filters(f -> f.rewritePath(
                                "/milestone/(?<segment>.*)",
                                "/milestone/${segment}"
                        ))
                        .uri("http://proposal-service:8092"))
                .route("microservice_candidature", r -> r
                        .path("/candidature/**")
                        .filters(f -> f.rewritePath(
                                "/candidature/(?<segment>.*)",
                                "/h2/candidature/${segment}"
                        ))
                        .uri("http://candidature-service:8093"))
                .route("microservice_application", r -> r
                        .path("/application/**")
                        .filters(f -> f.rewritePath(
                                "/application/(?<segment>.*)",
                                "/h2/application/${segment}"
                        ))
                        .uri("http://candidature-service:8093"))
                .route("microservice_user", r -> r
                        .path("/user", "/user/**")
                        .filters(f -> f.rewritePath(
                                "/user(?:/(?<segment>.*))?",
                                "/api/Users/${segment}"
                        ))
                        .uri("http://user-service:5088"))

                .build();
    }
}