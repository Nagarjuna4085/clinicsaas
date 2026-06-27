-- Tenant V8 — scheduled (future) appointments with a specific date/time.
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS scheduled_at TIMESTAMPTZ;
