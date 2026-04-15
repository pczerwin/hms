package com.effectivehygiene.hms.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    // Configures session-based security and ensures /api/** returns JSON for 401/403 errors.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        var apiMatcher = PathPatternRequestMatcher.pathPattern("/api/**");

        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .defaultSuccessUrl("/home", true)
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


    // Defines BCrypt as the hashing algorithm for stored user passwords.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}