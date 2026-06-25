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

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
