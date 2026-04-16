package com.effectivehygiene.hms.training;

import com.effectivehygiene.hms.training.dto.ComplianceMatrix;
import com.effectivehygiene.hms.training.dto.ComplianceMatrixResponse;
import com.effectivehygiene.hms.training.dto.ComplianceMatrixResponse.DocumentReferenceResponse;
import com.effectivehygiene.hms.training.dto.ComplianceMatrixResponse.EmployeeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only API for the compliance matrix.
 *
 * <pre>
 * GET /api/compliance/matrix                         — all active employees × all active doc refs
 * GET /api/compliance/matrix?employeeId={id}         — single employee × all active doc refs
 * GET /api/compliance/matrix?documentRefId={id}      — all active employees × single doc ref
 * GET /api/compliance/matrix?employeeId={e}&amp;documentRefId={d} — single cell
 * </pre>
 *
 * <p>Returns 404 if a filter ID references a non-existent or inactive entity.
 * Returns 401 for unauthenticated requests (handled by security filter chain).
 */
@RestController
@RequestMapping("/api/compliance")
public class ComplianceMatrixController {

    private final TrainingComplianceService complianceService;

    public ComplianceMatrixController(TrainingComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    /**
     * Returns the compliance matrix, optionally filtered.
     *
     * @param employeeId    optional — restrict to one active employee (404 if inactive/missing)
     * @param documentRefId optional — restrict to one active document reference (404 if inactive/missing)
     * @return 200 OK with {@link ComplianceMatrixResponse}
     */
    @GetMapping("/matrix")
    public ResponseEntity<ComplianceMatrixResponse> getMatrix(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long documentRefId) {

        ComplianceMatrix matrix = complianceService.buildMatrix(employeeId, documentRefId);

        ComplianceMatrixResponse response = toResponse(matrix);
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------------
    // Mapping helpers
    // -----------------------------------------------------------------------

    private ComplianceMatrixResponse toResponse(ComplianceMatrix matrix) {
        // Map internal employee projections to API response records.
        List<EmployeeResponse> employees = matrix.employees().stream()
                .map(e -> new EmployeeResponse(
                        e.id(),
                        e.employeeNumber(),
                        e.firstName(),
                        e.lastName(),
                        e.department(),
                        e.jobRole()))
                .toList();

        // Map internal document projections to API response records, including current version details.
        List<DocumentReferenceResponse> documents = matrix.documents().stream()
                .map(d -> new DocumentReferenceResponse(
                        d.id(),
                        d.referenceCode(),
                        d.originDepartment(),
                        d.mandatory(),
                        d.currentVersion(),
                        d.currentVersionName(),
                        d.currentVersionIssueDate()))
                .toList();

        // Convert enum TrainingStatus.COMPLETE → string "COMPLETE" for JSON serialization.
        // LinkedHashMap preserves insertion order for predictable JSON output.
        Map<Long, Map<Long, String>> stringMatrix = new LinkedHashMap<>();
        matrix.statusMatrix().forEach((empId, docMap) -> {
            Map<Long, String> stringRow = new LinkedHashMap<>();
            docMap.forEach((docRefId, status) -> stringRow.put(docRefId, status.name()));
            stringMatrix.put(empId, stringRow);
        });

        // timestamp is set to server's current time (UTC) to indicate when the matrix was calculated.
        return new ComplianceMatrixResponse(Instant.now(), employees, documents, stringMatrix);
    }
}


