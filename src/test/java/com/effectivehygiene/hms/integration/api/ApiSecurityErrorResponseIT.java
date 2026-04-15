package com.effectivehygiene.hms.integration.api;

import com.effectivehygiene.hms.security.SecurityConfig;
import com.effectivehygiene.hms.document.DocumentService;
import com.effectivehygiene.hms.employee.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that the security layer produces correct JSON error responses
 * for /api/** endpoints (401 unauthenticated, 403 forbidden) and that
 * authenticated requests pass through.
 */
@WebMvcTest
@Import(SecurityConfig.class)
class ApiSecurityErrorResponseIT extends ApiContractIntegrationTestBase {

    @MockitoBean
    EmployeeService employeeService;

    @MockitoBean
    DocumentService documentService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void unauthenticatedApiCall_returns401JsonError() throws Exception {
        var result = mockMvc.perform(get("/api/test/ping")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 401, "UNAUTHENTICATED");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void authenticatedApiCall_succeeds() throws Exception {
        mockMvc.perform(get("/api/test/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void forbiddenApiCall_returns403JsonError() throws Exception {
        var result = mockMvc.perform(get("/api/test/forbidden")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 403, "FORBIDDEN");
    }
}


