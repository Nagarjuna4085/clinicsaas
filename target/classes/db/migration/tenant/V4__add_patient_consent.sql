-- Tenant V4 — record when a patient consented to storing their health data.
ALTER TABLE patients ADD COLUMN IF NOT EXISTS consent_at TIMESTAMPTZ;
