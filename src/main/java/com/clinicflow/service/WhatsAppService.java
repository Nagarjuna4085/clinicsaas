package com.clinicflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Sends WhatsApp messages via Gupshup. Config-gated: with no API key it logs the
 * message instead of calling the API, so the app works without a Gupshup account.
 *
 * Configure with env vars GUPSHUP_API_KEY, GUPSHUP_APP_NAME, GUPSHUP_SOURCE.
 * Note: production WhatsApp requires pre-approved message templates — the exact
 * payload may need tuning to your Gupshup app.
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final String apiKey;
    private final String appName;
    private final String source;
    private final RestClient http = RestClient.create();

    public WhatsAppService(@Value("${app.gupshup.api-key:}") String apiKey,
                           @Value("${app.gupshup.app-name:ClinicFlow}") String appName,
                           @Value("${app.gupshup.source-number:}") String source) {
        this.apiKey = apiKey;
        this.appName = appName;
        this.source = source;
    }

    public boolean sendText(String phone, String message) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("[Gupshup not configured] WhatsApp to {}: {}", phone, message);
            return false;
        }
        try {
            String body = "channel=whatsapp"
                + "&source=" + source
                + "&destination=91" + phone
                + "&src.name=" + enc(appName)
                + "&message=" + enc(message);
            http.post().uri("https://api.gupshup.io/wa/api/v1/msg")
                .header("apikey", apiKey)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toBodilessEntity();
            log.info("WhatsApp dispatched to {}", phone);
            return true;
        } catch (Exception e) {
            log.warn("Gupshup WhatsApp send failed for {}: {}", phone, e.getMessage());
            return false;
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
