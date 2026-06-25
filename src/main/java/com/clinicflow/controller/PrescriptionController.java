package com.clinicflow.controller;

import com.clinicflow.dto.PrescriptionDto;
import com.clinicflow.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Tag(name = "Prescriptions", description = "Consultation notes, medicines and follow-up per visit")
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @Operation(summary = "Save prescription + consultation", description = "Saves diagnosis/advice, the medicine list, and the follow-up date for an appointment. Re-posting updates the existing record. Roles: DOCTOR, ADMIN.")
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<PrescriptionDto.Response> create(@Valid @RequestBody PrescriptionDto.CreateRequest req) {
        return ResponseEntity.ok(prescriptionService.create(req));
    }

    @Operation(summary = "Get prescription for appointment", description = "Returns the consultation + prescription for an appointment, or empty if none. Roles: DOCTOR, ADMIN, NURSE, RECEPTIONIST.")
    @GetMapping("/by-appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN','NURSE','RECEPTIONIST')")
    public ResponseEntity<PrescriptionDto.Response> get(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(prescriptionService.get(appointmentId));
    }

    @Operation(summary = "Download prescription PDF", description = "Generates a printable prescription PDF. Roles: DOCTOR, ADMIN, NURSE, RECEPTIONIST.")
    @GetMapping("/by-appointment/{appointmentId}/pdf")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN','NURSE','RECEPTIONIST')")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID appointmentId) {
        byte[] pdf = prescriptionService.pdf(appointmentId);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"prescription-" + appointmentId + ".pdf\"")
            .body(pdf);
    }

    @Operation(summary = "Send prescription via WhatsApp", description = "Notifies the patient on WhatsApp that their prescription is ready (no-op if Gupshup isn't configured) and marks it sent. Roles: DOCTOR, ADMIN.")
    @PostMapping("/by-appointment/{appointmentId}/send-whatsapp")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<PrescriptionDto.Response> sendWhatsapp(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(prescriptionService.sendWhatsapp(appointmentId));
    }
}
