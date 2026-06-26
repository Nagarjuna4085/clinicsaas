package com.clinicflow.dto;

import jakarta.validation.constraints.NotBlank;

public class SubscriptionDto {

    public record SubscribeRequest(
        @NotBlank String plan   // starter / clinic / pro / hospital
    ) {}

    public record SubscribeResponse(
        String subscriptionId,
        String shortUrl,        // Razorpay hosted checkout URL
        String status,
        String message
    ) {}

    public record Status(
        String plan,
        String status,          // trial / active / suspended
        String trialEndsAt,
        String razorpaySubId,
        boolean devTools        // whether dev-only simulate controls are enabled
    ) {}
}
