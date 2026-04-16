package com.effectivehygiene.hms.security;

import com.effectivehygiene.hms.api.error.ApiErrorResponse;
import com.effectivehygiene.hms.api.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;

/**
 * Returns a JSON 401 response on failed login instead of the default 302 redirect to /login?error.
 * Prevents Postman from following the redirect and landing on the HTML error page.
 */
public class RestLoginFailureHandler implements AuthenticationFailureHandler {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                401,
                ErrorCode.UNAUTHENTICATED,
                "Invalid username or password",
                request.getRequestURI()
        );

        MAPPER.writeValue(response.getOutputStream(), body);
    }
}
