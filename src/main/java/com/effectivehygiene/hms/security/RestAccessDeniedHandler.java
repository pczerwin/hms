package com.effectivehygiene.hms.security;

import com.effectivehygiene.hms.api.error.ApiErrorResponse;
import com.effectivehygiene.hms.api.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;

public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                403,
                ErrorCode.FORBIDDEN,
                "Access is denied.",
                request.getRequestURI()
        );

        response.setStatus(403);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getOutputStream(), body);
    }
}

