package com.esprit.microservice_proposal.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private Integer id;
    /** Keycloak subject (UUID) — propriétaire du projet côté microservice_project */
    private String clientId;
    private String clientEmail;
    private String title;
    private String description;
    private String category;
    private String skills;
    private Float budget_min;
    private Float budget_max;
    private String duration;
    /** Ex. JUNIOR, INTERMEDIATE, SENIOR — aligné sur le JSON du microservice projet */
    private String experienceLevel;
    private String status;
    private LocalDate deadline;
}