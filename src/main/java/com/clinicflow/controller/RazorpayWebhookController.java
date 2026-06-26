package com.clinicflow.controller;

import com.clinicflow.service.RazorpayService;
import com.clinicflow.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public Razorpay webhook receiver. Verifies the signature before acting.
 * Lives under /api/public/** (permitted without a JWT).
 */
@Tag(name = "Webhooks", description = "Razorpay payment webhooks")
@SecurityRequirements
@RestController
@RequestMapping("/api/public/webhooks")
public class RazorpayWebhookController {

    private final RazorpayService razorpayService;
    private final SubscriptionService subscriptionService;

    public RazorpayWebhookController(RazorpayService razorpayService,
                                     SubscriptionService subscriptionService) {
        this.razorpayService = razorpayService;
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "Razorpay webhook", description = "Receives subscription events; updates clinic status after verifying the signature.")
    @PostMapping("/razorpay")
    public ResponseEntity<String> razorpay(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        if (!razorpayService.verifyWebhookSignature(payload, signature)) {
            return ResponseEntity.badRequest().body("invalid signature");
        }
        subscriptionService.handleWebhook(payload);
        return ResponseEntity.ok("ok");
    }
}
