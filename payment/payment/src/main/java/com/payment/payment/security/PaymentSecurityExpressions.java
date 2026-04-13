package com.payment.payment.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("paymentSec")
public class PaymentSecurityExpressions {

    @Value("${payment.security.oauth2-enabled:true}")
    private boolean oauth2Enabled;

    public boolean oauthOff() {
        return !oauth2Enabled;
    }

    public boolean oauthOffOrHasRole(String role) {
        if (!oauth2Enabled) {
            return true;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String expected = "ROLE_" + role;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(expected::equals);
    }

    public boolean oauthOffOrHasAnyRole(String... roles) {
        if (!oauth2Enabled) {
            return true;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        var authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Arrays.stream(roles)
                .anyMatch(r -> authorities.contains("ROLE_" + r));
    }
}
