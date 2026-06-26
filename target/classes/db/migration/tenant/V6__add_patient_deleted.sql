-- Tenant V6 — soft-delete / right-to-erasure marker for patients.
ALTER TABLE patients ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
