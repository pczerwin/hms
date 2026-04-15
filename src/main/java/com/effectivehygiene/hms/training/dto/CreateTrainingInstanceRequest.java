package com.effectivehygiene.hms.training.dto;

import com.effectivehygiene.hms.training.TrainerType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class CreateTrainingInstanceRequest {

    @NotBlank(message = "Trainer name is required")
    @Size(max = 255, message = "Trainer name must be at most 255 characters")
    private String trainerName;

    @NotNull(message = "Trainer type is required")
    private TrainerType trainerType;

    @NotNull(message = "Training start date is required")
    @PastOrPresent(message = "Training start date must be in the past or present")
    private LocalDate trainingStartDate;

    @NotNull(message = "Training end date is required")
    @PastOrPresent(message = "Training end date must be in the past or present")
    private LocalDate trainingEndDate;

    @NotBlank(message = "Training duration is required")
    @Size(max = 50, message = "Training duration must be at most 50 characters")
    private String trainingDuration;

    @NotNull(message = "Training expiry date is required")
    @FutureOrPresent(message = "Training expiry date must be in the present or future")
    private LocalDate trainingExpiryDate;

    @Size(max = 2000, message = "Comments must be at most 2000 characters")
    private String comments;

    @NotBlank(message = "Trainer signature is required")
    @Size(max = 255, message = "Trainer signature must be at most 255 characters")
    private String trainerSignature;

    @NotNull(message = "Employee IDs are required")
    @NotEmpty(message = "At least one employee must attend the training")
    private List<Long> employeeIds;

    @NotNull(message = "Document version IDs are required")
    @NotEmpty(message = "At least one document version must be covered in the training")
    private List<Long> documentVersionIds;

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public TrainerType getTrainerType() {
        return trainerType;
    }

    public void setTrainerType(TrainerType trainerType) {
        this.trainerType = trainerType;
    }

    public LocalDate getTrainingStartDate() {
        return trainingStartDate;
    }

    public void setTrainingStartDate(LocalDate trainingStartDate) {
        this.trainingStartDate = trainingStartDate;
    }

    public LocalDate getTrainingEndDate() {
        return trainingEndDate;
    }

    public void setTrainingEndDate(LocalDate trainingEndDate) {
        this.trainingEndDate = trainingEndDate;
    }

    public String getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(String trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public LocalDate getTrainingExpiryDate() {
        return trainingExpiryDate;
    }

    public void setTrainingExpiryDate(LocalDate trainingExpiryDate) {
        this.trainingExpiryDate = trainingExpiryDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTrainerSignature() {
        return trainerSignature;
    }

    public void setTrainerSignature(String trainerSignature) {
        this.trainerSignature = trainerSignature;
    }

    public List<Long> getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(List<Long> employeeIds) {
        this.employeeIds = employeeIds;
    }

    public List<Long> getDocumentVersionIds() {
        return documentVersionIds;
    }

    public void setDocumentVersionIds(List<Long> documentVersionIds) {
        this.documentVersionIds = documentVersionIds;
    }
}

