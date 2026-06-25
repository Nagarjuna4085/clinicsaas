package com.clinicflow.controller;

import com.clinicflow.dto.AuthDto;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.entity.tenant.Staff;
import com.clinicflow.exception.BadRequestException;
import com.clinicflow.exception.NotFoundException;
import com.clinicflow.repository.global.StaffDirectoryRepository;
import com.clinicflow.repository.global.TenantRepository;
import com.clinicflow.repository.tenant.StaffRepository;
import com.clinicflow.security.JwtUtil;
import com.clinicflow.service.Msg91Service;
import com.clinicflow.service.OtpStore;
import com.clinicflow.service.RateLimiter;
import com.clinicflow.context.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.Map;

/**
 * Phone OTP authentication.
 * POST /api/auth/send-otp   → sends OTP (MSG91 when configured, else logged)
 * POST /api/auth/verify-otp → verifies OTP, returns JWT
 *
 * OTPs live in a pluggable {@link OtpStore} (in-memory by default, Redis in
 * prod) and both endpoints are rate-limited per phone.
 */
@Tag(name = "Auth", description = "Phone OTP login. Public — no JWT required.")
@SecurityRequirements // overrides the global bearerAuth: these endpoints are public
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Duration RATE_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_SENDS = 5;     // OTP requests per phone / window
    private static final int MAX_VERIFIES = 8;  // verify attempts per phone / window

    private final TenantRepository tenantRepo;
    private final StaffDirectoryRepository directoryRepo;
    private final StaffRepository staffRepo;
    private final JwtUtil jwtUtil;
    private final Msg91Service msg91Service;
    private final OtpStore otpStore;
    private final RateLimiter rateLimiter;

    public AuthController(TenantRepository tenantRepo,
                          StaffDirectoryRepository directoryRepo,
                          StaffRepository staffRepo,
                          JwtUtil jwtUtil,
                          Msg91Service msg91Service,
                          OtpStore otpStore,
                          RateLimiter rateLimiter) {
        this.tenantRepo = tenantRepo;
        this.directoryRepo = directoryRepo;
        this.staffRepo = staffRepo;
        this.jwtUtil = jwtUtil;
        this.msg91Service = msg91Service;
        this.otpStore = otpStore;
        this.rateLimiter = rateLimiter;
    }

    @Operation(summary = "Send OTP", description = "Generates a 6-digit OTP for the phone (sent via MSG91 when configured, otherwise logged). Single-use, expires in 10 minutes, rate-limited per phone.")
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody AuthDto.SendOtpRequest req) {
        rateLimiter.check("otp-send:" + req.phone(), MAX_SENDS, RATE_WINDOW);
        String otp = generateOtp();
        otpStore.save(req.phone(), otp);
        msg91Service.sendOtp(req.phone(), otp);
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @Operation(summary = "Verify OTP → JWT", description = "Validates the OTP, resolves the staff's clinic via the global directory (fallback: owner phone), and returns a JWT plus role/name/clinic.")
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody AuthDto.VerifyOtpRequest req) {
        rateLimiter.check("otp-verify:" + req.phone(), MAX_VERIFIES, RATE_WINDOW);

        String stored = otpStore.get(req.phone());
        if (stored == null || !stored.equals(req.otp())) {
            throw new BadRequestException("Invalid or expired OTP");
        }
        otpStore.remove(req.phone());

        // Resolve which clinic this phone belongs to (directory first, then owner phone).
        String schemaName = directoryRepo.findByPhone(req.phone())
            .map(d -> d.getSchemaName())
            .orElseGet(() -> tenantRepo.findByOwnerPhone(req.phone())
                .map(Tenant::getSchemaName)
                .orElseThrow(() -> new NotFoundException("No clinic found for this phone")));

        Tenant tenant = tenantRepo.findBySchemaName(schemaName)
            .orElseThrow(() -> new NotFoundException("Clinic not found"));

        // Switch to tenant schema to find staff record
        TenantContext.set(tenant.getSchemaName());
        try {
            Staff staff = staffRepo.findByPhone(req.phone())
                .orElseThrow(() -> new NotFoundException("Staff not found"));

            if (!staff.isActive()) {
                throw new BadRequestException("This staff account has been deactivated");
            }

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
        return String.valueOf(100000 + (int) (Math.random() * 900000));
    }
}
