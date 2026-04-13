package com.payment.payment.service;

import com.payment.payment.client.MilestoneClient;
import com.payment.payment.dto.PaymentRequest;
import com.payment.payment.dto.PaymentResponse;
import com.payment.payment.model.Payment;
import com.payment.payment.model.PaymentStatus;
import com.payment.payment.repository.PaymentRepository;
import com.payment.payment.security.JwtUserSupport;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

    private final PaymentRepository repo;
    private final MilestoneClient milestoneClient;
    private final JwtUserSupport jwtUserSupport;

    @Value("${payment.security.oauth2-enabled:true}")
    private boolean oauth2Enabled;

    public PaymentResponse create(Authentication authentication, PaymentRequest req) {
        if (oauth2Enabled && isFreelancer(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le client peut initier un paiement vers le freelancer.");
        }

        Long payerId = jwtUserSupport.resolveUserId(authentication)
                .orElse(req.payerId());
        if (payerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "payerId obligatoire si le token ne contient pas d'identifiant utilisateur (claim app_user_id / user_id, etc.).");
        }
        if (Objects.equals(payerId, req.payeeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le client (payeur) et le freelancer (bénéficiaire) doivent être distincts.");
        }

        BigDecimal fee = req.amount().multiply(new BigDecimal("0.05"));
        fee = fee.setScale(2, RoundingMode.HALF_UP);

        Payment p = Payment.builder()
                .contractId(req.contractId())
                .milestoneId(req.milestoneId())
                .payerId(payerId)
                .payeeId(req.payeeId())
                .amount(req.amount())
                .platformFee(fee)
                .method(req.method())
                .status(PaymentStatus.SUCCESS)
                .provider("MANUAL")
                .build();

        Payment saved = repo.save(p);

        if (saved.getMilestoneId() != null && saved.getStatus() == PaymentStatus.SUCCESS) {
            try {
                milestoneClient.markPaid(saved.getMilestoneId());
            } catch (FeignException e) {
                log.warn("Milestone mark-paid indisponible pour milestoneId={} : {}", saved.getMilestoneId(), e.getMessage());
            } catch (RuntimeException e) {
                log.warn("Erreur appel milestone-service pour milestoneId={} : {}", saved.getMilestoneId(), e.getMessage());
            }
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(Authentication authentication, Long id) {
        Payment p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: " + id));
        assertCanRead(authentication, p);
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> list(Authentication authentication, Long contractId, Long milestoneId) {
        if (!oauth2Enabled) {
            return listLegacy(contractId, milestoneId);
        }

        Long userId = jwtUserSupport.resolveUserId(authentication).orElse(null);
        boolean client = isClient(authentication);
        boolean freelancer = isFreelancer(authentication);

        if (client && userId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Identifiant utilisateur absent du JWT : impossible de lister les paiements du client.");
        }
        if (freelancer && userId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Identifiant utilisateur absent du JWT : impossible de lister les paiements du freelancer.");
        }

        Stream<Payment> stream;
        if (client) {
            stream = repo.findByPayerId(userId).stream();
        } else if (freelancer) {
            stream = repo.findByPayeeId(userId).stream();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rôle client ou freelancer requis.");
        }

        if (milestoneId != null) {
            stream = stream.filter(pm -> Objects.equals(pm.getMilestoneId(), milestoneId));
        } else if (contractId != null) {
            stream = stream.filter(pm -> Objects.equals(pm.getContractId(), contractId));
        }

        return stream.map(this::toResponse).toList();
    }

    public void delete(Authentication authentication, Long id) {
        Payment p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: " + id));
        if (oauth2Enabled) {
            if (!isClient(authentication)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le client peut annuler ce paiement.");
            }
            Long userId = jwtUserSupport.resolveUserId(authentication).orElse(null);
            if (userId == null || !Objects.equals(userId, p.getPayerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le client payeur peut supprimer ce paiement.");
            }
        }
        repo.deleteById(id);
    }

    private List<PaymentResponse> listLegacy(Long contractId, Long milestoneId) {
        if (milestoneId != null) {
            return repo.findByMilestoneId(milestoneId).stream().map(this::toResponse).toList();
        }
        if (contractId != null) {
            return repo.findByContractId(contractId).stream().map(this::toResponse).toList();
        }
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    private void assertCanRead(Authentication authentication, Payment p) {
        if (!oauth2Enabled) {
            return;
        }
        Long userId = jwtUserSupport.resolveUserId(authentication).orElse(null);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "JWT sans identifiant utilisateur.");
        }
        if (isClient(authentication) && Objects.equals(userId, p.getPayerId())) {
            return;
        }
        if (isFreelancer(authentication) && Objects.equals(userId, p.getPayeeId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à ce paiement.");
    }

    private static boolean isClient(Authentication authentication) {
        return hasRole(authentication, "client");
    }

    private static boolean isFreelancer(Authentication authentication) {
        return hasRole(authentication, "freelancer");
    }

    private static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String expected = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(expected::equals);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getContractId(),
                p.getMilestoneId(),
                p.getPayerId(),
                p.getPayeeId(),
                p.getAmount(),
                p.getPlatformFee(),
                p.getMethod(),
                p.getStatus(),
                p.getProvider(),
                p.getProviderRef(),
                p.getCreatedAt()
        );
    }
}
