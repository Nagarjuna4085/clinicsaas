package com.clinicflow.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class InMemoryOtpStoreTest {

    @Test
    void savesAndRetrievesOtp() {
        InMemoryOtpStore store = new InMemoryOtpStore(10);
        store.save("9876543210", "123456");
        assertThat(store.get("9876543210")).isEqualTo("123456");
    }

    @Test
    void removeClearsOtp() {
        InMemoryOtpStore store = new InMemoryOtpStore(10);
        store.save("9876543210", "123456");
        store.remove("9876543210");
        assertThat(store.get("9876543210")).isNull();
    }

    @Test
    void unknownPhoneReturnsNull() {
        InMemoryOtpStore store = new InMemoryOtpStore(10);
        assertThat(store.get("0000000000")).isNull();
    }
}
