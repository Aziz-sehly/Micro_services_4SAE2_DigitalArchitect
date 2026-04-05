package com.esprit.microservice_candidature.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "freelancer_preferences", uniqueConstraints = @UniqueConstraint(columnNames = "freelancer_id"))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FreelancerPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "freelancer_id", nullable = false, unique = true)
    private Integer freelancerId;

    @ElementCollection
    @CollectionTable(name = "preferences_skills", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "preferences_project_types", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "project_type")
    private List<String> preferredProjectTypes = new ArrayList<>();

    @Column(name = "min_budget")
    private Double minBudget;

    @Column(name = "max_budget")
    private Double maxBudget;

    @Column(name = "avg_delivery_days")
    private Integer avgDeliveryDays;

    @Column(name = "open_to_negotiation")
    private Boolean openToNegotiation = false;

    @Column(name = "open_to_other_project_types")
    private Boolean openToOtherProjectTypes = false;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
