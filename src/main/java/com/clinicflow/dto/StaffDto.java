package com.clinicflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public class StaffDto {

    public record CreateRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "\\d{10}") String phone,
        @NotBlank String role,      // DOCTOR / RECEPTIONIST / NURSE / ADMIN
        String regNumber,           // NMC registration (doctors only)
        String specialty
    ) {}

    public record Response(
        UUID id,
        String name,
        String phone,
        String role,
        String specialty,
        boolean active
    ) {}
}
