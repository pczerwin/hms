package com.effectivehygiene.hms.integration.api;

import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Shared helpers for integration tests that verify the ApiErrorResponse contract.
 * The five-field contract: timestamp, status, code, message, path.
 */
abstract class ApiContractIntegrationTestBase {

    /**
     * Assert the fixed ApiErrorResponse JSON shape on the given result.
     */
    protected void assertApiErrorContract(ResultActions result,
                                          int expectedStatus,
                                          String expectedCode) throws Exception {
        result
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.code").value(expectedCode))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").isNotEmpty());
    }
}

