package com.clinicflow.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class VitalsDto {

    public record CreateRequest(
        @NotNull UUID appointmentId,
        Short bpSystolic,
        Short bpDiastolic,
        Short pulse,
        BigDecimal temperature,
        Short spo2,
        BigDecimal weightKg,
        Short heightCm
    ) {}

    public record Response(
        UUID id,
        Short bpSystolic,
        Short bpDiastolic,
        Short pulse,
        BigDecimal temperature,
        Short spo2,
        BigDecimal weightKg,
        Short heightCm,
        String recordedByName,
        String recordedAt
    ) {}
}
