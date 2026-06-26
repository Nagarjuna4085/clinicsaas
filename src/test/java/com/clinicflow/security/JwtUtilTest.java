package com.clinicflow.security;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "0123456789012345678901234567890123456789"; // 40 chars

    @Test
    void generatesAndParsesClaims() {
        JwtUtil jwt = new JwtUtil(SECRET, 60_000);
        String token = jwt.generate("user-1", "ADMIN", "tenant_9876543210");

        assertThat(jwt.isValid(token)).isTrue();
        assertThat(jwt.extractUserId(token)).isEqualTo("user-1");
        assertThat(jwt.extractRole(token)).isEqualTo("ADMIN");
        assertThat(jwt.extractTenantId(token)).isEqualTo("tenant_9876543210");
    }

    @Test
    void rejectsGarbageToken() {
        JwtUtil jwt = new JwtUtil(SECRET, 60_000);
        assertThat(jwt.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void rejectsExpiredToken() {
        JwtUtil jwt = new JwtUtil(SECRET, -1_000); // already expired
        String token = jwt.generate("user-1", "ADMIN", "tenant_x");
        assertThat(jwt.isValid(token)).isFalse();
    }
}
