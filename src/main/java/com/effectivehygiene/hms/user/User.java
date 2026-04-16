package com.effectivehygiene.hms.user;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * System access account (manager/admin).
 * Not to be confused with Employee — employees are training subjects,
 * users are the people who operate the system.
 * Soft-deleted via the {@code active} flag; never physically removed.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Login username — must be unique across all accounts.
     */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /**
     * BCrypt-hashed password. Raw passwords are never stored.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Soft delete flag.
     * Deactivated users cannot log in but their records are retained for audit purposes.
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * Audit timestamps (UTC).
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // --------------------
    // Lifecycle callbacks
    // --------------------

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // --------------------
    // Getters & setters
    // --------------------

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

