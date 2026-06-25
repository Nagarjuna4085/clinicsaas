package com.clinicflow.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final Key key;
    private final long expirationMs;

    public JwtUtil(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        // Fail loud (in logs) if the insecure dev default is still in use.
        if (secret == null || secret.startsWith("dev-only")) {
            log.warn("!!! Using the DEVELOPMENT JWT secret. Set a strong JWT_SECRET " +
                "(>=32 random chars) before deploying to production. !!!");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    /**
     * Generate JWT containing userId, role, tenantId (schema name).
     * tenantId is how Spring Boot knows which clinic's schema to use.
     */
    public String generate(String userId, String role, String tenantId) {
        return Jwts.builder()
            .subject(userId)
            .claim("role", role)
            .claim("tenantId", tenantId)        // e.g. "tenant_ramesh_vjw"
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) key)
            .build().parseSignedClaims(token).getPayload();
    }

    public String extractUserId(String token)   { return parse(token).getSubject(); }
    public String extractRole(String token)     { return parse(token).get("role", String.class); }
    public String extractTenantId(String token) { return parse(token).get("tenantId", String.class); }

    public boolean isValid(String token) {
        try { parse(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }
}
