package com.mechsync.modules.users.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptPasswordHasherTest {

    @Test
    void hashesPasswordUsingBCrypt() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(encoder);

        String hash = hasher.hash("Password123!");

        assertNotEquals("Password123!", hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches("Password123!", hash));
    }
}
