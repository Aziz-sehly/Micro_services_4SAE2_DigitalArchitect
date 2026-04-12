package com.esprit.microservice_candidature.service;

import com.esprit.microservice_candidature.entity.FreelancerPreferences;
import com.esprit.microservice_candidature.repository.FreelancerPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class FreelancerPreferencesService implements IFreelancerPreferencesService {

    @Autowired
    private FreelancerPreferencesRepository repository;

    @Override
    public List<FreelancerPreferences> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<FreelancerPreferences> getByFreelancerId(String freelancerId) {
        return repository.findByFreelancerId(freelancerId);
    }

    @Override
    @Transactional
    public FreelancerPreferences create(String freelancerId, FreelancerPreferences preferences) {
        if (repository.findByFreelancerId(freelancerId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Des préférences existent déjà pour ce freelancer. Utilisez PUT pour les mettre à jour.");
        }
        preferences.setFreelancerId(freelancerId);
        return repository.save(preferences);
    }

    @Override
    @Transactional
    public FreelancerPreferences update(String freelancerId, FreelancerPreferences preferences) {
        FreelancerPreferences existing = repository.findByFreelancerId(freelancerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Préférences introuvables pour ce freelancer."));

        existing.setSkills(preferences.getSkills() != null ? preferences.getSkills() : existing.getSkills());
        existing.setPreferredProjectTypes(
                preferences.getPreferredProjectTypes() != null
                        ? preferences.getPreferredProjectTypes()
                        : existing.getPreferredProjectTypes());
        existing.setMinBudget(preferences.getMinBudget());
        existing.setMaxBudget(preferences.getMaxBudget());
        existing.setAvgDeliveryDays(preferences.getAvgDeliveryDays());
        existing.setOpenToNegotiation(
                preferences.getOpenToNegotiation() != null ? preferences.getOpenToNegotiation() : false);
        existing.setOpenToOtherProjectTypes(
                preferences.getOpenToOtherProjectTypes() != null
                        ? preferences.getOpenToOtherProjectTypes()
                        : false);
        existing.setNotes(preferences.getNotes());

        return repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(String freelancerId) {
        FreelancerPreferences existing = repository.findByFreelancerId(freelancerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Préférences introuvables pour ce freelancer."));
        existing.getSkills().clear();
        existing.getPreferredProjectTypes().clear();
        repository.flush();
        repository.delete(existing);
    }
}
