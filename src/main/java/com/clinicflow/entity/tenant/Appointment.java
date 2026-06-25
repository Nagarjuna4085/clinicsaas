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

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate = LocalDate.now();

    @Column(nullable = false, length = 20)
    private String status = "WAITING";  // WAITING/CONSULTING/COMPLETED/CANCELLED

    @Column(name = "visit_type", length = 20)
    private String visitType = "WALKIN"; // WALKIN/SCHEDULED/FOLLOWUP

    @Column(name = "followup_date")
    private LocalDate followupDate;

    @Column(name = "reminder_sent")
    private boolean reminderSent = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
