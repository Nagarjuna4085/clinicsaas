package com.clinicflow.controller;

import com.clinicflow.dto.StaffDto;
import com.clinicflow.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * Staff management within a clinic. All routes run against the caller's clinic
 * schema (resolved from their JWT), so an ADMIN only ever sees/creates staff in
 * their own clinic.
 */
@Tag(name = "Staff", description = "Manage clinic staff (doctors, receptionists, nurses). Scoped to the caller's clinic.")
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @Operation(summary = "Add a staff member", description = "Creates a staff record in the caller's clinic schema and registers their phone in the global directory so they can OTP-login. Role: ADMIN only.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffDto.Response> create(
            @Valid @RequestBody StaffDto.CreateRequest req) {
        return ResponseEntity.ok(staffService.create(req));
    }

    @Operation(summary = "List active staff", description = "Active staff in the caller's clinic — handy for picking a doctor when booking. Roles: ADMIN, RECEPTIONIST, DOCTOR, NURSE.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','NURSE')")
    public ResponseEntity<List<StaffDto.Response>> list() {
        return ResponseEntity.ok(staffService.listActive());
    }

    @Operation(summary = "Update a staff member", description = "Update name, role, registration and specialty (phone is fixed). Role: ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffDto.Response> update(@PathVariable UUID id,
                                                    @Valid @RequestBody StaffDto.UpdateRequest req) {
        return ResponseEntity.ok(staffService.update(id, req));
    }

    @Operation(summary = "Deactivate a staff member", description = "Soft-deactivates the staff member and revokes their login. Role: ADMIN.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        staffService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
