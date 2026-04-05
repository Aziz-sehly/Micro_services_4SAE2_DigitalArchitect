package com.milestone.milestone.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MilestoneRequest(
        Long contractId,
        String title,
        String deliverable,
        BigDecimal amount,
        LocalDate dueDate
) {}
