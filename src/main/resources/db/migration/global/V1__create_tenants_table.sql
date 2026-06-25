-- Global schema migration — runs ONCE at startup
CREATE TABLE IF NOT EXISTS global.tenants (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    schema_name       VARCHAR(60) NOT NULL UNIQUE,
    clinic_name       VARCHAR(120) NOT NULL,
    owner_phone       VARCHAR(15) NOT NULL,
    city              VARCHAR(60),
    plan              VARCHAR(20) NOT NULL DEFAULT 'starter',
    status            VARCHAR(20) NOT NULL DEFAULT 'trial',
    trial_ends_at     TIMESTAMPTZ DEFAULT NOW() + INTERVAL '90 days',
    razorpay_sub_id   VARCHAR(60),
    created_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tenants_phone  ON global.tenants (owner_phone);
CREATE INDEX IF NOT EXISTS idx_tenants_schema ON global.tenants (schema_name);
CREATE INDEX IF NOT EXISTS idx_tenants_status ON global.tenants (status);
