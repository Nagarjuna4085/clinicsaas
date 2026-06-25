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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication.
 *
 * Day-to-day login is by phone + password (JWT session). OTP is used only for
 * two rare events: clinic signup verification and forgot-password — keeping SMS
 * cost minimal.
 */
@Tag(name = "Auth", description = "Phone + password login; OTP only for signup & password reset. Public.")
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Duration RATE_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_SENDS = 5;
    private static final int MAX_LOGINS = 10;

    private final TenantRepository tenantRepo;
    private final StaffDirectoryRepository directoryRepo;
    private final StaffRepository staffRepo;
    private final JwtUtil jwtUtil;
    private final Msg91Service msg91Service;
    private final OtpStore otpStore;
    private final RateLimiter rateLimiter;
    private final PasswordEncoder passwordEncoder;
    private final boolean devTools;

    public AuthController(TenantRepository tenantRepo,
                          StaffDirectoryRepository directoryRepo,
                          StaffRepository staffRepo,
                          JwtUtil jwtUtil,
                          Msg91Service msg91Service,
                          OtpStore otpStore,
                          RateLimiter rateLimiter,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.dev-tools.enabled:false}") boolean devTools) {
        this.tenantRepo = tenantRepo;
        this.directoryRepo = directoryRepo;
        this.staffRepo = staffRepo;
        this.jwtUtil = jwtUtil;
        this.msg91Service = msg91Service;
        this.otpStore = otpStore;
        this.rateLimiter = rateLimiter;
        this.passwordEncoder = passwordEncoder;
        this.devTools = devTools;
    }

    @Operation(summary = "Send OTP", description = "Sends a one-time code (for signup verification or password reset). MSG91 when configured, otherwise logged. Rate-limited per phone.")
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody AuthDto.SendOtpRequest req) {
        rateLimiter.check("otp-send:" + req.phone(), MAX_SENDS, RATE_WINDOW);
        String otp = generateOtp();
        otpStore.save(req.phone(), otp);
        msg91Service.sendOtp(req.phone(), otp);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "OTP sent");
        if (devTools) body.put("devOtp", otp); // dev convenience
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Login", description = "Phone + password → JWT. If the staff must reset their password (first login), the token is null and mustReset=true.")
    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        rateLimiter.check("login:" + req.phone(), MAX_LOGINS, RATE_WINDOW);
        Tenant tenant = resolveTenant(req.phone());
        TenantContext.set(tenant.getSchemaName());
        try {
            Staff staff = activeStaff(req.phone());
            if (staff.getPasswordHash() == null
                    || !passwordEncoder.matches(req.password(), staff.getPasswordHash())) {
                throw new BadRequestException("Invalid phone or password");
            }
            if (staff.isMustResetPassword()) {
                return ResponseEntity.ok(new AuthDto.LoginResponse(
                    null, staff.getRole(), staff.getName(), tenant.getClinicName(), true));
            }
            String token = jwtUtil.generate(staff.getId().toString(), staff.getRole(), tenant.getSchemaName());
            return ResponseEntity.ok(new AuthDto.LoginResponse(
                token, staff.getRole(), staff.getName(), tenant.getClinicName(), false));
        } finally {
            TenantContext.clear();
        }
    }

    @Operation(summary = "Set password (first login)", description = "Verifies the temporary password and sets a new one, returning a JWT.")
    @PostMapping("/set-password")
    public ResponseEntity<AuthDto.AuthResponse> setPassword(@Valid @RequestBody AuthDto.SetPasswordRequest req) {
        Tenant tenant = resolveTenant(req.phone());
        TenantContext.set(tenant.getSchemaName());
        try {
            Staff staff = activeStaff(req.phone());
            if (staff.getPasswordHash() == null
                    || !passwordEncoder.matches(req.currentPassword(), staff.getPasswordHash())) {
                throw new BadRequestException("Current password is incorrect");
            }
            applyNewPassword(staff, req.newPassword());
            return ResponseEntity.ok(authResponse(staff, tenant));
        } finally {
            TenantContext.clear();
        }
    }

    @Operation(summary = "Reset password (forgot)", description = "Verifies an OTP sent to the phone and sets a new password, returning a JWT.")
    @PostMapping("/reset-password")
    public ResponseEntity<AuthDto.AuthResponse> resetPassword(@Valid @RequestBody AuthDto.ResetPasswordRequest req) {
        rateLimiter.check("reset:" + req.phone(), MAX_LOGINS, RATE_WINDOW);
        String otp = otpStore.get(req.phone());
        if (otp == null || !otp.equals(req.otp())) {
            throw new BadRequestException("Invalid or expired OTP");
        }
        otpStore.remove(req.phone());

        Tenant tenant = resolveTenant(req.phone());
        TenantContext.set(tenant.getSchemaName());
        try {
            Staff staff = activeStaff(req.phone());
            applyNewPassword(staff, req.newPassword());
            return ResponseEntity.ok(authResponse(staff, tenant));
        } finally {
            TenantContext.clear();
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /** Resolve the clinic for a phone via the directory (fallback: owner phone). */
    private Tenant resolveTenant(String phone) {
        String schema = directoryRepo.findByPhone(phone)
            .map(d -> d.getSchemaName())
            .orElseGet(() -> tenantRepo.findByOwnerPhone(phone)
                .map(Tenant::getSchemaName)
                .orElseThrow(() -> new NotFoundException("No clinic found for this phone")));
        return tenantRepo.findBySchemaName(schema)
            .orElseThrow(() -> new NotFoundException("Clinic not found"));
    }

    /** Must be called with TenantContext set. */
    private Staff activeStaff(String phone) {
        Staff staff = staffRepo.findByPhone(phone)
            .orElseThrow(() -> new BadRequestException("Invalid phone or password"));
        if (!staff.isActive()) {
            throw new BadRequestException("This staff account has been deactivated");
        }
        return staff;
    }

    private void applyNewPassword(Staff staff, String newPassword) {
        staff.setPasswordHash(passwordEncoder.encode(newPassword));
        staff.setMustResetPassword(false);
        staffRepo.save(staff);
    }

    private AuthDto.AuthResponse authResponse(Staff staff, Tenant tenant) {
        String token = jwtUtil.generate(staff.getId().toString(), staff.getRole(), tenant.getSchemaName());
        return new AuthDto.AuthResponse(token, staff.getRole(), staff.getName(), tenant.getClinicName());
    }

    private String generateOtp() {
        return String.valueOf(100000 + (int) (Math.random() * 900000));
    }
}
