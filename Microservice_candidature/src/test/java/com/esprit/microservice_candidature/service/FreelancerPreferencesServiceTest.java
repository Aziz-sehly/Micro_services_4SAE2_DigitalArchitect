package com.esprit.microservice_candidature.service;

import com.esprit.microservice_candidature.entity.FreelancerPreferences;
import com.esprit.microservice_candidature.repository.FreelancerPreferencesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreelancerPreferencesServiceTest {

    @Mock
    private FreelancerPreferencesRepository repository;

    @InjectMocks
    private FreelancerPreferencesService service;

    @Test
    void create_shouldThrowConflict_whenPreferencesAlreadyExist() {
        String freelancerId = "freelancer-1";
        FreelancerPreferences payload = new FreelancerPreferences();
        when(repository.findByFreelancerId(freelancerId)).thenReturn(Optional.of(new FreelancerPreferences()));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> service.create(freelancerId, payload));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(repository, times(0)).save(any(FreelancerPreferences.class));
    }

    @Test
    void create_shouldSetFreelancerIdAndSave_whenPreferencesDoNotExist() {
        String freelancerId = "freelancer-2";
        FreelancerPreferences payload = new FreelancerPreferences();
        when(repository.findByFreelancerId(freelancerId)).thenReturn(Optional.empty());
        when(repository.save(payload)).thenReturn(payload);

        FreelancerPreferences result = service.create(freelancerId, payload);

        assertEquals(freelancerId, result.getFreelancerId());
        verify(repository).save(payload);
    }

    @Test
    void update_shouldThrowNotFound_whenPreferencesDoNotExist() {
        String freelancerId = "unknown";
        when(repository.findByFreelancerId(freelancerId)).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class,
                        () -> service.update(freelancerId, new FreelancerPreferences()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void update_shouldMergeFieldsAndUseBooleanDefaults() {
        String freelancerId = "freelancer-3";
        FreelancerPreferences existing = new FreelancerPreferences();
        existing.setFreelancerId(freelancerId);
        existing.setSkills(new ArrayList<>(List.of("java")));
        existing.setPreferredProjectTypes(new ArrayList<>(List.of("api")));
        existing.setOpenToNegotiation(true);
        existing.setOpenToOtherProjectTypes(true);

        FreelancerPreferences payload = new FreelancerPreferences();
        payload.setSkills(new ArrayList<>(List.of("spring")));
        payload.setPreferredProjectTypes(null);
        payload.setMinBudget(100.0);
        payload.setMaxBudget(200.0);
        payload.setAvgDeliveryDays(7);
        payload.setOpenToNegotiation(null);
        payload.setOpenToOtherProjectTypes(null);
        payload.setNotes("notes");

        when(repository.findByFreelancerId(freelancerId)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        FreelancerPreferences result = service.update(freelancerId, payload);

        assertEquals(List.of("spring"), result.getSkills());
        assertEquals(List.of("api"), result.getPreferredProjectTypes());
        assertEquals(100.0, result.getMinBudget());
        assertEquals(200.0, result.getMaxBudget());
        assertEquals(7, result.getAvgDeliveryDays());
        assertFalse(result.getOpenToNegotiation());
        assertFalse(result.getOpenToOtherProjectTypes());
        assertEquals("notes", result.getNotes());
        verify(repository).save(existing);
    }

    @Test
    void delete_shouldClearCollectionsFlushAndDelete() {
        String freelancerId = "freelancer-4";
        FreelancerPreferences existing = new FreelancerPreferences();
        existing.setSkills(new ArrayList<>(List.of("java", "sql")));
        existing.setPreferredProjectTypes(new ArrayList<>(List.of("mobile")));

        when(repository.findByFreelancerId(freelancerId)).thenReturn(Optional.of(existing));

        service.delete(freelancerId);

        assertEquals(0, existing.getSkills().size());
        assertEquals(0, existing.getPreferredProjectTypes().size());
        verify(repository).flush();
        verify(repository).delete(existing);
    }
}
