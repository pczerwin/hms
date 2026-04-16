package com.effectivehygiene.hms.training.dto;

import com.effectivehygiene.hms.training.TrainingInstance;

import java.util.List;

public final class TrainingMapper {

    private TrainingMapper() {
    }

    public static TrainingInstance toEntity(CreateTrainingInstanceRequest request) {
        TrainingInstance returnInstance = new TrainingInstance();
        returnInstance.setTrainerName(normalizeRequiredText(request.getTrainerName()));
        returnInstance.setTrainingDuration(normalizeRequiredText(request.getTrainingDuration()));
        returnInstance.setComments(normalizeOptionalText(request.getComments()));
        returnInstance.setTrainerSignature(normalizeRequiredText(request.getTrainerSignature()));
        returnInstance.setTrainerType(request.getTrainerType());
        returnInstance.setTrainingStartDate(request.getTrainingStartDate());
        returnInstance.setTrainingEndDate(request.getTrainingEndDate());
        returnInstance.setTrainingExpiryDate(request.getTrainingExpiryDate());
        return returnInstance;
    }

    public static TrainingInstanceSingleResponse toSingleResponse(
            TrainingInstance trainingInstance,
            List<Long> employeeIds,
            List<Long> documentVersionIds
    ) {
        return new TrainingInstanceSingleResponse(
                trainingInstance.getId(),
                trainingInstance.getTrainerName(),
                trainingInstance.getTrainerType(),
                trainingInstance.getTrainingStartDate(),
                trainingInstance.getTrainingEndDate(),
                trainingInstance.getTrainingDuration(),
                trainingInstance.getTrainingExpiryDate(),
                trainingInstance.getComments(),
                trainingInstance.getTrainerSignature(),
                trainingInstance.getCreatedAt(),
                employeeIds,
                documentVersionIds
        );
    }

    public static TrainingInstanceMultiResponse toMultiResponse(
            TrainingInstance trainingInstance,
            List<Long> employeeIds,
            List<Long> documentVersionIds
    ) {
        return new TrainingInstanceMultiResponse(
                trainingInstance.getId(),
                trainingInstance.getTrainerName(),
                trainingInstance.getTrainerType(),
                trainingInstance.getTrainingStartDate(),
                trainingInstance.getTrainingEndDate(),
                trainingInstance.getTrainingExpiryDate(),
                trainingInstance.getCreatedAt(),
                employeeIds,
                documentVersionIds
        );
    }

    private static String normalizeRequiredText(String value) {
        if (value == null) {return null;}
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {return null;}
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
