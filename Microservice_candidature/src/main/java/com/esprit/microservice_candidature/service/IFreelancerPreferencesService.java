package com.esprit.microservice_candidature.service;

import com.esprit.microservice_candidature.entity.FreelancerPreferences;

import java.util.List;
import java.util.Optional;

public interface IFreelancerPreferencesService {
    List<FreelancerPreferences> getAll();
    Optional<FreelancerPreferences> getByFreelancerId(Integer freelancerId);
    FreelancerPreferences create(Integer freelancerId, FreelancerPreferences preferences);
    FreelancerPreferences update(Integer freelancerId, FreelancerPreferences preferences);
    void delete(Integer freelancerId);
}
