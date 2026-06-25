-- Tenant schema migration — runs for EACH new clinic that signs up

CREATE TABLE IF NOT EXISTS staff (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    phone       VARCHAR(15)  NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    reg_number  VARCHAR(40),
    specialty   VARCHAR(60),
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  TIMESTAMPTZ  DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_staff_phone ON staff (phone);

CREATE TABLE IF NOT EXISTS patients (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    uhid        VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    phone       VARCHAR(15),
    age         SMALLINT,
    dob         DATE,
    gender      VARCHAR(10),
    blood_group VARCHAR(5),
    address     TEXT,
    abha_id     VARCHAR(20),
    allergies   TEXT,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_patients_phone ON patients (phone);
CREATE INDEX IF NOT EXISTS idx_patients_uhid  ON patients (uhid);

CREATE TABLE IF NOT EXISTS appointments (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id    UUID        REFERENCES patients(id),
    doctor_id     UUID        REFERENCES staff(id),
    token_number  SMALLINT,
    visit_date    DATE        NOT NULL DEFAULT CURRENT_DATE,
    status        VARCHAR(20) DEFAULT 'WAITING',
    visit_type    VARCHAR(20) DEFAULT 'WALKIN',
    followup_date DATE,
    reminder_sent BOOLEAN     DEFAULT FALSE,
    notes         TEXT,
    created_at    TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_appt_date_doctor  ON appointments (visit_date, doctor_id);
CREATE INDEX IF NOT EXISTS idx_appt_patient      ON appointments (patient_id);
CREATE INDEX IF NOT EXISTS idx_appt_followup     ON appointments (followup_date) WHERE reminder_sent = FALSE;

CREATE TABLE IF NOT EXISTS vitals (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id  UUID        REFERENCES appointments(id),
    recorded_by     UUID        REFERENCES staff(id),
    bp_systolic     SMALLINT,
    bp_diastolic    SMALLINT,
    pulse           SMALLINT,
    temperature     DECIMAL(4,1),
    spo2            SMALLINT,
    weight_kg       DECIMAL(5,2),
    height_cm       SMALLINT,
    recorded_at     TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS consultations (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id  UUID        REFERENCES appointments(id),
    chief_complaint TEXT,
    diagnosis       TEXT,
    examination     TEXT,
    advice          TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS prescriptions (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id   UUID        REFERENCES appointments(id),
    pdf_url          TEXT,
    whatsapp_sent    BOOLEAN     DEFAULT FALSE,
    whatsapp_sent_at TIMESTAMPTZ,
    created_at       TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS prescription_items (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_id  UUID        REFERENCES prescriptions(id) ON DELETE CASCADE,
    medicine_name    VARCHAR(150) NOT NULL,
    dosage           VARCHAR(50),
    frequency        VARCHAR(50),
    duration         VARCHAR(30),
    instructions     TEXT,
    sort_order       SMALLINT    DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_rx_items_prescription ON prescription_items (prescription_id);

CREATE TABLE IF NOT EXISTS bills (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number  VARCHAR(20) UNIQUE,
    appointment_id  UUID        REFERENCES appointments(id),
    patient_id      UUID        REFERENCES patients(id),
    subtotal        NUMERIC(10,2) NOT NULL,
    cgst            NUMERIC(10,2) DEFAULT 0,
    sgst            NUMERIC(10,2) DEFAULT 0,
    total           NUMERIC(10,2) NOT NULL,
    payment_mode    VARCHAR(10)   DEFAULT 'CASH',
    status          VARCHAR(15)   DEFAULT 'PAID',
    pdf_url         TEXT,
    billed_at       TIMESTAMPTZ   DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_bills_billed_at  ON bills (billed_at);
CREATE INDEX IF NOT EXISTS idx_bills_patient    ON bills (patient_id);

CREATE TABLE IF NOT EXISTS bill_items (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_id     UUID        REFERENCES bills(id) ON DELETE CASCADE,
    description VARCHAR(150) NOT NULL,
    hsn_sac     VARCHAR(10),
    amount      NUMERIC(10,2) NOT NULL,
    gst_rate    DECIMAL(4,1)  DEFAULT 0
);

CREATE TABLE IF NOT EXISTS medicine_templates (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name              VARCHAR(150) NOT NULL,
    default_dosage    VARCHAR(50),
    default_frequency VARCHAR(50),
    default_duration  VARCHAR(30),
    usage_count       INT         DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_med_templates_name ON medicine_templates (name);

-- Seed common medicines for new clinic
INSERT INTO medicine_templates (name, default_dosage, default_frequency, default_duration) VALUES
('Paracetamol 500mg',     '1 tablet',  '1-1-1 (TDS)',      '5 days'),
('Paracetamol 650mg',     '1 tablet',  '1-1-1 (TDS)',      '5 days'),
('Azithromycin 500mg',    '1 tablet',  '1-0-0 (OD)',       '3 days'),
('Amoxicillin 500mg',     '1 capsule', '1-0-1 (BD)',       '5 days'),
('Cetirizine 10mg',       '1 tablet',  '0-0-1 (OD Night)', '5 days'),
('Pantoprazole 40mg',     '1 tablet',  '1-0-1 (BD)',       '10 days'),
('Metformin 500mg',       '1 tablet',  '1-1-1 (TDS)',      '30 days'),
('Amlodipine 5mg',        '1 tablet',  '1-0-0 (OD)',       '30 days'),
('ORS Sachet',            '1 sachet',  'After each loose stool', 'As needed'),
('Ibuprofen 400mg',       '1 tablet',  '1-1-1 (TDS)',      '3 days');
