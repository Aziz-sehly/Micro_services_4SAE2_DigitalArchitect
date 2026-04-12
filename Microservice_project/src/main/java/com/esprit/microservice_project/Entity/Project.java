package com.esprit.microservice_project.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String clientId;
    private String clientEmail;

    private String title;
    private String description;
    private String category;
    private String skills;
    private Float budget_min;
    private Float budget_max;
    private String duration;

    @Enumerated(EnumType.STRING)
    private Experience experienceLevel;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate deadline;
}