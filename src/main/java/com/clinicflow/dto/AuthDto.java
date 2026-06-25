package com.clinicflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthDto {

    public record SendOtpRequest(
        @NotBlank @Pattern(regexp = "\\d{10}") String phone
    ) {}

    public record VerifyOtpRequest(
        @NotBlank @Pattern(regexp = "\\d{10}") String phone,
        @NotBlank @Pattern(regexp = "\\d{6}")  String otp
    ) {}

    public record AuthResponse(
        String token,
        String role,
        String name,
        String clinicName
    ) {}
}
