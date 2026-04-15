package com.effectivehygiene.hms.security;


import com.effectivehygiene.hms.api.error.ApiErrorResponse;
import com.effectivehygiene.hms.api.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper objectMapper;

    public RestAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.objectMapper = jsonMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                401,
                ErrorCode.UNAUTHENTICATED,
                "Authentication required.",
                request.getRequestURI()
        );

        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
