package com.clinicflow.service;

import com.clinicflow.config.RazorpayProperties;
import org.junit.jupiter.api.Test;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;

class RazorpayServiceTest {

    private static String hmac(String secret, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] d = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : d) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    private RazorpayService withSecret(String secret) {
        RazorpayProperties props = new RazorpayProperties();
        props.setWebhookSecret(secret);
        return new RazorpayService(props);
    }

    @Test
    void acceptsValidSignature() throws Exception {
        String body = "{\"event\":\"subscription.activated\"}";
        RazorpayService svc = withSecret("whsec_test");
        assertThat(svc.verifyWebhookSignature(body, hmac("whsec_test", body))).isTrue();
    }

    @Test
    void rejectsTamperedSignature() {
        String body = "{\"event\":\"subscription.charged\"}";
        RazorpayService svc = withSecret("whsec_test");
        assertThat(svc.verifyWebhookSignature(body, "deadbeef")).isFalse();
        assertThat(svc.verifyWebhookSignature(body, null)).isFalse();
    }

    @Test
    void rejectsWhenSecretNotConfigured() {
        RazorpayService svc = withSecret("");
        assertThat(svc.verifyWebhookSignature("{}", "anything")).isFalse();
    }
}
