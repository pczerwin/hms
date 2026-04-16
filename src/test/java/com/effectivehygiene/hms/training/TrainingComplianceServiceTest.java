package com.effectivehygiene.hms.training;

import com.effectivehygiene.hms.document.DocumentReference;
import com.effectivehygiene.hms.document.DocumentReferenceRepository;
import com.effectivehygiene.hms.document.DocumentVersion;
import com.effectivehygiene.hms.document.DocumentVersionRepository;
import com.effectivehygiene.hms.domain.exception.EntityNotFoundException;
import com.effectivehygiene.hms.employee.Employee;
import com.effectivehygiene.hms.employee.EmployeeRepository;
import com.effectivehygiene.hms.training.dto.ComplianceMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TrainingComplianceService}.
 * All repositories are mocked — no Spring context or DB required.
 */
@ExtendWith(MockitoExtension.class)
class TrainingComplianceServiceTest {

    @Mock
    TrainingInstanceRepository trainingInstanceRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    DocumentReferenceRepository documentReferenceRepository;
    @Mock
    DocumentVersionRepository documentVersionRepository;

    @InjectMocks
    TrainingComplianceService service;

    // ── Fixtures ───────────────────────────────────────────────────────────

    private Employee activeEmployee;
    private DocumentReference activeDocRef;

    @BeforeEach
    void setUp() {
        activeEmployee = makeEmployee(1L, true);
        activeDocRef = makeDocRef(10L, true);
    }

    // ── calculateStatus — 4 status scenarios ───────────────────────────────

    @Test
    void calculateStatus_noTraining_returnsIncomplete() {
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.of());

