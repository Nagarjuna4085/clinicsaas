package com.clinicflow.controller;

import com.clinicflow.dto.SubscriptionDto;
import com.clinicflow.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Subscription", description = "Clinic subscription & billing status")
@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "Billing status", description = "Current plan, status and trial end for the clinic. Role: ADMIN.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionDto.Status> status() {
        return ResponseEntity.ok(subscriptionService.status());
    }

    @Operation(summary = "Subscribe to a plan", description = "Creates a Razorpay subscription and returns a checkout link. Role: ADMIN.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionDto.SubscribeResponse> subscribe(
            @Valid @RequestBody SubscriptionDto.SubscribeRequest req) {
        return ResponseEntity.ok(subscriptionService.subscribe(req.plan()));
    }

    @Operation(summary = "[DEV] Simulate a status change", description = "Dev-only: directly set status (trial/active/suspended) to simulate webhook outcomes without Razorpay. Disabled unless app.dev-tools.enabled=true. Role: ADMIN.")
    @PostMapping("/dev/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionDto.Status> devSetStatus(@RequestParam String status) {
        return ResponseEntity.ok(subscriptionService.devSetStatus(status));
    }
}
