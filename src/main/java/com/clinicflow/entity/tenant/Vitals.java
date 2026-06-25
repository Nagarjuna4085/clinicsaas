package com.clinicflow.entity.tenant;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vitals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vitals {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private Staff recordedBy;

    @Column(name = "bp_systolic")
    private Short bpSystolic;

    @Column(name = "bp_diastolic")
    private Short bpDiastolic;

    private Short pulse;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperature;

    private Short spo2;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm")
    private Short heightCm;

    @Builder.Default
    @Column(name = "recorded_at")
    private OffsetDateTime recordedAt = OffsetDateTime.now();
}
