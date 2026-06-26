package com.clinicflow.service;

/**
 * Stores one-time passwords with a TTL. Two implementations:
 *  - InMemoryOtpStore (default; fine for a single instance / dev)
 *  - RedisOtpStore (enable with app.otp.store=redis for multi-instance prod)
 */
public interface OtpStore {
    void save(String phone, String otp);
    String get(String phone);   // null if absent or expired
    void remove(String phone);
}
