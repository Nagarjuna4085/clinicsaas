package com.clinicflow.controller;

import com.clinicflow.dto.AuditDto;
import com.clinicflow.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Audit", description = "Audit trail of who viewed/changed records (ADMIN)")
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "Recent audit entries", description = "The latest 200 audit-trail entries for the clinic. Role: ADMIN.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditDto.Entry>> recent() {
        List<AuditDto.Entry> entries = auditService.recent().stream()
            .map(a -> new AuditDto.Entry(
                a.getAction(), a.getEntityType(), a.getEntityId(),
                a.getActor(), a.getActorRole(),
                a.getLoggedAt() != null ? a.getLoggedAt().toString() : null))
            .collect(Collectors.toList());
        return ResponseEntity.ok(entries);
    }
}
