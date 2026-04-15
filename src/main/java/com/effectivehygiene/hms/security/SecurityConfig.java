package com.effectivehygiene.hms.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Configuration
public class SecurityConfig {

    private final JsonMapper objectMapper;

    public SecurityConfig(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        /**
         * This matcher identifies API endpoints.
         * We will apply JSON-style security error responses ONLY for requests matching /api/**.
         *
         * Why?
         * - UI requests: should redirect to /login (good for browsers)
         * - API requests: should return JSON error body (good for frontend/clients)
         */
        var apiMatcher = PathPatternRequestMatcher.pathPattern("/api/**");

        http
                /**
                 * Authorization rules — who can access what.
                 * This block decides which requests require authentication/roles.
                 */
                .authorizeHttpRequests(authorize -> authorize

                        /**
                         * For now: require authentication for everything.
                         *
                         * NOTE: In a real app, you typically allow these without authentication:
                         * - /login
                         * - /error
                         * - /css/**, /js/**, /images/**
                         * But Spring Security's formLogin usually handles /login correctly anyway.
                         */
                        .anyRequest().authenticated()
                )

                /**
                 * Form login — enables a login page and session-based authentication.
                 *
                 * This is for browser users:
                 * - If not logged in and access a protected page, user is redirected to /login.
                 * - After successful login, user gets a session cookie.
                 */
                .formLogin(form -> form

                        /**
                         * Where to go after successful login.
                         * The 'true' means always go to /home even if user originally requested another page.
                         */
                        .defaultSuccessUrl("/home", true)
                )

                /**
                 * Logout support.
                 * Default:
                 * - POST /logout logs you out
                 * - invalidates session
                 */
                .logout(Customizer.withDefaults())

                /**
                 * Exception handling — THIS is the key part for Phase 2.4.
                 *
                 * Spring Security errors often occur BEFORE the request reaches controllers.
                 * That means @ControllerAdvice cannot catch them.
                 *
                 * So we plug custom handlers here.
                 */
                .exceptionHandling(ex -> ex

                        /**
                         * 401 handler (not authenticated) for API endpoints.
                         *
                         * Example:
                         * - GET /api/employees without session cookie
                         * - Instead of redirecting to /login, return JSON:
                         *   {timestamp, status:401, code:"UNAUTHENTICATED", ...}
                         */
                        .defaultAuthenticationEntryPointFor(
                                new RestAuthenticationEntryPoint(objectMapper),
                                apiMatcher
                        )

                        /**
                         * 403 handler (authenticated but forbidden) for API endpoints.
                         *
                         * Example:
                         * - User logged in but lacks required permissions
                         * - Return JSON:
                         *   {timestamp, status:403, code:"FORBIDDEN", ...}
                         */
                        .defaultAccessDeniedHandlerFor(
                                new RestAccessDeniedHandler(objectMapper),
                                apiMatcher
                        )
                )

                /**
                 * CSRF protection.
                 *
                 * CSRF is important for browser form POST requests with cookies/sessions.
                 * You disabled it, which is common in early MVPs, but you MUST revisit it later.
                 *
                 * Rule of thumb:
                 * - If your app uses sessions/cookies and accepts POST/PUT/DELETE from browser: enable CSRF.
                 * - If pure stateless API with tokens: often disabled.
                 */
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * UserDetailsService defines *who can log in* and what roles they have.
     *
     * InMemoryUserDetailsManager is MVP-friendly:
     * - users are stored in memory (NOT DB)
     * - restart app => users reset
     *
     * Later you will replace it with DB-backed users.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {

        UserDetails admin = User.builder()
                .username("admin")

                /**
                 * Password should be stored encoded/hashed.
                 * BCryptPasswordEncoder hashes password.
                 */
                .password(passwordEncoder.encode("admin123"))

                /**
                 * Roles: "ADMIN" becomes "ROLE_ADMIN" under the hood.
                 */
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * PasswordEncoder defines how passwords are hashed.
     *
     * BCrypt is a strong standard choice.
     * Good for MVP and production.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}