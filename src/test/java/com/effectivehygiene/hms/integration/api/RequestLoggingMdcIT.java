package com.effectivehygiene.hms.integration.api;

import com.effectivehygiene.hms.security.SecurityConfig;
import com.effectivehygiene.hms.document.DocumentService;
import com.effectivehygiene.hms.employee.EmployeeService;
import com.effectivehygiene.hms.logging.LoggingContextFilter;
import com.effectivehygiene.hms.training.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that LoggingContextFilter propagates MDC keys
 * (requestId, user, path) correctly during request processing.
 */
@WebMvcTest
@Import({SecurityConfig.class, LoggingContextFilter.class})
class RequestLoggingMdcIT {

    @MockitoBean
    EmployeeService employeeService;

    @MockitoBean
    DocumentService documentService;

    @MockitoBean
    TrainingService trainingService;

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(username = "testadmin")
    void requestWithCustomRequestId_propagatesInMdc() throws Exception {
        mockMvc.perform(get("/api/test/mdc")
                        .header("X-Request-Id", "test-req-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("test-req-123"))
                .andExpect(jsonPath("$.path").value("/api/test/mdc"));
    }

    @Test
    @WithMockUser(username = "testadmin")
    void requestWithoutRequestId_generatesUuid() throws Exception {
        mockMvc.perform(get("/api/test/mdc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(jsonPath("$.requestId").value(
                        matchesPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
    }

    @Test
    @WithMockUser(username = "testadmin")
    void authenticatedRequest_includesUsernameInMdc() throws Exception {
        mockMvc.perform(get("/api/test/mdc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("testadmin"));
    }
}


