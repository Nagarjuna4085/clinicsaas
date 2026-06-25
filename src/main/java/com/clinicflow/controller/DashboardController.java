package com.clinicflow.controller;

import com.clinicflow.dto.BillDto;
import com.clinicflow.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Aggregated daily metrics for the clinic")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Today's summary", description = "Revenue (with cash/UPI/card split), bill count, patients seen today, and waiting count. Roles: all staff.")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','NURSE')")
    public ResponseEntity<BillDto.DashboardSummary> summary() {
        return ResponseEntity.ok(dashboardService.summary());
    }
}
