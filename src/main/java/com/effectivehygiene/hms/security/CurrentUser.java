package com.effectivehygiene.hms.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import org.springframework.security.authentication.AnonymousAuthenticationToken;

public final class CurrentUser {

    // Utility class: prevent instantiation.
    private CurrentUser() {}

    // Resolves the current username for logs/audit, falling back to SYSTEM when unavailable.
    public static String usernameOrSystem() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // No authentication present
        if (auth == null) {
            return "SYSTEM";
        }

        // Treat anonymous as "SYSTEM" (common for audit logs)
        if (auth instanceof AnonymousAuthenticationToken) {
            return "SYSTEM";
        }

        // Some setups might mark auth as unauthenticated
        if (!auth.isAuthenticated()) {
            return "SYSTEM";
        }

        Object principal = auth.getPrincipal();

        // Principal sometimes is a String (e.g., username)
        if (principal instanceof String s) {
            // Normalize the common anonymous marker just in case
            return "anonymousUser".equalsIgnoreCase(s) ? "SYSTEM" : s;
        }

        // Safe fallback: auth name (usually username)
        String name = auth.getName();
        return (name == null || name.isBlank()) ? "SYSTEM" : name;
    }
}
