package com.clinicflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public class PrescriptionDto {

    public record CreateRequest(
        @NotNull UUID appointmentId,
        String chiefComplaint,
        String diagnosis,
        String examination,
        String advice,
        LocalDate followupDate,
        List<ItemRequest> medicines
    ) {
        record ItemRequest(
            @NotBlank String medicineName,
            String dosage,
            String frequency,
            String duration,
            String instructions
        ) {}
    }

    public record Response(
        UUID id,
        String pdfUrl,
        boolean whatsappSent,
        List<ItemResponse> medicines
    ) {
        record ItemResponse(
            String medicineName,
            String dosage,
            String frequency,
            String duration,
            String instructions
        ) {}
    }
}
