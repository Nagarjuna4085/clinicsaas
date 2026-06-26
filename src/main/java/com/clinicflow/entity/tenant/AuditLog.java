package com.clinicflow.entity.tenant;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/** An audit-trail entry: who did what to which record, and when. */
@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 80)
    private String actor;           // staff id or "system"

    @Column(name = "actor_role", length = 20)
    private String actorRole;

    @Column(nullable = false, length = 20)
    private String action;          // VIEW / CREATE / UPDATE / DELETE / EXPORT

    @Column(name = "entity_type", length = 30)
    private String entityType;

    @Column(name = "entity_id", length = 40)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Builder.Default
    @Column(name = "logged_at")
    private OffsetDateTime loggedAt = OffsetDateTime.now();
}
