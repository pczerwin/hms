package com.effectivehygiene.hms.training;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Represents a single training/refresher event (immutable historical record).
 *
 * <p>Design intent: an employee may attend training on the same document version
 * more than once (e.g. annual refreshers, remedial sessions). There is therefore
 * <strong>no</strong> unique constraint preventing multiple {@code TrainingInstance}
 * records that cover the same trainees and documents — this is intentional.
 * Compliance status is derived at query time from the full history of records,
 * not from a stored status field.
 */
@Entity
@Table(name = "training_instance")
public class TrainingInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trainer_name", nullable = false, updatable = false, length = 255)
    private String trainerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "trainer_type", nullable = false, updatable = false)
    private TrainerType trainerType;

    @Column(name = "training_start_date", nullable = false, updatable = false)
    private LocalDate trainingStartDate;

    @Column(name = "training_end_date", nullable = false, updatable = false)
    private LocalDate trainingEndDate;

    @Column(name = "training_duration", nullable = false, updatable = false, length = 50)
    private String trainingDuration;

    @Column(name = "training_expiry_date", nullable = false, updatable = false)
    private LocalDate trainingExpiryDate;

    @Column(name = "comments", columnDefinition = "TEXT", updatable = false)
    private String comments;

    @Column(name = "trainer_signature", nullable = false, updatable = false, length = 255)
    private String trainerSignature;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --------------------
    // Lifecycle callbacks
    // --------------------

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    // --------------------
    // Getters & setters
    // --------------------


    public Long getId() {
        return id;
    }


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

    public Instant getCreatedAt() {
        return createdAt;
    }

}
