package com.esprit.microservice_candidature.service;

import com.esprit.microservice_candidature.entity.FreelancerPreferences;

import java.util.List;
import java.util.Optional;

public interface IFreelancerPreferencesService {
    List<FreelancerPreferences> getAll();

    Optional<FreelancerPreferences> getByFreelancerId(String freelancerId);

    FreelancerPreferences create(String freelancerId, FreelancerPreferences preferences);

    FreelancerPreferences update(String freelancerId, FreelancerPreferences preferences);

    void delete(String freelancerId);
}
