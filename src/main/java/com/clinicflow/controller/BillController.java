package com.clinicflow.controller;

import com.clinicflow.dto.BillDto;
import com.clinicflow.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Tag(name = "Billing", description = "Read GST invoices generated for appointments")
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @Operation(summary = "Today's bills", description = "All invoices billed today, newest first. Roles: ADMIN, RECEPTIONIST, DOCTOR.")
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<List<BillDto.Response>> today() {
        return ResponseEntity.ok(billService.today());
    }

    @Operation(summary = "Get bill by id", description = "Full invoice with line items. Roles: ADMIN, RECEPTIONIST, DOCTOR.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<BillDto.Response> get(@PathVariable UUID id) {
        return ResponseEntity.ok(billService.get(id));
    }

    @Operation(summary = "Download invoice PDF", description = "Generates a tax-invoice PDF for the bill. Roles: ADMIN, RECEPTIONIST, DOCTOR.")
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        byte[] pdf = billService.pdf(id);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"invoice-" + id + ".pdf\"")
            .body(pdf);
    }
}
