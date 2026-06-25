package com.clinicflow.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public class AppointmentDto {

    public record CreateRequest(
        @NotNull UUID patientId,
        @NotNull UUID doctorId,
        String visitType,   // WALKIN / SCHEDULED / FOLLOWUP
        int opFee,
        String paymentMode
    ) {}

    public record QueueItem(
        UUID id,
        Short tokenNumber,
        String patientName,
        Short age,
        String gender,
        String status,
        String visitType,
        String createdAt
    ) {}

    public record StatusUpdate(
        @NotNull String status  // CONSULTING / COMPLETED / CANCELLED
    ) {}
}
