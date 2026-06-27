package com.clinicflow.entity.tenant;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Staff doctor;

    @Column(name = "token_number")
    private Short tokenNumber;

    @Builder.Default
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate = LocalDate.now();

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "WAITING";  // WAITING/CONSULTING/COMPLETED/CANCELLED

    @Builder.Default
    @Column(name = "visit_type", length = 20)
    private String visitType = "WALKIN"; // WALKIN/SCHEDULED/FOLLOWUP

    @Column(name = "followup_date")
    private LocalDate followupDate;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;   // set when booked for a future date/time

    @Builder.Default
    @Column(name = "reminder_sent")
    private boolean reminderSent = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
