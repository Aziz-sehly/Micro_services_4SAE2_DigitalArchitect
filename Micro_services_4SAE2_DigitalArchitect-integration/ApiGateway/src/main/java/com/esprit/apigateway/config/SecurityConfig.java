package com.esprit.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers("/public/**", "/actuator/**").permitAll()

                        // Freelancer-only endpoints
                        .pathMatchers("/api/freelancer/**", "/api/proposals/**", "/api/candidatures/**").hasRole("freelancer")

                        // Client-only endpoints
                        .pathMatchers("/api/client/**", "/api/projects/**", "/api/milestones/**", "/api/payments/**").hasRole("client")

                        // Shared endpoints (both roles)
                        .pathMatchers("/api/reviews/**").hasAnyRole("freelancer", "client")

                        // Everything else requires authentication
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract roles from Keycloak token
            List<String> roles = jwt.getClaimAsStringList("realm_access.roles");
            if (roles == null) {
                roles = Collections.emptyList();
            }
            return Flux.fromIterable(roles)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collectList()
                    .flux()
                    .flatMap(Flux::fromIterable);
        });
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
