package com.clinicflow.controller;

import com.clinicflow.dto.PatientDto;
import com.clinicflow.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // Register new patient or fetch existing
    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR')")
    public ResponseEntity<PatientDto.Response> register(
            @Valid @RequestBody PatientDto.RegisterRequest req) {
        return ResponseEntity.ok(patientService.registerOrUpdate(req));
    }

    // Search by name or phone (receptionist search bar)
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    public ResponseEntity<List<PatientDto.Response>> search(
            @RequestParam String q) {
        return ResponseEntity.ok(patientService.search(q));
    }

    // Lookup by phone (auto-detect returning patient)
    @GetMapping("/by-phone/{phone}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR')")
    public ResponseEntity<?> byPhone(@PathVariable String phone) {
        return patientService.findByPhone(phone)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // Get full patient details
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    public ResponseEntity<PatientDto.Response> get(@PathVariable UUID id) {
        return ResponseEntity.ok(patientService.getById(id));
    }
}
