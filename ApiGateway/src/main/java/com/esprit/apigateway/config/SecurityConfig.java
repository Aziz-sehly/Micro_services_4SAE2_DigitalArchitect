package com.esprit.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

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
                        .pathMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()

                        // Paiement (routage gateway: /payment/api/**) — client paie, freelancer consulte
                        .pathMatchers(HttpMethod.POST, "/payment/api/payments").hasRole("client")
                        .pathMatchers(HttpMethod.GET, "/payment/api/**").hasAnyRole("client", "freelancer")
                        .pathMatchers(HttpMethod.DELETE, "/payment/api/**").hasRole("client")

                        // Freelancer-only endpoints
                        .pathMatchers("/api/freelancer/**", "/api/proposals/**", "/api/candidatures/**").hasRole("freelancer")

                        // Client-only endpoints
                        .pathMatchers("/api/client/**", "/api/projects/**", "/api/milestones/**", "/api/payments/**").hasRole("client")

                        // User microservice (.NET)
                        .pathMatchers(HttpMethod.POST, "/user").permitAll()
                        .pathMatchers(HttpMethod.GET, "/user/me").authenticated()
                        .pathMatchers(HttpMethod.GET, "/user/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/user/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/user/**").hasRole("ADMIN")

                        // Reviews — GET public, write requires auth
                        .pathMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
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
            // Keycloak : realm_access est un objet JSON { "roles": [ "client", ... ] }
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || realmAccess.get("roles") == null) {
                return Flux.empty();
            }
            Object raw = realmAccess.get("roles");
            if (!(raw instanceof List<?> list)) {
                return Flux.empty();
            }
            return Flux.fromIterable(list)
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
        });
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
