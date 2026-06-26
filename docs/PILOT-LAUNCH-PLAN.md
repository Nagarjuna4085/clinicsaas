# ClinicFlow — Pilot Launch Plan

Goal: get from "working demo" to **one real pilot clinic using it daily**, with the
smallest responsible amount of work. The product features are done; what remains
is the "make it real and legal" layer. Three gates, in order.

---

## Gate 1 — Make login actually work (real OTP)

A clinic can't use the app if staff can't log in. Today the OTP only prints to the
server console / shows in dev mode.

**Pick one delivery channel:**

| Option | What it needs | Time to ready | Notes |
|---|---|---|---|
| **SMS via MSG91** | MSG91 account + **DLT registration** (sender ID + template approval) | ~2–5 days (DLT approval) | Standard for India; DLT is a regulator step, unavoidable for SMS |
| **WhatsApp OTP via Gupshup** | Gupshup account + WhatsApp Business sender + approved template | ~2–7 days | Often higher delivery + cheaper; needs Meta business verification |

**Steps**
1. Create the provider account (MSG91 or Gupshup).
2. Complete DLT / WhatsApp business verification + get the OTP template approved.
3. Set env vars (`MSG91_AUTH_KEY` + `MSG91_TEMPLATE_ID`, or `GUPSHUP_*`).
4. Dev adjusts the provider call to match the approved template (small code change).
5. Test end-to-end with a real phone.

**Interim (already built):** `DEV_TOOLS=true` returns the OTP in the UI so you can
demo before the provider is live. Must be **off** in production.

**Owner does:** accounts, KYC, DLT/template approval.  **Dev does:** wire the call to the template, test.

---

## Gate 2 — Deploy it somewhere (smallest viable)

Right now it only runs on your laptop. A clinic needs a URL.

**Minimum production stack**
- **App host** — one of: Render, Railway, Fly.io, or a small cloud VPS (DigitalOcean/Hetzner). All can run the existing Docker images.
- **Managed PostgreSQL** — use the host's managed Postgres (don't self-run the DB for real patient data). Gives you backups + encryption at rest out of the box.
- **Domain + HTTPS** — a domain (e.g. clinicflow.in) with TLS. Managed hosts terminate TLS for you.
- **Secrets** — set `JWT_SECRET` (strong, 32+ chars), DB creds, and provider keys as host environment secrets — never in the repo.

**Steps**
1. Provision managed Postgres; create the `clinicflow` database.
2. Deploy the backend image; point `DB_URL/DB_USERNAME/DB_PASSWORD` at the managed DB; set a real `JWT_SECRET`; `DEV_TOOLS=false`.
3. Deploy the frontend image (or any static host) with `VITE_API_BASE_URL=https://api.yourdomain`.
4. Point Razorpay webhook at `https://api.yourdomain/api/public/webhooks/razorpay`.
5. Smoke test: register a clinic → log in (real OTP) → full patient/visit/bill flow.

**Owner does:** pick host, buy domain, create accounts.  **Dev does:** deploy configs (render.yaml / fly.toml / compose), env wiring, smoke test.

> Recommendation for a first pilot: **Render or Railway** — managed Postgres + Docker deploy + TLS with the least setup. Cheapest to start, easy to graduate later.

---

## Gate 3 — Minimum compliance for handling patient data

You'll be storing medical records. For a *controlled pilot* you don't need
enterprise-grade everything, but these are the non-negotiable minimums under
India's **DPDP Act 2023**.

**Must-have before real patients**
- [ ] **Consent capture** — a clear consent checkbox at clinic signup and at patient registration (store who/when consented). *Dev: small DB field + UI.*
- [ ] **Privacy Policy + Terms of Service + a basic Data Processing Agreement** with the clinic. *Owner: use a template / lawyer.*
- [ ] **Encryption in transit (HTTPS)** — comes free with a managed host. *Gate 2.*
- [ ] **Encryption at rest** — comes free with managed Postgres; optionally add field-level encryption for the most sensitive columns (allergies, diagnosis, ABHA). *Dev: optional hardening.*
- [ ] **Backups** — managed Postgres automated backups enabled + a tested restore. *Owner toggles; Dev verifies.*
- [ ] **Audit log** — record who viewed/changed a patient record. *Dev: ~1 day.*
- [ ] **Data export & delete** — endpoints to export and erase a patient's data on request (DPDP right). *Dev: ~1 day.*

**Can defer past the pilot**
- ABHA/ABDM integration, full role-based audit dashboards, DPDP grievance-officer workflow, ISO/security certifications.

**Owner does:** legal docs, sign DPA with pilot clinic.  **Dev does:** consent fields, audit log, export/delete endpoints, optional field encryption.

---

## Suggested sequence (~2–3 weeks)

1. **Week 1** — Dev: consent capture, audit log, export/delete, finalize provider call. Owner: start MSG91/Gupshup + DLT (runs in background), draft privacy policy/ToS.
2. **Week 2** — Dev: deploy to Render/Railway (DB, domain, TLS, secrets), smoke test. Owner: finish DLT approval, sign DPA template with the pilot clinic.
3. **Week 3** — Real OTP live → onboard the pilot clinic → watch it run, fix what real usage surfaces.

## Pilot success criteria
- Staff log in with a real OTP on a real phone.
- A full day runs through it: register → queue → consult → prescribe (PDF) → bill.
- Follow-up reminders actually deliver.
- Owner checks the dashboard and trusts the numbers.
- Zero data isolation / security incidents.

---

### What I (dev) can start now vs. what needs you (founder)

**I can build now:** consent capture, audit logging, data export/delete endpoints,
field-level encryption, and the deploy configs for whichever host you pick.

**Only you can do:** create provider/host accounts, complete DLT / WhatsApp /
Razorpay verification (KYC), buy the domain, and get the privacy policy / ToS / DPA
(template or lawyer).
