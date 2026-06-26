package com.clinicflow.service;

import com.clinicflow.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.assertj.core.api.Assertions.*;

class RateLimiterTest {

    @Test
    void allowsUpToTheLimitThenThrows() {
        RateLimiter limiter = new RateLimiter();
        Duration window = Duration.ofMinutes(1);

        // 3 allowed
        limiter.check("k", 3, window);
        limiter.check("k", 3, window);
        limiter.check("k", 3, window);

        // 4th over the limit
        assertThatThrownBy(() -> limiter.check("k", 3, window))
            .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void separateKeysAreIndependent() {
        RateLimiter limiter = new RateLimiter();
        Duration window = Duration.ofMinutes(1);
        limiter.check("a", 1, window);
        // different key still allowed
        assertThatCode(() -> limiter.check("b", 1, window)).doesNotThrowAnyException();
        // same key now over limit
        assertThatThrownBy(() -> limiter.check("a", 1, window))
            .isInstanceOf(RateLimitExceededException.class);
    }
}
