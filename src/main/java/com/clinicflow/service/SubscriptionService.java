package com.clinicflow.service;

import com.clinicflow.context.TenantContext;
import com.clinicflow.dto.SubscriptionDto;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.exception.NotFoundException;
import com.clinicflow.repository.global.TenantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
    private static final Set<String> VALID_STATUSES = Set.of("trial", "active", "suspended");

    private final TenantRepository tenantRepo;
    private final RazorpayService razorpay;
    private final boolean devTools;
    private final ObjectMapper mapper = new ObjectMapper();

    public SubscriptionService(TenantRepository tenantRepo, RazorpayService razorpay,
                               @Value("${app.dev-tools.enabled:false}") boolean devTools) {
        this.tenantRepo = tenantRepo;
        this.razorpay = razorpay;
        this.devTools = devTools;
    }

    /** DEV ONLY: directly set the clinic's status to simulate webhook outcomes. */
    @Transactional
    public SubscriptionDto.Status devSetStatus(String status) {
        if (!devTools) {
            throw new IllegalStateException("Dev tools are disabled");
        }
        String s = status == null ? "" : status.toLowerCase();
        if (!VALID_STATUSES.contains(s)) {
            throw new IllegalArgumentException("Status must be one of " + VALID_STATUSES);
        }
        Tenant t = current();
        t.setStatus(s);
        tenantRepo.save(t);
        log.info("[dev-tools] clinic {} status set to {}", t.getSchemaName(), s);
        return toStatus(t);
    }

    @Transactional
    public SubscriptionDto.SubscribeResponse subscribe(String plan) {
        Tenant t = current();
        String planId = razorpay.planId(plan);
        Map<String, Object> sub = razorpay.createSubscription(planId);

        t.setRazorpaySubId((String) sub.get("id"));
        t.setPlan(plan.toLowerCase());
        tenantRepo.save(t);

        return new SubscriptionDto.SubscribeResponse(
            (String) sub.get("id"),
            (String) sub.get("short_url"),
            t.getStatus(),
            "Complete payment at the checkout link to activate your subscription.");
    }

    @Transactional(readOnly = true)
    public SubscriptionDto.Status status() {
        return toStatus(current());
    }

    private SubscriptionDto.Status toStatus(Tenant t) {
        return new SubscriptionDto.Status(
            t.getPlan(), t.getStatus(),
            t.getTrialEndsAt() != null ? t.getTrialEndsAt().toString() : null,
            t.getRazorpaySubId(), devTools);
    }

    /** Processes a verified Razorpay webhook and flips the clinic's status. */
    @Transactional
    public void handleWebhook(String rawBody) {
        try {
            JsonNode root = mapper.readTree(rawBody);
            String event = root.path("event").asText("");
            String subId = root.path("payload").path("subscription").path("entity").path("id").asText(null);
            if (subId == null) return;

            tenantRepo.findByRazorpaySubId(subId).ifPresent(t -> {
                switch (event) {
                    case "subscription.activated", "subscription.charged", "subscription.resumed" ->
                        t.setStatus("active");
                    case "subscription.halted", "subscription.cancelled", "subscription.completed",
                         "subscription.paused" ->
                        t.setStatus("suspended");
                    default -> { return; }
                }
                tenantRepo.save(t);
                log.info("Razorpay {} → clinic {} status {}", event, t.getSchemaName(), t.getStatus());
            });
        } catch (Exception e) {
            log.warn("Failed to process Razorpay webhook: {}", e.getMessage());
        }
    }

    /** Daily: suspend clinics whose trial has ended without an active subscription. */
    @Transactional
    public int expireTrials() {
        List<Tenant> expired = tenantRepo.findByStatusAndTrialEndsAtBefore("trial", OffsetDateTime.now());
        expired.forEach(t -> t.setStatus("suspended"));
        tenantRepo.saveAll(expired);
        if (!expired.isEmpty()) log.info("Suspended {} expired-trial clinics", expired.size());
        return expired.size();
    }

    private Tenant current() {
        String schema = TenantContext.get();
        return tenantRepo.findBySchemaName(schema)
            .orElseThrow(() -> new NotFoundException("Clinic not found"));
    }
}
