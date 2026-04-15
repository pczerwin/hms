package com.effectivehygiene.hms.training;

import com.effectivehygiene.hms.document.DocumentVersion;
import jakarta.persistence.*;

@Entity
@Table(
        name = "training_document",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_training_document",
                columnNames = {"training_instance_id", "document_version_id"}
        )
)
public class TrainingDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "training_instance_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_td_training")
    )
    private TrainingInstance trainingInstance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_version_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_td_document_version")
    )
    private DocumentVersion documentVersion;

    public Long getId() {
        return id;
    }

    public TrainingInstance getTrainingInstance() {
        return trainingInstance;
    }

    public void setTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstance = trainingInstance;
    }

    public DocumentVersion getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(DocumentVersion documentVersion) {
        this.documentVersion = documentVersion;
    }
}
