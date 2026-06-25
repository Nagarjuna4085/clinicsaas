package com.clinicflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Sends OTP SMS via MSG91. Config-gated: if no auth key is set, it logs the OTP
 * instead of calling the API, so local dev works without an MSG91 account.
 *
 * Configure with env vars MSG91_AUTH_KEY and MSG91_TEMPLATE_ID.
 */
@Service
public class Msg91Service {

    private static final Logger log = LoggerFactory.getLogger(Msg91Service.class);

    private final String authKey;
    private final String templateId;
    private final RestClient http = RestClient.create();

    public Msg91Service(@Value("${app.msg91.auth-key:}") String authKey,
                        @Value("${app.msg91.template-id:}") String templateId) {
        this.authKey = authKey;
        this.templateId = templateId;
    }

    public void sendOtp(String phone, String otp) {
        if (authKey == null || authKey.isBlank()) {
            // Fallback for local dev — read the OTP from the console/logs.
            log.info("[MSG91 not configured] OTP for {} is {}", phone, otp);
            return;
        }
        try {
            String url = "https://control.msg91.com/api/v5/otp"
                + "?template_id=" + templateId
                + "&mobile=91" + phone
                + "&otp=" + otp;
            http.post().uri(url)
                .header("authkey", authKey)
                .retrieve()
                .toBodilessEntity();
            log.info("OTP SMS dispatched to {}", phone);
        } catch (Exception e) {
            // Never fail login because SMS delivery hiccupped; OTP is still valid.
            log.warn("MSG91 OTP send failed for {} ({}); OTP is {}", phone, e.getMessage(), otp);
        }
    }
}
