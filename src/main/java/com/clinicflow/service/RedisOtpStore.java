package com.clinicflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

/**
 * Redis-backed OTP store — survives restarts and works across instances.
 * Enable with app.otp.store=redis and configure spring.data.redis.*.
 */
@Service
@ConditionalOnProperty(name = "app.otp.store", havingValue = "redis")
public class RedisOtpStore implements OtpStore {

    private final StringRedisTemplate redis;
    private final long ttlMinutes;

    public RedisOtpStore(StringRedisTemplate redis,
                         @Value("${app.otp.expiry-minutes:10}") long ttlMinutes) {
        this.redis = redis;
        this.ttlMinutes = ttlMinutes;
    }

    private String key(String phone) {
        return "otp:" + phone;
    }

    @Override
    public void save(String phone, String otp) {
        redis.opsForValue().set(key(phone), otp, Duration.ofMinutes(ttlMinutes));
    }

    @Override
    public String get(String phone) {
        return redis.opsForValue().get(key(phone));
    }

    @Override
    public void remove(String phone) {
        redis.delete(key(phone));
    }
}
