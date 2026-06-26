-- Tenant migration V3 — password-based staff login.
-- password_hash: BCrypt hash; must_reset_password: force a reset on first login.
ALTER TABLE staff ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS must_reset_password BOOLEAN DEFAULT FALSE;
