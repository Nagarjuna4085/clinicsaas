package com.clinicflow.entity.global;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Global lookup: which clinic (schema) a staff phone belongs to.
 * Lives in the "global" schema and lets OTP login resolve the tenant for any
 * staff member, not just the clinic owner.
 */
@Entity
@Table(schema = "global", name = "staff_directory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffDirectory {

    @Id
    @Column(length = 15)
    private String phone;

    @Column(name = "schema_name", nullable = false, length = 60)
    private String schemaName;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
