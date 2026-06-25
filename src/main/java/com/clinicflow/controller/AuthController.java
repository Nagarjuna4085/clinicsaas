package com.clinicflow.controller;

import com.clinicflow.dto.AuthDto;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.entity.tenant.Staff;
import com.clinicflow.repository.global.TenantRepository;
import com.clinicflow.repository.tenant.StaffRepository;
import com.clinicflow.security.JwtUtil;
import com.clinicflow.context.TenantContext;
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
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // In-memory OTP store for MVP — replace with Redis in production
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private final TenantRepository tenantRepo;
    private final StaffRepository staffRepo;
    private final JwtUtil jwtUtil;
    private final long otpExpiryMs;

    public AuthController(TenantRepository tenantRepo,
                          StaffRepository staffRepo,
                          JwtUtil jwtUtil,
                          @Value("${app.otp.expiry-minutes:10}") long otpExpiryMinutes) {
        this.tenantRepo = tenantRepo;
        this.staffRepo = staffRepo;
        this.jwtUtil = jwtUtil;
        this.otpExpiryMs = otpExpiryMinutes * 60_000L;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody AuthDto.SendOtpRequest req) {
        // In MVP, log OTP to console. In production, call MSG91.
        String otp = generateOtp();
        otpStore.put(req.phone(),
            new OtpEntry(otp, System.currentTimeMillis() + otpExpiryMs));
        System.out.println("OTP for " + req.phone() + " : " + otp); // dev only!
        // TODO: call MSG91Service.send(req.phone(), otp)
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

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

        // Find which clinic this phone belongs to
        Tenant tenant = tenantRepo.findByOwnerPhone(req.phone())
            .orElseThrow(() -> new RuntimeException("No clinic found for this phone"));

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
