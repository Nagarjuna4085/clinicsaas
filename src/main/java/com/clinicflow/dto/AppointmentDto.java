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
        String paymentMode,
        String scheduledAt  // optional ISO date-time (yyyy-MM-ddTHH:mm) for a future booking
    ) {}

    public record QueueItem(
        UUID id,
        Short tokenNumber,
        String patientName,
        Short age,
        String gender,
        String status,
        String visitType,
        String createdAt,
        String scheduledAt
    ) {}

    public record StatusUpdate(
        @NotNull String status  // CONSULTING / COMPLETED / CANCELLED
    ) {}
}
