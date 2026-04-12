package com.esprit.microservice_candidature.dto;

import com.esprit.microservice_candidature.entity.FreelancerPreferences;

import java.util.ArrayList;
import java.util.List;

public final class FreelancerPreferencesMapper {

    private FreelancerPreferencesMapper() {
    }

    public static FreelancerPreferencesResponse toResponse(FreelancerPreferences entity) {
        FreelancerPreferencesResponse dto = new FreelancerPreferencesResponse();
        dto.setId(entity.getId());
        dto.setFreelancerId(entity.getFreelancerId());
        dto.setSkills(copyList(entity.getSkills()));
        dto.setPreferredProjectTypes(copyList(entity.getPreferredProjectTypes()));
        dto.setMinBudget(entity.getMinBudget());
        dto.setMaxBudget(entity.getMaxBudget());
        dto.setAvgDeliveryDays(entity.getAvgDeliveryDays());
        dto.setOpenToNegotiation(entity.getOpenToNegotiation());
        dto.setOpenToOtherProjectTypes(entity.getOpenToOtherProjectTypes());
        dto.setNotes(entity.getNotes());
        return dto;
    }

    public static FreelancerPreferences fromRequest(FreelancerPreferencesRequest request) {
        FreelancerPreferences entity = new FreelancerPreferences();
        entity.setSkills(copyList(request.getSkills()));
        entity.setPreferredProjectTypes(copyList(request.getPreferredProjectTypes()));
        entity.setMinBudget(request.getMinBudget());
        entity.setMaxBudget(request.getMaxBudget());
        entity.setAvgDeliveryDays(request.getAvgDeliveryDays());
        entity.setOpenToNegotiation(request.getOpenToNegotiation() != null ? request.getOpenToNegotiation() : false);
        entity.setOpenToOtherProjectTypes(
                request.getOpenToOtherProjectTypes() != null ? request.getOpenToOtherProjectTypes() : false);
        entity.setNotes(request.getNotes());
        return entity;
    }

    private static List<String> copyList(List<String> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(source);
    }
}
