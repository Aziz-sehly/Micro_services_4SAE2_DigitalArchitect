package com.esprit.microservice_proposal.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Transmet le JWT vers les appels Feign.
 * <p>
 * Ne doit <strong>pas</strong> être référencé dans {@code @FeignClient(configuration=...)} :
 * un contexte Feign « partiel » n’embarque pas les {@code HttpMessageConverters} Jackson,
 * ce qui provoque {@code DecodeException: no suitable HttpMessageConverter} sur les DTO.
 */
@Configuration
public class FeignAuthorizationConfig {

    @Bean
    public RequestInterceptor forwardAuthorizationInterceptor() {
        return (template) -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return;
            }
            HttpServletRequest request = attrs.getRequest();
            String auth = request.getHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                template.header("Authorization", auth);
            }
        };
    }
}
