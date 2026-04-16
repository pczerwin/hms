package com.effectivehygiene.hms.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * Returns a JSON 200 response on successful login instead of the default 302 redirect.
 * Prevents Postman from following the redirect and landing on the /home page.
 */
public class RestLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ObjectNode body = MAPPER.createObjectNode();
        body.put("status", 200);
        body.put("message", "Login successful");
        body.put("username", authentication.getName());

        MAPPER.writeValue(response.getOutputStream(), body);
    }
}
