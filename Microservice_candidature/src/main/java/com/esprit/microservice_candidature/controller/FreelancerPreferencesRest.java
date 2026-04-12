package com.esprit.microservice_candidature.controller;

import com.esprit.microservice_candidature.dto.FreelancerPreferencesMapper;
import com.esprit.microservice_candidature.dto.FreelancerPreferencesRequest;
import com.esprit.microservice_candidature.dto.FreelancerPreferencesResponse;
import com.esprit.microservice_candidature.entity.FreelancerPreferences;
import com.esprit.microservice_candidature.service.IFreelancerPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/candidature", "/application"})
public class FreelancerPreferencesRest {

    @Autowired
    private IFreelancerPreferencesService preferencesService;

    /**
     * Freelancer : ses propres préférences (JWT {@code sub}).
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<FreelancerPreferencesResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        return preferencesService.getByFreelancerId(jwt.getSubject())
                .map(p -> ResponseEntity.ok(FreelancerPreferencesMapper.toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/me")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<FreelancerPreferencesResponse> createMe(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody FreelancerPreferencesRequest body) {
        FreelancerPreferences entity = FreelancerPreferencesMapper.fromRequest(body);
        FreelancerPreferences created = preferencesService.create(jwt.getSubject(), entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(FreelancerPreferencesMapper.toResponse(created));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<FreelancerPreferencesResponse> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody FreelancerPreferencesRequest body) {
        FreelancerPreferences entity = FreelancerPreferencesMapper.fromRequest(body);
        FreelancerPreferences updated = preferencesService.update(jwt.getSubject(), entity);
        return ResponseEntity.ok(FreelancerPreferencesMapper.toResponse(updated));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt) {
        preferencesService.delete(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin : liste de toutes les préférences.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('admin')")
    public List<FreelancerPreferencesResponse> getAll() {
        return preferencesService.getAll().stream()
                .map(FreelancerPreferencesMapper::toResponse)
                .toList();
    }

    /**
     * Admin : préférences d’un freelancer identifié par le sujet Keycloak ({@code sub}).
     */
    @GetMapping("/freelancer/{subject}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<FreelancerPreferencesResponse> getBySubject(@PathVariable String subject) {
        return preferencesService.getByFreelancerId(subject)
                .map(p -> ResponseEntity.ok(FreelancerPreferencesMapper.toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }
}
