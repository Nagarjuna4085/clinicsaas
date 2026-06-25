package com.clinicflow.entity.tenant;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "prescription_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;        // e.g. "Paracetamol 500mg"

    @Column(length = 50)
    private String dosage;              // e.g. "1 tablet"

    @Column(length = 50)
    private String frequency;           // e.g. "1-0-1" or "TDS"

    @Column(length = 30)
    private String duration;            // e.g. "5 days"

    @Column(columnDefinition = "TEXT")
    private String instructions;        // e.g. "After food"

    @Column(name = "sort_order")
    private Short sortOrder = 0;
}
