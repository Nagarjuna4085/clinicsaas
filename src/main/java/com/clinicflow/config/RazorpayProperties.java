package com.clinicflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/** Razorpay credentials and the app-plan → Razorpay-plan-id mapping. */
@Component
@ConfigurationProperties(prefix = "app.razorpay")
@Getter
@Setter
public class RazorpayProperties {
    private String keyId = "";
    private String keySecret = "";
    private String webhookSecret = "";
    private Map<String, String> planIds = new HashMap<>();

    public boolean isConfigured() {
        return keyId != null && !keyId.isBlank()
            && keySecret != null && !keySecret.isBlank();
    }
}
