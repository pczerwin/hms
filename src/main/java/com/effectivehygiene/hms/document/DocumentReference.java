package com.effectivehygiene.hms.document;


import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "document_reference")
public class DocumentReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * Logical document identifier (e.g. SOP number).
     * Versions are handled in DocumentVersion.
     */
    @Column(name = "reference_code", nullable = false, unique = true, length = 100)
    private String referenceCode;

    @Column(name = "origin_department", nullable = false, length = 50)
    private String originDepartment;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory = false;


    /**
     * Soft delete flag.
     * Document references are never physically deleted.
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

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

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getOriginDepartment() {
        return originDepartment;
    }

    public void setOriginDepartment(String originDepartment) {
        this.originDepartment = originDepartment;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
