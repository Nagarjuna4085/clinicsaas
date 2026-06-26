-- Global V3 — record when the clinic owner accepted Terms/Privacy at signup.
ALTER TABLE global.tenants ADD COLUMN IF NOT EXISTS consent_at TIMESTAMPTZ;
