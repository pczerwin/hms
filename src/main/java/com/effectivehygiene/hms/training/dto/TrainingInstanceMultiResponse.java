package com.effectivehygiene.hms.training.dto;

import com.effectivehygiene.hms.training.TrainerType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record TrainingInstanceMultiResponse(
        Long id,
        String trainerName,
        TrainerType trainerType,
        LocalDate trainingStartDate,
        LocalDate trainingEndDate,
        LocalDate trainingExpiryDate,
        Instant createdAt,
        List<Long> employeeIds,
        List<Long> documentVersionIds
) {
}
