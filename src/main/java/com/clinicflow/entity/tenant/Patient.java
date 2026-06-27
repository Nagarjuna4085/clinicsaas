package com.clinicflow.entity.tenant;

import com.clinicflow.config.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String uhid;                // e.g. VJW-00042

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 15)
    private String phone;

    private Short age;

    private LocalDate dob;

    @Column(length = 10)
    private String gender;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String address;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "abha_id")
    private String abhaId;              // Govt ABHA health ID (encrypted at rest)

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String allergies;           // Shown as warning during Rx (encrypted at rest)

    @Builder.Default
    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "consent_at")
    private OffsetDateTime consentAt;   // when the patient consented to data storage

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;   // set when PII is erased (right to erasure)
}
