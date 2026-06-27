package com.clinicflow.controller;

import com.clinicflow.dto.ReportDto;
import com.clinicflow.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reports", description = "Time-series analytics (ADMIN)")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Revenue & visits over time", description = "Daily revenue and visit counts for the last N days (default 30, max 365). Role: ADMIN.")
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportDto.RevenueReport> revenue(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportService.revenue(days));
    }
}
