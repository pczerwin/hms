package com.effectivehygiene.hms.training.dto;

import com.effectivehygiene.hms.training.TrainerType;

import java.time.Instant;
import java.time.LocalDate;

public record TrainingInstanceResponse(
        Long id,
        String trainerName,
        TrainerType trainerType,
        LocalDate trainingStartDate,
        LocalDate trainingEndDate,
        String trainingDuration,
        LocalDate trainingExpiryDate,
        String comments,
        String trainerSignature,
        Instant createdAt
) {
}

