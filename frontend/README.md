# ClinicFlow Frontend

Modern React frontend for the ClinicFlow multi-tenant clinic SaaS, talking only to the existing Spring Boot REST API.

## Stack
React 19 · Vite · React Router v6 · Axios · Tailwind CSS · TanStack Query · React Hook Form + Zod · Zustand (auth).

## Prerequisites
- Node 18+ and npm
- The ClinicFlow backend running on `http://localhost:8080` (Postgres up, a clinic registered).

## Setup
```bash
cd frontend
npm install
cp .env.example .env   # adjust VITE_API_BASE_URL if your backend isn't on :8080
npm run dev            # http://localhost:5173
```

The backend's CORS already allows `http://localhost:*`, so the dev server works directly. A Vite proxy for `/api` is also configured as an alternative.

## Auth flow
1. **Sign up** a clinic at `/signup` (calls `POST /api/public/clinics/register`).
2. **Log in** at `/login` with the owner/staff phone → OTP. The OTP is printed in the **backend console** (MSG91 isn't wired in the backend MVP).
3. The JWT is stored in a persisted Zustand store and attached to every request; a 401 clears it and redirects to login.

## Modules (mapped to real endpoints only)
| Module | Endpoints used |
|---|---|
| Auth | `/api/public/clinics/register`, `/api/auth/send-otp`, `/api/auth/verify-otp` |
| Dashboard | derived from `/api/appointments/today` + `/api/staff` |
| Patients | `/api/patients` (POST), `/search`, `/by-phone/{phone}`, `/{id}` |
| Appointments | `/api/appointments` (POST), `/today`, `/{id}/status` (PATCH) |
| Staff | `/api/staff` (GET, POST — add is ADMIN-only) |

Modules with **no backend endpoint** (Billing, Reports, Notifications, Clinic profile/edit, Staff edit/delete, password reset) are intentionally not built — no mock APIs are used. Search & pagination for Staff are client-side since the API returns the full list.

## Structure
```
src/
├── app/            # App + router
├── lib/            # axios client, queryClient, jwt, constants
├── store/          # Zustand auth store
├── routes/         # ProtectedRoute, RoleRoute
├── components/     # ui primitives + layout (AppShell, Sidebar, Topbar)
└── features/       # auth, dashboard, patients, appointments, staff
```

## Build
```bash
npm run build
npm run preview
```
