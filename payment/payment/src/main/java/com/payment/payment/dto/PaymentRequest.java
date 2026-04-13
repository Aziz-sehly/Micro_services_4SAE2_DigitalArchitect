package com.payment.payment.dto;

import com.payment.payment.model.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Création d’un paiement : le payeur est le client, le bénéficiaire le freelancer.
 * {@code payerId} est optionnel si le JWT contient un claim d’id utilisateur (voir JwtUserSupport) ;
 * sinon il doit être fourni (ex. tests / profil sans OAuth2).
 */
public record PaymentRequest(
        @NotNull Long contractId,
        Long milestoneId,
        Long payerId,
        @NotNull Long payeeId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @NotNull PaymentMethod method
) {}