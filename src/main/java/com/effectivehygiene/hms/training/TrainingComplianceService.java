package com.effectivehygiene.hms.training;

import com.effectivehygiene.hms.document.DocumentReference;
import com.effectivehygiene.hms.document.DocumentReferenceRepository;
import com.effectivehygiene.hms.document.DocumentVersion;
import com.effectivehygiene.hms.document.DocumentVersionRepository;
import com.effectivehygiene.hms.domain.exception.EntityNotFoundException;
import com.effectivehygiene.hms.employee.Employee;
import com.effectivehygiene.hms.employee.EmployeeRepository;
import com.effectivehygiene.hms.training.dto.ComplianceMatrix;
import com.effectivehygiene.hms.training.dto.ComplianceMatrix.DocumentReferenceInfo;
import com.effectivehygiene.hms.training.dto.ComplianceMatrix.EmployeeInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Read-only service that derives compliance status for each active employee × active document
 * reference pair. Status is never stored — it is calculated dynamically from immutable training records.
 *
 * <p>Status priority (first match wins):
 * <ol>
 *   <li>COMPLETE — most recent training is on the CURRENT version AND not expired</li>
 *   <li>OUTDATED_DOCUMENT — most recent training is on a NON-CURRENT version AND not expired</li>
 *   <li>EXPIRED_TRAINING — most recent training EXISTS but IS expired (any version)</li>
 *   <li>INCOMPLETE — no training record exists for this pair</li>
 * </ol>
 */
@Service
@Transactional(readOnly = true)
public class TrainingComplianceService {

    private final TrainingInstanceRepository trainingInstanceRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentReferenceRepository documentReferenceRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public TrainingComplianceService(TrainingInstanceRepository trainingInstanceRepository,
                                     EmployeeRepository employeeRepository,
                                     DocumentReferenceRepository documentReferenceRepository,
                                     DocumentVersionRepository documentVersionRepository) {
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.employeeRepository = employeeRepository;
        this.documentReferenceRepository = documentReferenceRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Builds the full compliance matrix, optionally narrowed by a single employee and/or
     * a single document reference.
     *
     * @param employeeIdFilter    when non-null, restrict rows to this employee (must be active, else 404)
     * @param documentRefIdFilter when non-null, restrict columns to this document reference (must be active, else 404)
     * @return fully populated {@link ComplianceMatrix}
     * @throws EntityNotFoundException if a supplied filter ID does not exist or belongs to an inactive entity
     */
    public ComplianceMatrix buildMatrix(Long employeeIdFilter, Long documentRefIdFilter) {

        List<Employee> employees = resolveEmployees(employeeIdFilter);
        List<DocumentReference> docRefs = resolveDocumentReferences(documentRefIdFilter);

        // Bulk-fetch current versions for all docRefs; caching avoids repeated lookups.
        // Map allows null values for docRefs with no current version.
        Map<Long, DocumentVersion> currentVersionByDocRefId = new LinkedHashMap<>();
        for (DocumentReference dr : docRefs) {
            currentVersionByDocRefId.put(
                    dr.getId(),
                    documentVersionRepository.findByDocumentReferenceAndIsCurrentTrue(dr).orElse(null));
        }

        // LinkedHashMap preserves insertion order for consistent API response ordering.
        Map<Long, Map<Long, TrainingStatus>> statusMatrix = new LinkedHashMap<>();

        // Calculate status for each (employee, docRef) pair.
        for (Employee emp : employees) {
            Map<Long, TrainingStatus> row = new LinkedHashMap<>();
            for (DocumentReference docRef : docRefs) {
                row.put(docRef.getId(), calculateStatus(emp.getId(), docRef.getId()));
            }
            statusMatrix.put(emp.getId(), row);
        }

        List<EmployeeInfo> employeeInfos = employees.stream()
                .map(e -> new EmployeeInfo(
                        e.getId(),
                        e.getEmployeeNumber(),
                        e.getFirstName(),
                        e.getLastName(),
                        e.getDepartment(),
                        e.getJobRole()))
                .toList();

        List<DocumentReferenceInfo> docRefInfos = docRefs.stream()
                .map(d -> {
                    DocumentVersion cv = currentVersionByDocRefId.get(d.getId());
                    return new DocumentReferenceInfo(
                            d.getId(),
                            d.getReferenceCode(),
                            d.getOriginDepartment(),
                            d.isMandatory(),
                            cv != null ? cv.getVersion() : null,
                            cv != null ? cv.getDocumentName() : null,
                            cv != null ? cv.getVersionIssueDate() : null);
                })
                .toList();

        return new ComplianceMatrix(employeeInfos, docRefInfos, statusMatrix);
    }

    /**
     * Calculates the compliance status for a single (employeeId, documentReferenceId) pair.
     *
     * <p>Uses the most recent training record (by {@code createdAt} DESC) that covers this
     * document reference (active references only). The JPQL query returns at most 1 row.
     *
     * @param employeeId    PK of the employee
     * @param documentRefId PK of the document reference
     * @return derived {@link TrainingStatus}
     */
    public TrainingStatus calculateStatus(Long employeeId, Long documentRefId) {
        List<Object[]> rows = trainingInstanceRepository
                .findMostRecentTrainingWithVersionStatus(employeeId, documentRefId);

        // No training found → employee has not attended training on this document.
        if (rows.isEmpty()) {
            return TrainingStatus.INCOMPLETE;
        }

        // Extract TrainingInstance and the boolean isCurrent flag from the query result.
        Object[] row = rows.get(0);
        TrainingInstance mostRecent = (TrainingInstance) row[0];
        boolean isCurrentVersion = (Boolean) row[1];

        // Expiry takes precedence: if training is expired, status is EXPIRED_TRAINING regardless of version.
        if (isExpired(mostRecent)) {
            return TrainingStatus.EXPIRED_TRAINING;
        }

        // If not expired, status depends on whether training covers the current version.
        return isCurrentVersion ? TrainingStatus.COMPLETE : TrainingStatus.OUTDATED_DOCUMENT;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * A training is expired when today is strictly after the expiry date.
     * Valid on the expiry date itself (inclusive).
     *
     * <p>Example: training expires 2026-04-16
     * - 2026-04-16: NOT expired (use trainings)
     * - 2026-04-17: EXPIRED (do not use)
     */
    boolean isExpired(TrainingInstance ti) {
        return LocalDate.now().isAfter(ti.getTrainingExpiryDate());
    }

    /**
     * Resolve active employees: if filter is provided, return singleton list;
     * otherwise return all active employees. Throws 404 if filter ID is invalid/inactive.
     */
    private List<Employee> resolveEmployees(Long employeeIdFilter) {
        if (employeeIdFilter == null) {
            return employeeRepository.findByActiveTrue();
        }
        Employee emp = employeeRepository.findById(employeeIdFilter)
                .filter(Employee::isActive)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Employee not found or inactive: id=" + employeeIdFilter));
        return List.of(emp);
    }

    /**
     * Resolve active document references: if filter is provided, return singleton list;
     * otherwise return all active references. Throws 404 if filter ID is invalid/inactive.
     */
    private List<DocumentReference> resolveDocumentReferences(Long documentRefIdFilter) {
        if (documentRefIdFilter == null) {
            return documentReferenceRepository.findByActiveTrue();
        }
        DocumentReference docRef = documentReferenceRepository.findById(documentRefIdFilter)
                .filter(DocumentReference::isActive)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Document reference not found or inactive: id=" + documentRefIdFilter));
        return List.of(docRef);
    }
}







