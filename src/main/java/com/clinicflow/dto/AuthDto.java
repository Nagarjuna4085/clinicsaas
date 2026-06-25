package com.clinicflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public record SendOtpRequest(
        @NotBlank @Pattern(regexp = "\\d{10}") String phone
    ) {}

    public record LoginRequest(
        @NotBlank @Pattern(regexp = "\\d{10}") String phone,
        @NotBlank String password
    ) {}

    // First-login reset: prove the temporary password, then set a real one.
    public record SetPasswordRequest(
        @NotBlank @Pattern(regexp = "\\d{10}") String phone,
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String newPassword
    ) {}

    // Forgot password: OTP to the phone, then set a new password.
    public record ResetPasswordRequest(
        @NotBlank @Pattern(regexp = "\\d{10}") String phone,
        @NotBlank @Pattern(regexp = "\\d{6}") String otp,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String newPassword
    ) {}

    public record AuthResponse(
        String token,
        String role,
        String name,
        String clinicName
    ) {}

    // Login result — token is null when the user must reset their password first.
    public record LoginResponse(
        String token,
        String role,
        String name,
        String clinicName,
        boolean mustReset
    ) {}
}
