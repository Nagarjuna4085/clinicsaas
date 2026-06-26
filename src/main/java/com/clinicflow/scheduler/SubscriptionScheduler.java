package com.clinicflow.scheduler;

import com.clinicflow.service.SubscriptionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Suspends clinics whose free trial has ended without an active subscription. */
@Component
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;

    public SubscriptionScheduler(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // Every day at 1:00 AM IST
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kolkata")
    public void expireTrials() {
        subscriptionService.expireTrials();
    }
}
