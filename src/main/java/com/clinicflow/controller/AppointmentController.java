package com.clinicflow.controller;

import com.clinicflow.dto.AppointmentDto;
import com.clinicflow.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Tag(name = "Appointments", description = "Daily token queue: book visits, view the queue, update status")
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Operation(summary = "Book an appointment + issue token",
        description = "Registers a patient into today's queue, assigns the next token number for the doctor, and auto-creates the OP-fee bill. Roles: RECEPTIONIST, ADMIN.")
    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<AppointmentDto.QueueItem> create(
            @Valid @RequestBody AppointmentDto.CreateRequest req) {
        return ResponseEntity.ok(appointmentService.createAppointment(req));
    }

    @Operation(summary = "Today's full queue", description = "All of today's appointments ordered by token. Roles: RECEPTIONIST, ADMIN, DOCTOR, NURSE.")
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    public ResponseEntity<List<AppointmentDto.QueueItem>> today() {
        return ResponseEntity.ok(appointmentService.getAllTodaysAppointments());
    }

    @Operation(summary = "Doctor's personal queue", description = "Today's appointments for one doctor, ordered by token. A DOCTOR may only request their own queue; ADMIN/RECEPTIONIST may request any doctor's.")
    @GetMapping("/today/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','RECEPTIONIST','ADMIN')")
    public ResponseEntity<List<AppointmentDto.QueueItem>> doctorQueue(
            @PathVariable UUID doctorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean privileged = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_RECEPTIONIST"));
        // A plain doctor can only see their own queue (id is the JWT subject).
        if (!privileged && !doctorId.toString().equals(auth.getName())) {
            throw new AccessDeniedException("You can only view your own queue");
        }
        return ResponseEntity.ok(appointmentService.getTodaysQueue(doctorId));
    }

    @Operation(summary = "Update appointment status", description = "Move an appointment to CONSULTING / COMPLETED / CANCELLED. Roles: DOCTOR, RECEPTIONIST, ADMIN.")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR','RECEPTIONIST','ADMIN')")
    public ResponseEntity<AppointmentDto.QueueItem> updateStatus(
            @PathVariable UUID id,
            @RequestBody AppointmentDto.StatusUpdate req) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, req.status()));
    }
}
