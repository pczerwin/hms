package com.effectivehygiene.hms.integration.api;

import com.effectivehygiene.hms.domain.exception.EntityNotFoundException;
import com.effectivehygiene.hms.security.SecurityConfig;
import com.effectivehygiene.hms.training.ComplianceMatrixController;
import com.effectivehygiene.hms.training.TrainingComplianceService;
import com.effectivehygiene.hms.training.TrainingStatus;
import com.effectivehygiene.hms.training.dto.ComplianceMatrix;
import com.effectivehygiene.hms.training.dto.ComplianceMatrix.DocumentReferenceInfo;
import com.effectivehygiene.hms.training.dto.ComplianceMatrix.EmployeeInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComplianceMatrixController.class)
@Import(SecurityConfig.class)
class ApiComplianceMatrixIT extends ApiContractIntegrationTestBase {

    @MockitoBean
    TrainingComplianceService complianceService;

    @Autowired
    MockMvc mockMvc;

    // ── Fixtures ───────────────────────────────────────────────────────────

    private static final EmployeeInfo EMP_1 =
            new EmployeeInfo(1L, "EMP-001", "Alice", "Smith", "QA", "Analyst");

    private static final DocumentReferenceInfo DOC_10 =
            new DocumentReferenceInfo(10L, "SOP-001", "Production", true, "v2.0", "Cleaning SOP", java.time.LocalDate.of(2026, 1, 1));

    private ComplianceMatrix singleCellMatrix(TrainingStatus status) {
        Map<Long, Map<Long, TrainingStatus>> m = new LinkedHashMap<>();
        m.put(1L, Map.of(10L, status));
        return new ComplianceMatrix(List.of(EMP_1), List.of(DOC_10), m);
    }

    // ── Authentication guard ───────────────────────────────────────────────

    @Test
    void getMatrix_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/compliance/matrix").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ── Happy path — all statuses ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMatrix_noFilter_returns200WithCompleteStatus() throws Exception {
        when(complianceService.buildMatrix(null, null))
                .thenReturn(singleCellMatrix(TrainingStatus.COMPLETE));

        mockMvc.perform(get("/api/compliance/matrix").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.employees[0].id").value(1))
                .andExpect(jsonPath("$.employees[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.documents[0].referenceCode").value("SOP-001"))
                .andExpect(jsonPath("$.documents[0].currentVersion").value("v2.0"))
                .andExpect(jsonPath("$.documents[0].currentVersionName").value("Cleaning SOP"))
                .andExpect(jsonPath("$.documents[0].currentVersionIssueDate").value("2026-01-01"))
                .andExpect(jsonPath("$.matrix['1']['10']").value("COMPLETE"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMatrix_outdatedDocumentStatus_isRepresentedCorrectly() throws Exception {
        when(complianceService.buildMatrix(null, null))
                .thenReturn(singleCellMatrix(TrainingStatus.OUTDATED_DOCUMENT));

        mockMvc.perform(get("/api/compliance/matrix").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matrix['1']['10']").value("OUTDATED_DOCUMENT"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMatrix_expiredTrainingStatus_isRepresentedCorrectly() throws Exception {
        when(complianceService.buildMatrix(null, null))
                .thenReturn(singleCellMatrix(TrainingStatus.EXPIRED_TRAINING));

        mockMvc.perform(get("/api/compliance/matrix").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matrix['1']['10']").value("EXPIRED_TRAINING"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMatrix_incompleteStatus_isRepresentedCorrectly() throws Exception {
        when(complianceService.buildMatrix(null, null))
                .thenReturn(singleCellMatrix(TrainingStatus.INCOMPLETE));

        mockMvc.perform(get("/api/compliance/matrix").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matrix['1']['10']").value("INCOMPLETE"));
    }

    // ── Filter by employeeId ───────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMatrix_filterByEmployeeId_passesFilterToService() throws Exception {
        when(complianceService.buildMatrix(1L, null))
                .thenReturn(singleCellMatrix(TrainingStatus.COMPLETE));

        mockMvc.perform(get("/api/compliance/matrix")
                        .param("employeeId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMatrix_unknownEmployeeId_returns404() throws Exception {
        when(complianceService.buildMatrix(999L, null))
                .thenThrow(new EntityNotFoundException("Employee not found or inactive: id=999"));

        var result = mockMvc.perform(get("/api/compliance/matrix")
                        .param("employeeId", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 404, "ENTITY_NOT_FOUND");
    }

    // ── Filter by documentRefId ────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMatrix_filterByDocumentRefId_passesFilterToService() throws Exception {
        when(complianceService.buildMatrix(null, 10L))
                .thenReturn(singleCellMatrix(TrainingStatus.COMPLETE));

        mockMvc.perform(get("/api/compliance/matrix")
                        .param("documentRefId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents[0].id").value(10));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMatrix_unknownDocumentRefId_returns404() throws Exception {
        when(complianceService.buildMatrix(null, 999L))
                .thenThrow(new EntityNotFoundException("Document reference not found or inactive: id=999"));

        var result = mockMvc.perform(get("/api/compliance/matrix")
                        .param("documentRefId", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 404, "ENTITY_NOT_FOUND");
    }

    // ── Both filters combined ──────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMatrix_bothFilters_passedToService() throws Exception {
        when(complianceService.buildMatrix(1L, 10L))
                .thenReturn(singleCellMatrix(TrainingStatus.COMPLETE));

        mockMvc.perform(get("/api/compliance/matrix")
                        .param("employeeId", "1")
                        .param("documentRefId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matrix['1']['10']").value("COMPLETE"));
    }
}



