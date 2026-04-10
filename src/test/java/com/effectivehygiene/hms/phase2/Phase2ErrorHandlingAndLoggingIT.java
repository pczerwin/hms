package com.effectivehygiene.hms.phase2;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



import ch.qos.logback.classic.Logger;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

/**
 * Phase 2 integration tests:
 * 2.1 Validation -> ApiErrorResponse (400)
 * 2.2 Security -> JSON errors for /api/** (401/403)
 * 2.3 Logging -> MDC keys exist on log events (requestId/user/path)
 */


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})

class Phase2ErrorHandlingAndLoggingIT {

    @Autowired
    MockMvc mockMvc;

    // -------------------------
    // 2.1 Validation (400 JSON)
    // -------------------------
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN") // must be authenticated to reach controller
    void validationFailure_returnsApiErrorResponseContract() throws Exception {
        // Missing required field "name" -> triggers validation exception
        String invalidJson = "{}";

        mockMvc.perform(post("/api/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Assert our standard error contract fields exist and are correct
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/test/validate"));
    }

    // ------------------------------
    // 2.2 Security (401/403 as JSON)
    // ------------------------------
    @Test
    void unauthenticatedApiCall_returns401JsonApiErrorResponse() throws Exception {
        // No authentication -> should be 401 with JSON body (not redirect/html)
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/ping"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void forbiddenApiCall_returns403JsonApiErrorResponse() throws Exception {
        // Authenticated but not allowed -> should be 403 with JSON body
        mockMvc.perform(get("/api/admin-only"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/admin-only"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminApiCall_succeeds() throws Exception {
        // Admin role should pass the @PreAuthorize check
        mockMvc.perform(get("/api/admin-only"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    // --------------------------
    // 2.3 MDC present in logging
    // --------------------------
    private ListAppender<ILoggingEvent> listAppender;

    @AfterEach
    void detachAppender() {
        // Clean up the temporary log capture appender after each test
        if (listAppender != null) {
            Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            root.detachAppender(listAppender);
            listAppender = null;
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void mdcKeys_arePresentOnLogEvents_duringRequest() throws Exception {
        // Capture logs in-memory so we can assert MDC fields exist
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        listAppender = new ListAppender<>();
        listAppender.start();
        root.addAppender(listAppender);

        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        // Find the specific log line produced by ping() and check MDC keys
        List<ILoggingEvent> events = listAppender.list;

        ILoggingEvent pingEvent = events.stream()
                .filter(e -> "Ping called".equals(e.getFormattedMessage()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected log message 'Ping called' not found"));

        Map<String, String> mdc = pingEvent.getMDCPropertyMap();

        // These keys must be set by LoggingContextFilter
        assertThat(mdc).containsKeys("requestId", "user", "path");
        assertThat(mdc.get("requestId")).isNotBlank();
        assertThat(mdc.get("user")).isEqualTo("admin");
        assertThat(mdc.get("path")).isEqualTo("/api/ping");
    }

    // ------------------------------------------------------------
    // Test-only API endpoints used to trigger validation/security/logging
    // ------------------------------------------------------------
    @TestConfiguration
    @EnableMethodSecurity // enables @PreAuthorize so 403 test is meaningful
    static class TestApiConfig {

        @Bean
        TestApiController testApiController() {
            return new TestApiController();
        }
    }

    @RestController
    static class TestApiController {

        private static final org.slf4j.Logger log =
                LoggerFactory.getLogger(TestApiController.class);

        @GetMapping("/api/ping")
        String ping() {
            log.info("Ping called"); // used by MDC test to find the correct log event
            return "ok";
        }

        @GetMapping("/api/admin-only")
        @PreAuthorize("hasRole('ADMIN')")
        String adminOnly() {
            return "ok";
        }

        @PostMapping("/api/test/validate")
        String validate(@RequestBody @jakarta.validation.Valid TestDto dto) {
            return "ok";
        }
    }

    // DTO used only to trigger validation failure when "name" is missing/blank
    public record TestDto(
            @NotBlank(message = "name is required")
            String name
    ) {}
}

