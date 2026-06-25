package com.clinicflow.entity.tenant;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "staff")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;               // OTP login identifier

    @Column(nullable = false, length = 20)
    private String role;                // DOCTOR / RECEPTIONIST / NURSE / ADMIN

    @Column(name = "reg_number", length = 40)
    private String regNumber;           // NMC registration (doctors only)

    @Column(length = 60)
    private String specialty;           // General / Gynec / Ortho

    @Column(name = "password_hash", length = 100)
    private String passwordHash;        // BCrypt; null until a password is set

    @Builder.Default
    @Column(name = "must_reset_password")
    private boolean mustResetPassword = false;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
