package com.clinicflow.service;

import com.clinicflow.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight fixed-window rate limiter (in-memory).
 *
 * Good enough to stop OTP/SMS abuse and brute force on a single instance.
 * For a multi-instance deployment this should be backed by Redis so the window
 * is shared across nodes.
 */
@Component
public class RateLimiter {

    // key -> [windowStartMillis, count]
    private final Map<String, long[]> windows = new ConcurrentHashMap<>();

    /** Throws RateLimitExceededException if more than {@code max} hits occur within {@code window}. */
    public void check(String key, int max, Duration window) {
        long now = System.currentTimeMillis();
        long win = window.toMillis();
        long[] state = windows.compute(key, (k, v) -> {
            if (v == null || now - v[0] >= win) {
                return new long[]{now, 1};
            }
            v[1]++;
            return v;
        });
        if (state[1] > max) {
            throw new RateLimitExceededException("Too many attempts. Please try again later.");
        }
    }
}
