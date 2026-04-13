package com.payment.payment.controller;


import com.payment.payment.dto.PaymentRequest;
import com.payment.payment.dto.PaymentResponse;
import com.payment.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    /**
     * Le client paie le freelancer : seul le rôle {@code client} peut créer un paiement
     * (désactivé côté contrôle si {@code payment.security.oauth2-enabled=false}).
     */
    @PostMapping("/payments")
    @PreAuthorize("@paymentSec.oauthOffOrHasRole('client')")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(Authentication authentication, @RequestBody @Valid PaymentRequest req) {
        return service.create(authentication, req);
    }

    /**
     * Client : paiements dont il est payeur. Freelancer : dont il est bénéficiaire.
     */
    @GetMapping("/payments/{id}")
    @PreAuthorize("@paymentSec.oauthOffOrHasAnyRole('client','freelancer')")
    public PaymentResponse getById(Authentication authentication, @PathVariable Long id) {
        return service.getById(authentication, id);
    }

    @GetMapping("/payments")
    @PreAuthorize("@paymentSec.oauthOffOrHasAnyRole('client','freelancer')")
    public List<PaymentResponse> list(
            Authentication authentication,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) Long milestoneId
    ) {
        return service.list(authentication, contractId, milestoneId);
    }

    /**
     * Seul le client payeur peut supprimer (ex. annulation métier).
     */
    @DeleteMapping("/payments/{id}")
    @PreAuthorize("@paymentSec.oauthOffOrHasRole('client')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        service.delete(authentication, id);
    }
}
