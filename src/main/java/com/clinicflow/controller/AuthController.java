package com.clinicflow.controller;

import com.clinicflow.dto.AuthDto;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.entity.tenant.Staff;
import com.clinicflow.repository.global.StaffDirectoryRepository;
import com.clinicflow.repository.global.TenantRepository;
import com.clinicflow.repository.tenant.StaffRepository;
import com.clinicflow.security.JwtUtil;
import com.clinicflow.service.Msg91Service;
import com.clinicflow.context.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phone OTP authentication.
 * POST /api/auth/send-otp   → sends OTP via MSG91
 * POST /api/auth/verify-otp → verifies OTP, returns JWT
 */
@Tag(name = "Auth", description = "Phone OTP login. Public — no JWT required.")
@SecurityRequirements // overrides the global bearerAuth: these endpoints are public
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // In-memory OTP store for MVP — replace with Redis in production
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private final TenantRepository tenantRepo;
    private final StaffDirectoryRepository directoryRepo;
    private final StaffRepository staffRepo;
    private final JwtUtil jwtUtil;
    private final Msg91Service msg91Service;
    private final long otpExpiryMs;

    public AuthController(TenantRepository tenantRepo,
                          StaffDirectoryRepository directoryRepo,
                          StaffRepository staffRepo,
                          JwtUtil jwtUtil,
                          Msg91Service msg91Service,
                          @Value("${app.otp.expiry-minutes:10}") long otpExpiryMinutes) {
        this.tenantRepo = tenantRepo;
        this.directoryRepo = directoryRepo;
        this.staffRepo = staffRepo;
        this.jwtUtil = jwtUtil;
        this.msg91Service = msg91Service;
        this.otpExpiryMs = otpExpiryMinutes * 60_000L;
    }

    @Operation(summary = "Send OTP", description = "Generates a 6-digit OTP for the phone. In this MVP the code is printed to the server console (MSG91 not wired). OTP is single-use and expires in 10 minutes.")
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody AuthDto.SendOtpRequest req) {
        // In MVP, log OTP to console. In production, call MSG91.
        String otp = generateOtp();
        otpStore.put(req.phone(),
            new OtpEntry(otp, System.currentTimeMillis() + otpExpiryMs));
        // Sends via MSG91 if configured; otherwise logs the OTP for local dev.
        msg91Service.sendOtp(req.phone(), otp);
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @Operation(summary = "Verify OTP → JWT", description = "Validates the OTP, resolves the staff's clinic via the global directory (fallback: owner phone), and returns a JWT plus role/name/clinic. Use the token via the Authorize button.")
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody AuthDto.VerifyOtpRequest req) {
        OtpEntry stored = otpStore.get(req.phone());
        if (stored == null || !stored.otp().equals(req.otp())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP"));
        }
        if (System.currentTimeMillis() > stored.expiresAt()) {
            otpStore.remove(req.phone());
            return ResponseEntity.badRequest().body(Map.of("error", "OTP expired"));
        }
        otpStore.remove(req.phone());

        // Resolve which clinic this phone belongs to.
        // Prefer the global staff directory (works for ANY staff member); fall
        // back to owner-phone lookup for clinics registered before the directory
        // existed.
        String schemaName = directoryRepo.findByPhone(req.phone())
            .map(d -> d.getSchemaName())
            .orElseGet(() -> tenantRepo.findByOwnerPhone(req.phone())
                .map(Tenant::getSchemaName)
                .orElseThrow(() -> new RuntimeException("No clinic found for this phone")));

        Tenant tenant = tenantRepo.findBySchemaName(schemaName)
            .orElseThrow(() -> new RuntimeException("Clinic not found"));

        // Switch to tenant schema to find staff record
        TenantContext.set(tenant.getSchemaName());
        try {
            Staff staff = staffRepo.findByPhone(req.phone())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

            String token = jwtUtil.generate(
                staff.getId().toString(),
                staff.getRole(),
                tenant.getSchemaName()
            );

            return ResponseEntity.ok(new AuthDto.AuthResponse(
                token,
                staff.getRole(),
                staff.getName(),
                tenant.getClinicName()
            ));
        } finally {
            TenantContext.clear();
        }
    }

    private String generateOtp() {
        return String.valueOf(100000 + (int)(Math.random() * 900000));
    }

    /** OTP value plus its absolute expiry timestamp (epoch millis). */
    private record OtpEntry(String otp, long expiresAt) {}
}
