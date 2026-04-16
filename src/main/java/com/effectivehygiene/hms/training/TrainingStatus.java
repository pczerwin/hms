package com.effectivehygiene.hms.training;

/**
 * Derived compliance status for a single (employee, documentReference) pair.
 *
 * <p>Priority order — first matching rule wins:
 * <ol>
 *   <li>{@link #COMPLETE} — most recent training covers the CURRENT version AND is not expired</li>
 *   <li>{@link #OUTDATED_DOCUMENT} — most recent training covers a NON-CURRENT version AND is not expired</li>
 *   <li>{@link #EXPIRED_TRAINING} — most recent training EXISTS but IS expired (any version)</li>
 *   <li>{@link #INCOMPLETE} — no training record exists for this pair</li>
 * </ol>
 *
 * <p>This status is never stored; it is calculated at query time from immutable training records.
 */
public enum TrainingStatus {
    COMPLETE,
    OUTDATED_DOCUMENT,
    EXPIRED_TRAINING,
    INCOMPLETE
}

