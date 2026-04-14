package com.effectivehygiene.hms.integration.api;

import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Test-only REST controller providing lightweight API endpoints for
 * integration tests (security, validation contract, MDC propagation).
 * Detected by {@code @WebMvcTest} component scan since it's a @RestController
 * under the application's base package.
 */
@RestController
@RequestMapping("/api/test")
class ApiTestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    /** Simulates a controller-level AccessDeniedException for 403 contract testing. */
    @GetMapping("/forbidden")
    public String forbidden() {
        throw new AccessDeniedException("Test: access denied");
    }

    /** Returns current MDC values so tests can assert on logging context propagation. */
    @GetMapping("/mdc")
    public Map<String, String> mdc() {
        Map<String, String> ctx = new HashMap<>();
        ctx.put("requestId", MDC.get("requestId") != null ? MDC.get("requestId") : "");
        ctx.put("user", MDC.get("user") != null ? MDC.get("user") : "");
        ctx.put("path", MDC.get("path") != null ? MDC.get("path") : "");
        return ctx;
    }
}
