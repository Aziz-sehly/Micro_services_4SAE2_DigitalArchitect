package com.esprit.microservice_candidature.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FreelancerPreferencesRequest {

    private List<String> skills = new ArrayList<>();
    private List<String> preferredProjectTypes = new ArrayList<>();
    private Double minBudget;
    private Double maxBudget;
    private Integer avgDeliveryDays;
    private Boolean openToNegotiation;
    private Boolean openToOtherProjectTypes;
    private String notes;
}
