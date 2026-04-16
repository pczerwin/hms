package com.effectivehygiene.hms.training;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainingInstanceRepository extends JpaRepository<TrainingInstance, Long> {

    /**
     * Returns all TrainingInstance records for the given employee on the given document reference,
     * ordered most-recent first (by createdAt DESC).
     *
     * <p>Only considers training documents whose parent DocumentReference is active.
     * The caller typically takes the first result (index 0) as the most recent training.
     * This method is provided for flexibility; {@link #findMostRecentTrainingWithVersionStatus} is more efficient
     * for single-cell lookups since it returns at most 1 row and includes version status in one query.
     *
     * @param employeeId    the employee's PK
     * @param documentRefId the DocumentReference PK
     * @return list of matching TrainingInstances, most recent first; empty if none exist
     */
    @Query("""
            SELECT ti
            FROM TrainingInstance ti
            JOIN TrainingTrainee tt ON tt.trainingInstance = ti
            JOIN TrainingDocument td ON td.trainingInstance = ti
            JOIN DocumentVersion dv ON td.documentVersion = dv
            JOIN DocumentReference dr ON dv.documentReference = dr
            WHERE tt.employee.id = :employeeId
              AND dr.id = :documentRefId
              AND dr.active = TRUE
            ORDER BY ti.createdAt DESC
            """)
    List<TrainingInstance> findTrainingsForEmployeeOnDocRef(
            @Param("employeeId") Long employeeId,
            @Param("documentRefId") Long documentRefId);

    /**
     * Returns the single most-recent TrainingInstance and version status for the given employee × document pair.
     * This is the method used by {@link com.effectivehygiene.hms.training.TrainingComplianceService#calculateStatus}.
     *
     * <p>Result columns: [0] = TrainingInstance, [1] = Boolean isCurrent (whether the version is current).
     * Only active document references are considered. Returns at most 1 row.
     *
     * <p><strong>Pitfall Note:</strong> The Boolean isCurrent must be cast carefully; it's a boxed Boolean
     * and may be null in edge cases (though in this query it should always be non-null since we're selecting
     * from a DocumentVersion which has a non-nullable {@code isCurrent} field). Caller is responsible for
     * safe casting: {@code (Boolean) row[1]}.
     *
     * @param employeeId    the employee's PK
     * @param documentRefId the DocumentReference PK
     * @return list of Object[] rows; empty if no training exists for this pair
     */
    @Query("""
            SELECT ti, dv.isCurrent
            FROM TrainingInstance ti
            JOIN TrainingTrainee tt ON tt.trainingInstance = ti
            JOIN TrainingDocument td ON td.trainingInstance = ti
            JOIN DocumentVersion dv ON td.documentVersion = dv
            JOIN DocumentReference dr ON dv.documentReference = dr
            WHERE tt.employee.id = :employeeId
              AND dr.id = :documentRefId
              AND dr.active = TRUE
            ORDER BY ti.createdAt DESC
            LIMIT 1
            """)
    List<Object[]> findMostRecentTrainingWithVersionStatus(
            @Param("employeeId") Long employeeId,
            @Param("documentRefId") Long documentRefId);
}


