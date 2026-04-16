package com.effectivehygiene.hms.training.dto;

import com.effectivehygiene.hms.training.TrainingStatus;

import java.util.List;
import java.util.Map;

/**
 * Internal intermediate result produced by {@code TrainingComplianceService.buildMatrix()}.
 *
 * <p>Carries the resolved employee/document lists and the pre-calculated status map
 * so the controller can map directly to the API response without re-querying.
 * This record is never exposed to the API directly; see {@link ComplianceMatrixResponse}.
 *
 * @param employees    active employees included in this matrix (may be a single-item list when filtered)
 * @param documents    active document references included in this matrix, with current version details
 * @param statusMatrix empId → (documentReferenceId → TrainingStatus); status enum form for internal use
 */
public record ComplianceMatrix(
        List<EmployeeInfo> employees,
        List<DocumentReferenceInfo> documents,
        Map<Long, Map<Long, TrainingStatus>> statusMatrix
) {

    /**
     * Lightweight projection of an active employee for matrix labelling.
     */
    public record EmployeeInfo(
            Long id,
            String employeeNumber,
            String firstName,
            String lastName,
            String department,
            String jobRole
    ) {}

    /**
     * Lightweight projection of an active document reference, including current version details.
     * Current version fields ({@code currentVersion}, {@code currentVersionName}, {@code currentVersionIssueDate})
     * are {@code null} when no current version exists for the reference.
     */
    public record DocumentReferenceInfo(
            Long id,
            String referenceCode,
            String originDepartment,
            boolean mandatory,
            String currentVersion,
            String currentVersionName,
            java.time.LocalDate currentVersionIssueDate
    ) {}
}


