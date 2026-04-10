package com.effectivehygiene.hms.logging;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.effectivehygiene.hms.security.CurrentUser.usernameOrSystem;

@Component
public class LoggingContextFilter extends OncePerRequestFilter {

    // Optional: support passing request id from client (useful for tracing across systems)
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1) requestId: use header if provided; otherwise generate a new UUID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // 2) user: based on Spring Security (your helper returns "SYSTEM" if not logged in)
        String user = usernameOrSystem();

        // 3) path: useful for quickly locating which endpoint triggered logs
        String path = request.getRequestURI();

        // Put values into MDC (thread-local context used by logging) [1](https://github.com/spring-projects/spring-boot/issues/45150)[2](https://github.com/spring-projects/spring-security/issues/18211)
        MDC.put("requestId", requestId);
        MDC.put("user", user);
        MDC.put("path", path);

        try {
            // Continue request processing
            filterChain.doFilter(request, response);
        } finally {
            // Always clear MDC to avoid leaking values to the next request on same thread [1](https://github.com/spring-projects/spring-boot/issues/45150)[2](https://github.com/spring-projects/spring-security/issues/18211)
            MDC.clear();
        }
    }
}
