package com.clinicflow.config.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM helper for at-rest field encryption.
 *
 * - The key is derived (SHA-256) from a secret string, so any random secret works.
 * - Encryption is enabled only when a secret is configured; otherwise values pass
 *   through as plaintext (so local dev / existing data keep working).
 * - Ciphertext is tagged with an "enc:" prefix. On read, untagged values are
 *   returned as-is — this makes adoption backward-compatible: existing plaintext
 *   rows still read fine, and new writes get encrypted.
 */
public final class FieldCrypto {

    private static final String PREFIX = "enc:";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static volatile SecretKeySpec key; // null = disabled

    private FieldCrypto() {}

    public static void init(String secret) {
        if (secret == null || secret.isBlank()) {
            key = null;
            return;
        }
        try {
            byte[] k = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            key = new SecretKeySpec(k, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialise field encryption", e);
        }
    }

    public static boolean enabled() {
        return key != null;
    }

    public static String encrypt(String plain) {
        if (key == null || plain == null) return plain;
        try {
            byte[] iv = new byte[IV_LEN];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return PREFIX + Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public static String decrypt(String stored) {
        if (stored == null || !stored.startsWith(PREFIX)) return stored; // legacy/plaintext
        if (key == null) return stored; // key not configured — can't decrypt, return as-is
        try {
            byte[] all = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            byte[] iv = new byte[IV_LEN];
            byte[] ct = new byte[all.length - IV_LEN];
            System.arraycopy(all, 0, iv, 0, IV_LEN);
            System.arraycopy(all, IV_LEN, ct, 0, ct.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}
