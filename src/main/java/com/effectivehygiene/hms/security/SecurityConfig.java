package com.effectivehygiene.hms.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    // Configures session-based security with explicit SecurityContext persistence.
    // Returns JSON on login success/failure instead of HTML redirects.
    // Ensures /api/** returns JSON for 401/403 errors.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        var apiMatcher = PathPatternRequestMatcher.pathPattern("/api/**");

        http
                // Persist SecurityContext to HTTP session so authentication survives across requests
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository())
                )
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // Return JSON 200 on success instead of redirect to /home
                        .successHandler(new RestLoginSuccessHandler())
                        // Return JSON 401 on failure instead of redirect to /login?error
                        .failureHandler(new RestLoginFailureHandler())
                )
                .logout(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new RestAuthenticationEntryPoint(),
                                apiMatcher
                        )
                        .defaultAccessDeniedHandlerFor(
                                new RestAccessDeniedHandler(),
                                apiMatcher
                        )
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // Persists the authenticated SecurityContext to the HTTP session (JSESSIONID).
    // Required so authentication state is restored on subsequent requests.
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // Defines BCrypt as the hashing algorithm for stored user passwords.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}