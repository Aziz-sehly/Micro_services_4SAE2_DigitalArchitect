package com.esprit.microservice_candidature.controller;

import com.esprit.microservice_candidature.entity.FreelancerPreferences;
import com.esprit.microservice_candidature.service.IFreelancerPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/candidature", "/application"})
public class FreelancerPreferencesRest {

    @Autowired
    private IFreelancerPreferencesService preferencesService;

    /**
     * GET /candidature/all - List all freelancer preferences (admin).
     */
    @GetMapping("/all")
    public ResponseEntity<List<FreelancerPreferences>> getAll() {
        return ResponseEntity.ok(preferencesService.getAll());
    }

    /**
     * GET /candidature/{freelancerId}
     * Returns the freelancer's preferences, or 404 if not found.
     */
    @GetMapping("/{freelancerId}")
    public ResponseEntity<FreelancerPreferences> getByFreelancerId(@PathVariable Integer freelancerId) {
        Optional<FreelancerPreferences> opt = preferencesService.getByFreelancerId(freelancerId);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /candidature/{freelancerId}
     * Creates new preferences for the freelancer.
     */
    @PostMapping("/{freelancerId}")
    public ResponseEntity<FreelancerPreferences> create(
            @PathVariable Integer freelancerId,
            @RequestBody FreelancerPreferences preferences) {
        try {
            FreelancerPreferences created = preferencesService.create(freelancerId, preferences);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /candidature/{freelancerId}
     * Updates existing preferences for the freelancer.
     */
    @PutMapping("/{freelancerId}")
    public ResponseEntity<FreelancerPreferences> update(
            @PathVariable Integer freelancerId,
            @RequestBody FreelancerPreferences preferences) {
        try {
            FreelancerPreferences updated = preferencesService.update(freelancerId, preferences);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /candidature/{freelancerId}
     * Deletes the freelancer's preferences.
     */
    @DeleteMapping("/{freelancerId}")
    public ResponseEntity<Void> delete(@PathVariable Integer freelancerId) {
        try {
            preferencesService.delete(freelancerId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
