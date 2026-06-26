package com.clinicflow.controller;

import com.clinicflow.dto.ClinicDto;
import com.clinicflow.service.ClinicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Clinic", description = "View and update the current clinic's profile")
@RestController
@RequestMapping("/api/clinic")
public class ClinicProfileController {

    private final ClinicService clinicService;

    public ClinicProfileController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Operation(summary = "Get clinic profile", description = "Profile of the caller's clinic. Roles: all staff.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','NURSE')")
    public ResponseEntity<ClinicDto.Profile> profile() {
        return ResponseEntity.ok(clinicService.getProfile());
    }

    @Operation(summary = "Update clinic profile", description = "Update clinic name and city. Role: ADMIN.")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClinicDto.Profile> update(@Valid @RequestBody ClinicDto.UpdateRequest req) {
        return ResponseEntity.ok(clinicService.update(req));
    }
}
