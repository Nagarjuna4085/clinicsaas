-- Tenant schema migration V2 — concurrency-safe id generation + guards.
-- Runs per clinic schema. Sequences live inside each tenant schema, so they
-- are isolated per clinic and resolved via the request's search_path.

-- Atomic UHID generator (replaces COUNT(*) in PatientService)
CREATE SEQUENCE IF NOT EXISTS patient_uhid_seq START 1;

-- Atomic invoice-number generator (replaces COUNT(*) in AppointmentService)
CREATE SEQUENCE IF NOT EXISTS bill_invoice_seq START 1;

-- Keep existing schemas consistent if they already hold data: set each
-- sequence so the next value is (row count + 1) and we never reissue an
-- existing id. The `false` flag means the next nextval() returns this value.
SELECT setval('patient_uhid_seq', (SELECT COUNT(*) FROM patients) + 1, false);
SELECT setval('bill_invoice_seq', (SELECT COUNT(*) FROM bills) + 1, false);

-- Prevent two patients from silently getting the same daily token for the
-- same doctor. NULLs are treated as distinct, so untokened rows are unaffected.
CREATE UNIQUE INDEX IF NOT EXISTS uq_appt_token
    ON appointments (visit_date, doctor_id, token_number);
