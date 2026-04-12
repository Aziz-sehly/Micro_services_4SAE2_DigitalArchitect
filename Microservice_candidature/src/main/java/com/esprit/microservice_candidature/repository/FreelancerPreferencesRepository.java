package com.esprit.microservice_candidature.repository;

import com.esprit.microservice_candidature.entity.FreelancerPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreelancerPreferencesRepository extends JpaRepository<FreelancerPreferences, Integer> {
    Optional<FreelancerPreferences> findByFreelancerId(String freelancerId);
}
