package com.clinicflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Default OTP store: in-memory with per-entry expiry. Lost on restart; single-instance only. */
@Service
@ConditionalOnProperty(name = "app.otp.store", havingValue = "memory", matchIfMissing = true)
public class InMemoryOtpStore implements OtpStore {

    private record Entry(String otp, long expiresAt) {}

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final long ttlMs;

    public InMemoryOtpStore(@Value("${app.otp.expiry-minutes:10}") long expiryMinutes) {
        this.ttlMs = expiryMinutes * 60_000L;
    }

    @Override
    public void save(String phone, String otp) {
        store.put(phone, new Entry(otp, System.currentTimeMillis() + ttlMs));
    }

    @Override
    public String get(String phone) {
        Entry e = store.get(phone);
        if (e == null) return null;
        if (System.currentTimeMillis() > e.expiresAt()) {
            store.remove(phone);
            return null;
        }
        return e.otp();
    }

    @Override
    public void remove(String phone) {
        store.remove(phone);
    }
}
