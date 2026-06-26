package com.clinicflow.entity.global;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Global table — one row per clinic that signs up.
 * Lives in the "global" PostgreSQL schema.
 */
@Entity
@Table(schema = "global", name = "tenants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "schema_name", nullable = false, unique = true, length = 60)
    private String schemaName;          // e.g. tenant_ramesh_vjw

    @Column(name = "clinic_name", nullable = false, length = 120)
    private String clinicName;          // e.g. "Dr. Ramesh Clinic"

    @Column(name = "owner_phone", nullable = false, length = 15)
    private String ownerPhone;          // used for OTP login

    @Column(length = 60)
    private String city;

    @Column(nullable = false, length = 20)
    private String plan = "starter";    // starter / clinic / pro / hospital

    @Column(nullable = false, length = 20)
    private String status = "trial";    // active / trial / suspended

    @Column(name = "trial_ends_at")
    private OffsetDateTime trialEndsAt;

    @Column(name = "razorpay_sub_id", length = 60)
    private String razorpaySubId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "consent_at")
    private OffsetDateTime consentAt;   // when the owner accepted Terms/Privacy

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (trialEndsAt == null) trialEndsAt = OffsetDateTime.now().plusDays(90);
    }
}
