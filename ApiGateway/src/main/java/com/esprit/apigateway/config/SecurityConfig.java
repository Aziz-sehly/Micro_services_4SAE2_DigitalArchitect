package com.esprit.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${gateway.security.require-jwt:false}")
    private boolean requireJwt;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);

        http.authorizeExchange(exchanges -> {
            exchanges.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            exchanges.pathMatchers("/actuator/**").permitAll();
            if (requireJwt) {
                exchanges.pathMatchers("/api/reviews/**", "/project/**", "/proposal/**",
                        "/payment/**", "/milestone/**", "/candidature/**", "/application/**")
                        .authenticated();
            }
            exchanges.anyExchange().permitAll();
        });

        if (requireJwt) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }

        return http.build();
    }
}