        assertThat(service.calculateStatus(1L, 10L)).isEqualTo(TrainingStatus.INCOMPLETE);
    }

    @Test
    void calculateStatus_currentVersionNotExpired_returnsComplete() {
        TrainingInstance ti = makeTrainingInstance(LocalDate.now().plusYears(1));
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.<Object[]>of(new Object[]{ti, true}));

        assertThat(service.calculateStatus(1L, 10L)).isEqualTo(TrainingStatus.COMPLETE);
    }

    @Test
    void calculateStatus_nonCurrentVersionNotExpired_returnsOutdatedDocument() {
        TrainingInstance ti = makeTrainingInstance(LocalDate.now().plusYears(1));
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.<Object[]>of(new Object[]{ti, false}));

        assertThat(service.calculateStatus(1L, 10L)).isEqualTo(TrainingStatus.OUTDATED_DOCUMENT);
    }

    @Test
    void calculateStatus_expiredTraining_returnsExpiredTraining() {
        // Expired yesterday — any version
        TrainingInstance ti = makeTrainingInstance(LocalDate.now().minusDays(1));
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.<Object[]>of(new Object[]{ti, true}));

        assertThat(service.calculateStatus(1L, 10L)).isEqualTo(TrainingStatus.EXPIRED_TRAINING);
    }

    @Test
    void calculateStatus_expiresExactlyToday_notExpired() {
        // Valid on expiry date itself (inclusive)
        TrainingInstance ti = makeTrainingInstance(LocalDate.now());
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.<Object[]>of(new Object[]{ti, true}));

        assertThat(service.calculateStatus(1L, 10L)).isEqualTo(TrainingStatus.COMPLETE);
    }

    // ── isExpired boundary ─────────────────────────────────────────────────

    @Test
    void isExpired_expiryDateYesterday_returnsTrue() {
        TrainingInstance ti = makeTrainingInstance(LocalDate.now().minusDays(1));
        assertThat(service.isExpired(ti)).isTrue();
    }

    @Test
    void isExpired_expiryDateToday_returnsFalse() {
        TrainingInstance ti = makeTrainingInstance(LocalDate.now());
        assertThat(service.isExpired(ti)).isFalse();
    }

    @Test
    void isExpired_expiryDateTomorrow_returnsFalse() {
        TrainingInstance ti = makeTrainingInstance(LocalDate.now().plusDays(1));
        assertThat(service.isExpired(ti)).isFalse();
    }

    // ── buildMatrix — filter validation ───────────────────────────────────

    @Test
    void buildMatrix_invalidEmployeeId_throws404() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buildMatrix(999L, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void buildMatrix_inactiveEmployee_throws404() {
        Employee inactive = makeEmployee(2L, false);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.buildMatrix(2L, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("2");
    }

    @Test
    void buildMatrix_invalidDocRefId_throws404() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(documentReferenceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buildMatrix(1L, 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void buildMatrix_inactiveDocRef_throws404() {
        DocumentReference inactive = makeDocRef(20L, false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(documentReferenceRepository.findById(20L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.buildMatrix(1L, 20L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("20");
    }

    @Test
    void buildMatrix_noFilters_returnsMatrixForAllActiveEntities() {
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(activeEmployee));
        when(documentReferenceRepository.findByActiveTrue()).thenReturn(List.of(activeDocRef));
        when(documentVersionRepository.findByDocumentReferenceAndIsCurrentTrue(activeDocRef))
                .thenReturn(Optional.empty());

        TrainingInstance ti = makeTrainingInstance(LocalDate.now().plusYears(1));
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.<Object[]>of(new Object[]{ti, true}));

        ComplianceMatrix matrix = service.buildMatrix(null, null);

        assertThat(matrix.employees()).hasSize(1);
        assertThat(matrix.documents()).hasSize(1);
        assertThat(matrix.statusMatrix().get(1L).get(10L)).isEqualTo(TrainingStatus.COMPLETE);
    }

    @Test
    void buildMatrix_currentVersionPresentOnDocRef_isIncludedInDocumentInfo() {
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(activeEmployee));
        when(documentReferenceRepository.findByActiveTrue()).thenReturn(List.of(activeDocRef));

        DocumentVersion cv = makeDocumentVersion("v2.0", "Cleaning SOP", LocalDate.of(2026, 1, 15), activeDocRef);
        when(documentVersionRepository.findByDocumentReferenceAndIsCurrentTrue(activeDocRef))
                .thenReturn(Optional.of(cv));
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.of());

        ComplianceMatrix matrix = service.buildMatrix(null, null);

        var docInfo = matrix.documents().get(0);
        assertThat(docInfo.currentVersion()).isEqualTo("v2.0");
        assertThat(docInfo.currentVersionName()).isEqualTo("Cleaning SOP");
        assertThat(docInfo.currentVersionIssueDate()).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void buildMatrix_noCurrentVersion_nullVersionFieldsInDocumentInfo() {
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(activeEmployee));
        when(documentReferenceRepository.findByActiveTrue()).thenReturn(List.of(activeDocRef));
        when(documentVersionRepository.findByDocumentReferenceAndIsCurrentTrue(activeDocRef))
                .thenReturn(Optional.empty());
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.of());

        ComplianceMatrix matrix = service.buildMatrix(null, null);

        var docInfo = matrix.documents().get(0);
        assertThat(docInfo.currentVersion()).isNull();
        assertThat(docInfo.currentVersionName()).isNull();
        assertThat(docInfo.currentVersionIssueDate()).isNull();
    }

    @Test
    void buildMatrix_filterByEmployee_returnsOnlyThatRow() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(documentReferenceRepository.findByActiveTrue()).thenReturn(List.of(activeDocRef));
        when(documentVersionRepository.findByDocumentReferenceAndIsCurrentTrue(activeDocRef))
                .thenReturn(Optional.empty());
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.of());

        ComplianceMatrix matrix = service.buildMatrix(1L, null);

        assertThat(matrix.employees()).hasSize(1).extracting("id").containsExactly(1L);
        assertThat(matrix.statusMatrix().get(1L).get(10L)).isEqualTo(TrainingStatus.INCOMPLETE);
    }

    @Test
    void buildMatrix_filterByDocRef_returnsOnlyThatColumn() {
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(activeEmployee));
        when(documentReferenceRepository.findById(10L)).thenReturn(Optional.of(activeDocRef));
        when(documentVersionRepository.findByDocumentReferenceAndIsCurrentTrue(activeDocRef))
                .thenReturn(Optional.empty());
        when(trainingInstanceRepository.findMostRecentTrainingWithVersionStatus(1L, 10L))
                .thenReturn(List.of());

        ComplianceMatrix matrix = service.buildMatrix(null, 10L);

        assertThat(matrix.documents()).hasSize(1).extracting("id").containsExactly(10L);
    }

    // ── Factory helpers ────────────────────────────────────────────────────
    private Employee makeEmployee(Long id, boolean active) {
        Employee e = new Employee();
        e.setFirstName("First" + id);
        e.setLastName("Last" + id);
        // Reflectively set id — Employee uses @GeneratedValue so no setter
        try {
            var field = Employee.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(e, id);
            if (!active) {
                e.deactivate();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return e;
    }

    private DocumentReference makeDocRef(Long id, boolean active) {
        DocumentReference d = new DocumentReference();
        d.setReferenceCode("REF-" + id);
        d.setOriginDepartment("QA");
        d.setActive(active);
        try {
            var field = DocumentReference.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(d, id);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return d;
    }

    private TrainingInstance makeTrainingInstance(LocalDate expiryDate) {
        TrainingInstance ti = new TrainingInstance();
        ti.setTrainerName("Trainer");
        ti.setTrainerType(TrainerType.CONTRACTOR);
        ti.setTrainingStartDate(LocalDate.now());
        ti.setTrainingEndDate(LocalDate.now());
        ti.setTrainingDuration("1h");
        ti.setTrainingExpiryDate(expiryDate);
        ti.setTrainerSignature("SIG");
        // Simulate @PrePersist
        try {
            var field = TrainingInstance.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(ti, Instant.now());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return ti;
    }

    private DocumentVersion makeDocumentVersion(String version, String name, LocalDate issueDate,
                                                 DocumentReference docRef) {
        DocumentVersion dv = new DocumentVersion();
        dv.setVersion(version);
        dv.setDocumentName(name);
        dv.setVersionIssueDate(issueDate);
        dv.setCurrent(true);
        dv.setDocumentReference(docRef);
        return dv;
    }
}









