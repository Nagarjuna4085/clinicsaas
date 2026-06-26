package com.clinicflow.service;

import com.clinicflow.config.RazorpayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Talks to the Razorpay Subscriptions API and verifies webhook signatures.
 * Config-gated: throws a clear error if credentials aren't set.
 */
@Service
public class RazorpayService {

    private static final Logger log = LoggerFactory.getLogger(RazorpayService.class);

    private final RazorpayProperties props;
    private final RestClient http = RestClient.create();

    public RazorpayService(RazorpayProperties props) {
        this.props = props;
    }

    /** Creates a subscription for the given Razorpay plan id; returns id + short_url. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createSubscription(String planId) {
        if (!props.isConfigured()) {
            throw new IllegalStateException("Billing is not configured (set RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET)");
        }
        if (planId == null || planId.isBlank()) {
            throw new IllegalArgumentException("No Razorpay plan id configured for this plan");
        }
        return http.post()
            .uri("https://api.razorpay.com/v1/subscriptions")
            .headers(h -> h.setBasicAuth(props.getKeyId(), props.getKeySecret()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of(
                "plan_id", planId,
                "total_count", 12,        // months to bill
                "customer_notify", 1
            ))
            .retrieve()
            .body(Map.class);
    }

    /** Verifies the X-Razorpay-Signature header against the raw webhook body. */
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        String secret = props.getWebhookSecret();
        if (secret == null || secret.isBlank() || signature == null) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return constantTimeEquals(hex.toString(), signature);
        } catch (Exception e) {
            log.warn("Webhook signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }

    public String planId(String planName) {
        return props.getPlanIds().get(planName == null ? "" : planName.toLowerCase());
    }
}
