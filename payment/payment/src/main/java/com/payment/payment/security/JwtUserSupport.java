package com.payment.payment.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JwtUserSupport {

    private static final List<String> USER_ID_CLAIMS = List.of(
            "app_user_id", "user_id", "database_id", "userId", "preferred_user_id"
    );

    /**
     * Identifiant métier (Long) aligné avec payerId/payeeId en base.
     * À mapper dans Keycloak (claim personnalisé) pour la production.
     */
    public Optional<Long> resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        for (String claim : USER_ID_CLAIMS) {
            Object v = jwt.getClaim(claim);
            if (v instanceof Number n) {
                return Optional.of(n.longValue());
            }
            if (v instanceof String s) {
                try {
                    return Optional.of(Long.parseLong(s.trim()));
                } catch (NumberFormatException ignored) {
                    // continue
                }
            }
        }
        return Optional.empty();
    }
}
