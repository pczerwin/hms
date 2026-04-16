package com.effectivehygiene.hms.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility test — run once to generate BCrypt hashes for SQL seed scripts.
 * Not part of the normal test suite; safe to delete after use.
 */
class HashGeneratorTest {

    @Test
    void printAdminHash() {
        String hash = new BCryptPasswordEncoder().encode("admin123");
        System.out.println("BCrypt hash for admin123: " + hash);
    }
}

