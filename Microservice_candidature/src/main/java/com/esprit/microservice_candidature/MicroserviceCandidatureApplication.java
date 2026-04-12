package com.esprit.microservice_candidature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MicroserviceCandidatureApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroserviceCandidatureApplication.class, args);
    }
}
