-- Global migration V2 — maps a staff phone to the clinic (schema) they belong
-- to, so OTP login works for ANY staff member, not just the clinic owner.
-- A phone is globally unique: one person logs into exactly one clinic.
CREATE TABLE IF NOT EXISTS global.staff_directory (
    phone        VARCHAR(15)  PRIMARY KEY,
    schema_name  VARCHAR(60)  NOT NULL,
    created_at   TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_staff_dir_schema ON global.staff_directory (schema_name);
