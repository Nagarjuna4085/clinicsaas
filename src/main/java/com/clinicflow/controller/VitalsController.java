package com.clinicflow.controller;

import com.clinicflow.dto.VitalsDto;
import com.clinicflow.service.VitalsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Tag(name = "Vitals", description = "Record and read patient vitals per visit")
@RestController
@RequestMapping("/api/vitals")
public class VitalsController {

    private final VitalsService vitalsService;

    public VitalsController(VitalsService vitalsService) {
        this.vitalsService = vitalsService;
    }

    @Operation(summary = "Record vitals", description = "Create or update vitals for an appointment. Records the current staff as recordedBy. Roles: NURSE, DOCTOR, ADMIN.")
    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE','DOCTOR','ADMIN')")
    public ResponseEntity<VitalsDto.Response> record(@Valid @RequestBody VitalsDto.CreateRequest req) {
        return ResponseEntity.ok(vitalsService.record(req));
    }

    @Operation(summary = "Get vitals for appointment", description = "Returns the vitals for an appointment, or empty if none recorded. Roles: NURSE, DOCTOR, ADMIN, RECEPTIONIST.")
    @GetMapping("/by-appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('NURSE','DOCTOR','ADMIN','RECEPTIONIST')")
    public ResponseEntity<VitalsDto.Response> get(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(vitalsService.get(appointmentId));
    }
}
