package com.clinicflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClinicDto {

    /**
     * Self-service clinic signup. Requires a one-time OTP verification of the
     * owner phone and an owner password; provisions a new tenant schema and the
     * owner's staff record.
     */
    public record RegisterRequest(
        @NotBlank String clinicName,
        @NotBlank @Pattern(regexp = "\\d{10}") String ownerPhone,
        @NotBlank @Pattern(regexp = "\\d{6}") String otp,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotBlank String ownerName,
        String city,
        String plan,        // starter / clinic / pro / hospital (default starter)
        // The owner is always created as ADMIN. If they also consult, they pass
        // a specialty (and optionally NMC reg) and become bookable as a provider.
        String regNumber,
        String specialty
    ) {}

    public record RegisterResponse(
        String clinicName,
        String schemaName,
        String ownerName,
        String role,
        String message
    ) {}

    public record Profile(
        String clinicName,
        String ownerPhone,
        String city,
        String plan,
        String status,
        String schemaName,
        String trialEndsAt
    ) {}

    public record UpdateRequest(
        @NotBlank String clinicName,
        String city
    ) {}
}
