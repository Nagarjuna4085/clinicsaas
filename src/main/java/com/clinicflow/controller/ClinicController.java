package com.clinicflow.controller;

import com.clinicflow.context.TenantContext;
import com.clinicflow.dto.ClinicDto;
import com.clinicflow.entity.global.StaffDirectory;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.entity.tenant.Staff;
import com.clinicflow.repository.global.StaffDirectoryRepository;
import com.clinicflow.repository.tenant.StaffRepository;
import com.clinicflow.service.TenantProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public self-service clinic onboarding.
 *
 * Lives under /api/public/** (permitted without a JWT in SecurityConfig).
 * This closes the gap where TenantProvisioningService existed but no endpoint
 * called it — so a brand-new clinic had no way to register and OTP login would
 * always fail with "No clinic found for this phone".
 */
@Tag(name = "Clinic Onboarding", description = "Public self-service clinic signup (provisions a tenant schema). No JWT required.")
@SecurityRequirements
@RestController
@RequestMapping("/api/public/clinics")
public class ClinicController {

    private final TenantProvisioningService provisioningService;
    private final StaffRepository staffRepo;
    private final StaffDirectoryRepository directoryRepo;

    public ClinicController(TenantProvisioningService provisioningService,
                            StaffRepository staffRepo,
                            StaffDirectoryRepository directoryRepo) {
        this.provisioningService = provisioningService;
        this.staffRepo = staffRepo;
        this.directoryRepo = directoryRepo;
    }

    @Operation(summary = "Register a new clinic", description = "Provisions a dedicated PostgreSQL schema, runs the tenant migrations, creates the owner staff record, and registers the owner phone in the global directory so they can OTP-login.")
    @PostMapping("/register")
    public ResponseEntity<ClinicDto.RegisterResponse> register(
            @Valid @RequestBody ClinicDto.RegisterRequest req) {

        // 1. Provision schema + run tenant migrations + save global.tenants row
        Tenant tenant = provisioningService.provisionNewClinic(
            req.clinicName(),
            req.ownerPhone(),
            req.city(),
            req.plan() != null ? req.plan() : "starter"
        );

        // 2. Create the owner's staff record inside the new schema so the
        //    subsequent OTP login (which looks up staff by phone) succeeds.
        //    TenantContext is set BEFORE the save so the save opens a new
        //    transaction bound to the new schema's search_path.
        String role = req.role() != null ? req.role().toUpperCase() : "DOCTOR";
        TenantContext.set(tenant.getSchemaName());
        try {
            Staff owner = Staff.builder()
                .name(req.ownerName())
                .phone(req.ownerPhone())
                .role(role)
                .regNumber(req.regNumber())
                .specialty(req.specialty())
                .isActive(true)
                .build();
            staffRepo.save(owner);

            // Register the owner's phone in the global directory so OTP login
            // resolves their clinic (same path every staff member uses).
            directoryRepo.save(StaffDirectory.builder()
                .phone(req.ownerPhone())
                .schemaName(tenant.getSchemaName())
                .build());
        } finally {
            TenantContext.clear();
        }

        return ResponseEntity.ok(new ClinicDto.RegisterResponse(
            tenant.getClinicName(),
            tenant.getSchemaName(),
            req.ownerName(),
            role,
            "Clinic registered. Log in via POST /api/auth/send-otp with the owner phone."
        ));
    }
}
