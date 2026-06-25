package com.clinicflow.security;

import com.clinicflow.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/**
 * Runs before every request.
 * 1. Extracts JWT from Authorization header
 * 2. Sets TenantContext (which schema Hibernate will use)
 * 3. Sets Spring Security principal (role-based access)
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.isValid(token)) {
                String tenantId = jwtUtil.extractTenantId(token);
                String role     = jwtUtil.extractRole(token);
                String userId   = jwtUtil.extractUserId(token);

                // KEY STEP: tell Hibernate which schema to use for this request
                TenantContext.set(tenantId);

                // Tell Spring Security who this user is
                var auth = new UsernamePasswordAuthenticationToken(
                    userId, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Always clear tenant — prevents thread pool leakage
            TenantContext.clear();
        }
    }
}
