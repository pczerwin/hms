package com.effectivehygiene.hms.training.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * API response body for {@code GET /api/compliance/matrix}.
 *
 * <p>The {@code matrix} field is a nested map: employeeId → (documentReferenceId → status string).
 * Status string values match {@link com.effectivehygiene.hms.training.TrainingStatus} enum names:
 * {@code COMPLETE}, {@code OUTDATED_DOCUMENT}, {@code EXPIRED_TRAINING}, {@code INCOMPLETE}.
 *
 * <p>The {@code documents} array includes current version details ({@code currentVersion},
 * {@code currentVersionName}, {@code currentVersionIssueDate}); these are {@code null} if no
 * current version is assigned to the document reference.
 *
 * @param timestamp   UTC instant when this matrix was calculated (server time)
 * @param employees   active employees included in this matrix
 * @param documents   active document references with current version details
 * @param matrix      empId → (docRefId → status string)
 */
public record ComplianceMatrixResponse(
        Instant timestamp,
        List<EmployeeResponse> employees,
        List<DocumentReferenceResponse> documents,
        Map<Long, Map<Long, String>> matrix
) {

    /**
     * Employee information in the matrix response.
     */
    public record EmployeeResponse(
            Long id,
            String employeeNumber,
            String firstName,
            String lastName,
            String department,
            String jobRole
    ) {}

    /**
     * Document reference information with current version details.
     * Version fields are {@code null} if no current version is assigned.
     */
    public record DocumentReferenceResponse(
            Long id,
            String referenceCode,
            String originDepartment,
            boolean mandatory,
            String currentVersion,
            String currentVersionName,
            java.time.LocalDate currentVersionIssueDate
    ) {}
}


