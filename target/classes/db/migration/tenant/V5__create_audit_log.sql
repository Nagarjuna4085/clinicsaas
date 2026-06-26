-- Tenant V5 — audit trail of who viewed/changed records (DPDP accountability).
CREATE TABLE IF NOT EXISTS audit_log (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    actor       VARCHAR(80),        -- staff id (or 'system')
    actor_role  VARCHAR(20),
    action      VARCHAR(20) NOT NULL, -- VIEW / CREATE / UPDATE / DELETE / EXPORT
    entity_type VARCHAR(30),
    entity_id   VARCHAR(40),
    details     TEXT,
    logged_at   TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_audit_logged_at ON audit_log (logged_at);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_log (entity_type, entity_id);
