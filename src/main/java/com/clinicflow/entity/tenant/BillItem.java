package com.clinicflow.entity.tenant;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bill_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Column(nullable = false, length = 150)
    private String description;         // "Consultation fee", "Dressing"

    @Column(name = "hsn_sac", length = 10)
    private String hsnSac;              // GST code e.g. 999312

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "gst_rate", precision = 4, scale = 1)
    private BigDecimal gstRate = BigDecimal.ZERO; // 0 / 5 / 12 / 18
}
