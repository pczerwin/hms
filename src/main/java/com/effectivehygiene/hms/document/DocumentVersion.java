package com.effectivehygiene.hms.document;


import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "document_version")
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_reference_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_docver_reference")
    )
    private DocumentReference documentReference;

    @Column(name = "document_name", nullable = false, length = 255)
    private String docName;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    /**
     * Business date: when this version of the document was issued/effective.
     */
    @Column(name = "version_issue_date", nullable = false)
    private LocalDate versionIssueDate;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent = false;

    @Column(name = "default_training_validity_days")
    private Integer defaultTrainingValidityDays;

    /**
     * System audit timestamp: when record was created in HMS.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --------------------
    // Lifecycle hook
    // --------------------

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    // --------------------
    // Getters setters
    // --------------------


    public Long getId() {
        return id;
    }


    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDate getVersionIssueDate() {
        return versionIssueDate;
    }

    public void setVersionIssueDate(LocalDate versionIssueDate) {
        this.versionIssueDate = versionIssueDate;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public Integer getDefaultTrainingValidityDays() {
        return defaultTrainingValidityDays;
    }

    public void setDefaultTrainingValidityDays(Integer defaultTrainingValidityDays) {
        this.defaultTrainingValidityDays = defaultTrainingValidityDays;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
