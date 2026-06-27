package com.clinicflow.config.crypto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FieldCryptoTest {

    @Test
    void roundTripWhenEnabled() {
        FieldCrypto.init("a-test-secret");
        String enc = FieldCrypto.encrypt("Penicillin allergy");
        assertThat(enc).startsWith("enc:");
        assertThat(enc).isNotEqualTo("Penicillin allergy");
        assertThat(FieldCrypto.decrypt(enc)).isEqualTo("Penicillin allergy");
    }

    @Test
    void differentCiphertextEachTime() {
        FieldCrypto.init("a-test-secret");
        assertThat(FieldCrypto.encrypt("same")).isNotEqualTo(FieldCrypto.encrypt("same"));
    }

    @Test
    void passthroughWhenDisabled() {
        FieldCrypto.init("");
        assertThat(FieldCrypto.enabled()).isFalse();
        assertThat(FieldCrypto.encrypt("x")).isEqualTo("x");
        assertThat(FieldCrypto.decrypt("x")).isEqualTo("x");
    }

    @Test
    void legacyPlaintextReadsAsIs() {
        FieldCrypto.init("a-test-secret");
        // A value never encrypted by us (no "enc:" prefix) is returned unchanged.
        assertThat(FieldCrypto.decrypt("legacy plaintext")).isEqualTo("legacy plaintext");
    }

    @Test
    void nullSafe() {
        FieldCrypto.init("a-test-secret");
        assertThat(FieldCrypto.encrypt(null)).isNull();
        assertThat(FieldCrypto.decrypt(null)).isNull();
    }
}
