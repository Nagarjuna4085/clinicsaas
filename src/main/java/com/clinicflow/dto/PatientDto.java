package com.clinicflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class PatientDto {

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
