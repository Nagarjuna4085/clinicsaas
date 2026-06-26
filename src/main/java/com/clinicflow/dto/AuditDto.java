package com.clinicflow.dto;

public class AuditDto {
    public record Entry(
        String action,
        String entityType,
        String entityId,
        String actor,
        String actorRole,
        String loggedAt
    ) {}
}
