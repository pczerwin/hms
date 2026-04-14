package com.effectivehygiene.hms.integration.api;

import com.effectivehygiene.hms.security.SecurityConfig;
import com.effectivehygiene.hms.employee.EmployeeController;
import com.effectivehygiene.hms.employee.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that DTO validation failures produce the correct
 * ApiErrorResponse contract (400 + VALIDATION_FAILED).
 */
@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class ApiValidationErrorResponseIT extends ApiContractIntegrationTestBase {

    @MockitoBean
    EmployeeService employeeService;

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void invalidPayload_returnsValidationErrorContract() throws Exception {
        var result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 400, "VALIDATION_FAILED");
    }
}

