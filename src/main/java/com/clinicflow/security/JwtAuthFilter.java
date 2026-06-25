package com.clinicflow.security;

import com.clinicflow.context.TenantContext;
import com.clinicflow.repository.global.TenantRepository;
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
 * 4. Blocks suspended clinics (402), except billing/profile paths so an admin
 *    can still log in and re-subscribe.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TenantRepository tenantRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, TenantRepository tenantRepository) {
        this.jwtUtil = jwtUtil;
        this.tenantRepository = tenantRepository;
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

                // Billing gate: block a suspended clinic from using the app, but
                // allow billing + profile endpoints so they can renew.
                if (tenantId != null && !isBillingExempt(request.getRequestURI())
                        && isSuspended(tenantId)) {
                    TenantContext.clear();
                    response.setStatus(402); // Payment Required
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"error\":\"Subscription inactive — please renew to continue.\"}");
                    return;
                }
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Always clear tenant — prevents thread pool leakage
            TenantContext.clear();
        }
    }

    private boolean isSuspended(String schemaName) {
        try {
            return tenantRepository.findBySchemaName(schemaName)
                .map(t -> "suspended".equals(t.getStatus()))
                .orElse(false);
        } catch (Exception e) {
            // Fail open on lookup errors to avoid locking everyone out.
            return false;
        }
    }

    private boolean isBillingExempt(String uri) {
        return uri.startsWith("/api/subscription")
            || uri.equals("/api/clinic")
            || uri.startsWith("/api/auth")
            || uri.startsWith("/api/public");
    }
}
