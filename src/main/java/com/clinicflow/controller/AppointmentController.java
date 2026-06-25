package com.clinicflow.controller;

import com.clinicflow.dto.AppointmentDto;
import com.clinicflow.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Create appointment + issue token
    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<AppointmentDto.QueueItem> create(
            @Valid @RequestBody AppointmentDto.CreateRequest req) {
        return ResponseEntity.ok(appointmentService.createAppointment(req));
    }

    // Today's full queue (receptionist view)
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    public ResponseEntity<List<AppointmentDto.QueueItem>> today() {
        return ResponseEntity.ok(appointmentService.getAllTodaysAppointments());
    }

    // Doctor's personal queue
    @GetMapping("/today/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','RECEPTIONIST','ADMIN')")
    public ResponseEntity<List<AppointmentDto.QueueItem>> doctorQueue(
            @PathVariable UUID doctorId) {
        return ResponseEntity.ok(appointmentService.getTodaysQueue(doctorId));
    }

    // Update appointment status (CONSULTING / COMPLETED / CANCELLED)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR','RECEPTIONIST','ADMIN')")
    public ResponseEntity<AppointmentDto.QueueItem> updateStatus(
            @PathVariable UUID id,
            @RequestBody AppointmentDto.StatusUpdate req) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, req.status()));
    }
}
