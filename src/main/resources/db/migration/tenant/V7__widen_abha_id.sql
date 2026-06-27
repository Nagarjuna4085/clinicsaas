-- Tenant V7 — abha_id is now encrypted at rest, which is longer than the
-- original 20 chars. Widen the column to hold the ciphertext.
ALTER TABLE patients ALTER COLUMN abha_id TYPE TEXT;
