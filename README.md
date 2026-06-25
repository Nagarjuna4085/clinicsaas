# ClinicFlow API — Spring Boot Multi-tenant Backend

## Run with Docker (whole stack)

The fastest way to run everything (Postgres + API + frontend):

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- API + Swagger: http://localhost:8080/swagger-ui.html

Postgres data persists in the `pgdata` volume. Override secrets/integration keys
via the `environment:` block in `docker-compose.yml` (notably `JWT_SECRET`, and
the `RAZORPAY_*` / `MSG91_*` / `GUPSHUP_*` keys). `DEV_TOOLS` is on for local
testing — set it to `false` for anything real.

To build the images individually:

```bash
docker build -t clinicflow-api .              # backend
docker build -t clinicflow-frontend frontend  # frontend
```

## CI

`.github/workflows/ci.yml` runs on every push / PR:
- **backend** — `mvn -B verify` (compiles + runs the unit tests)
- **frontend** — `npm ci && npm run build`
- **docker** — builds both images (after the above pass)

## Project Structure

```
src/main/java/com/clinicflow/
├── ClinicFlowApplication.java        ← Entry point
│
├── context/
│   └── TenantContext.java            ← Thread-local schema store
│
├── config/
│   ├── HibernateConfig.java          ← Wires multi-tenant to Hibernate
│   ├── MultiTenantConnectionProvider ← Switches search_path per request
│   ├── TenantSchemaResolver.java     ← Tells Hibernate which schema
│   └── SecurityConfig.java          ← JWT + CORS config
│
├── security/
│   ├── JwtUtil.java                  ← Generate + parse JWT
│   └── JwtAuthFilter.java            ← Sets TenantContext per request
│
├── entity/
│   ├── global/Tenant.java            ← global.tenants table
│   └── tenant/                       ← Per-clinic tables
│       ├── Staff.java
│       ├── Patient.java
│       ├── Appointment.java
│       ├── Vitals.java
│       ├── Prescription.java
│       ├── PrescriptionItem.java
│       ├── Bill.java
│       └── BillItem.java
│
├── repository/
│   ├── global/TenantRepository.java
│   └── tenant/                       ← All per-clinic repositories
│
├── service/
│   ├── TenantProvisioningService.java ← Creates schema on signup
│   ├── PatientService.java
│   └── AppointmentService.java
│
├── controller/
│   ├── AuthController.java           ← /api/auth/**
│   ├── PatientController.java        ← /api/patients/**
│   └── AppointmentController.java    ← /api/appointments/**
│
└── scheduler/
    └── FollowUpReminderScheduler.java ← Daily WhatsApp reminders

src/main/resources/
├── application.yml
└── db/migration/
    ├── global/V1__create_tenants_table.sql   ← Runs once at startup
    └── tenant/V1__create_clinic_tables.sql   ← Runs per new clinic
```

## How Multi-tenancy Works

1. Every HTTP request carries `Authorization: Bearer <JWT>`
2. JWT contains: `{ userId, role, tenantId: "tenant_9876543210" }`
3. `JwtAuthFilter` extracts `tenantId` → sets `TenantContext`
4. `MultiTenantConnectionProvider` runs `SET search_path TO tenant_9876543210`
5. All JPA queries transparently hit that clinic's tables
6. `TenantContext.clear()` in finally block prevents thread leakage

## API Endpoints

### Auth
POST /api/auth/send-otp      { phone }
POST /api/auth/verify-otp    { phone, otp } → { token, role, name, clinicName }

### Patients
POST   /api/patients              Register/find patient
GET    /api/patients/search?q=    Search by name or phone
GET    /api/patients/by-phone/:p  Lookup by phone
GET    /api/patients/:id          Get patient details

### Appointments (Queue)
POST   /api/appointments              Create appointment + issue token
GET    /api/appointments/today        All of today's queue
GET    /api/appointments/today/doctor/:id   Doctor's queue
PATCH  /api/appointments/:id/status   Update status

## Setup

### Prerequisites
- Java 21
- PostgreSQL 15+
- Maven 3.9+

### Run locally
```bash
# 1. Create database
psql -c "CREATE DATABASE clinicflow;"
psql -c "CREATE SCHEMA global;" clinicflow

# 2. Set env vars
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-32-char-secret-key-here-min

# 3. Run
mvn spring-boot:run
```

### First clinic signup
```bash
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phone":"9876543210"}'

# Check console for OTP, then:
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"phone":"9876543210","otp":"123456"}'
```

## What's NOT built yet (Phase 2)
- PrescriptionController + PrescriptionService
- BillingController + DashboardController
- MSG91 OTP integration
- Gupshup WhatsApp integration
- AWS S3 PDF upload
- iText PDF generation
- Razorpay subscription webhook
