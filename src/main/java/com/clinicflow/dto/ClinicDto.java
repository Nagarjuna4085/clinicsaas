package com.clinicflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ClinicDto {

    /**
     * Self-service clinic signup. Provisions a new tenant schema and creates
     * the owner's staff record so they can immediately log in via OTP.
     */
    public record RegisterRequest(
        @NotBlank String clinicName,
        @NotBlank @Pattern(regexp = "\\d{10}") String ownerPhone,
        @NotBlank String ownerName,
        String city,
        String plan,        // starter / clinic / pro / hospital (default starter)
        String role,        // owner's staff role (default DOCTOR)
        String regNumber,   // NMC registration, if the owner is a doctor
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
