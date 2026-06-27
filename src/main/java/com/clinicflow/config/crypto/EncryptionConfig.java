package com.clinicflow.config.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Initialises field-level encryption at startup from app.encryption.key.
 * Runs before any entity is read/written in a request, so the JPA converter
 * always has the key ready.
 */
@Configuration
public class EncryptionConfig {

    private static final Logger log = LoggerFactory.getLogger(EncryptionConfig.class);

    public EncryptionConfig(@Value("${app.encryption.key:}") String key) {
        FieldCrypto.init(key);
        if (FieldCrypto.enabled()) {
            log.info("Field-level encryption is ENABLED.");
        } else {
            log.warn("Field-level encryption is DISABLED (set APP_ENCRYPTION_KEY to enable).");
        }
    }
}
