package com.effectivehygiene.hms.training.dto;

import com.effectivehygiene.hms.training.TrainingInstance;

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

    public static TrainingInstanceResponse toResponse(TrainingInstance trainingInstance) {
        return new TrainingInstanceResponse(
                trainingInstance.getId(),
                trainingInstance.getTrainerName(),
                trainingInstance.getTrainerType(),
                trainingInstance.getTrainingStartDate(),
                trainingInstance.getTrainingEndDate(),
                trainingInstance.getTrainingDuration(),
                trainingInstance.getTrainingExpiryDate(),
                trainingInstance.getComments(),
                trainingInstance.getTrainerSignature(),
                trainingInstance.getCreatedAt()
        );
    }


    private static String normalizeRequiredText(String value) {
        if (value == null) {return null;}
        return value.trim();
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {return null;}

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
