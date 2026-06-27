package com.clinicflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

public class PatientDto {

    public record Visit(
        String visitDate,
        Short tokenNumber,
        String status,
        String visitType,
        String doctorName,
        String diagnosis,
        String invoiceNumber,
        BigDecimal billTotal
    ) {}

    public record RegisterRequest(
        @NotBlank String name,
        String phone,
        Short age,
        String gender,
        String bloodGroup,
        String abhaId,
        String allergies,
        boolean consent     // patient consented to storing their health data
    ) {}

    public record Response(
        UUID id,
        String uhid,
        String name,
        String phone,
        Short age,
        String gender,
        String bloodGroup,
        String allergies,
        long totalVisits
    ) {}
}
