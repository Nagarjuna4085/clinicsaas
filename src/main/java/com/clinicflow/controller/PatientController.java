package com.clinicflow.controller;

import com.clinicflow.dto.PatientDto;
import com.clinicflow.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Tag(name = "Patients", description = "Patient registry: register/find, search, lookup by phone")
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Operation(summary = "Register or update a patient", description = "Creates a new patient (auto-generates UHID) or updates an existing one matched by phone. Roles: RECEPTIONIST, ADMIN, DOCTOR.")
    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR')")
    public ResponseEntity<PatientDto.Response> register(
            @Valid @RequestBody PatientDto.RegisterRequest req) {
        return ResponseEntity.ok(patientService.registerOrUpdate(req));
    }

    @Operation(summary = "Search patients", description = "Search by name or phone (receptionist search bar). Query param `q`. Roles: RECEPTIONIST, ADMIN, DOCTOR, NURSE.")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    public ResponseEntity<List<PatientDto.Response>> search(
            @RequestParam String q) {
        return ResponseEntity.ok(patientService.search(q));
    }

    @Operation(summary = "Lookup patient by phone", description = "Auto-detect a returning patient by phone number; 404 if none. Roles: RECEPTIONIST, ADMIN, DOCTOR.")
    @GetMapping("/by-phone/{phone}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR')")
    public ResponseEntity<?> byPhone(@PathVariable String phone) {
        return patientService.findByPhone(phone)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get patient by id", description = "Full patient details including total visit count. Roles: RECEPTIONIST, ADMIN, DOCTOR, NURSE.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    public ResponseEntity<PatientDto.Response> get(@PathVariable UUID id) {
        return ResponseEntity.ok(patientService.getById(id));
    }
}
