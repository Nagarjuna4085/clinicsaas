package com.clinicflow.service;

import com.clinicflow.entity.tenant.AuditLog;
import com.clinicflow.repository.tenant.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Writes audit-trail entries for sensitive actions (who viewed/changed records).
 * Runs in the current tenant's schema, so each clinic keeps its own audit log.
 */
@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    // REQUIRES_NEW: the audit entry persists independently of the caller's
    // transaction (e.g. read-only views, or an operation that later rolls back).
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, String entityId, String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actor = auth != null ? auth.getName() : "system";
        String role = auth != null
            ? auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .findFirst().orElse("").replace("ROLE_", "")
            : "";
        repo.save(AuditLog.builder()
            .actor(actor)
            .actorRole(role)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .build());
    }

    @Transactional(readOnly = true)
    public List<AuditLog> recent() {
        return repo.findTop200ByOrderByLoggedAtDesc();
    }
}
