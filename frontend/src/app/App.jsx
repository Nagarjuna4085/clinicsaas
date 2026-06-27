import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from '../routes/ProtectedRoute'
import RoleRoute from '../routes/RoleRoute'
import AppShell from '../components/layout/AppShell'
import { ROLES } from '../lib/constants'

import LoginPage from '../features/auth/LoginPage'
import SignupPage from '../features/auth/SignupPage'
import ForgotPasswordPage from '../features/auth/ForgotPasswordPage'
import SuspendedPage from '../features/subscription/SuspendedPage'
import PricingPage from '../features/legal/PricingPage'
import PrivacyPage from '../features/legal/PrivacyPage'
import TermsPage from '../features/legal/TermsPage'
import RefundPage from '../features/legal/RefundPage'
import ContactPage from '../features/legal/ContactPage'
import DashboardPage from '../features/dashboard/DashboardPage'
import PatientsPage from '../features/patients/PatientsPage'
import PatientDetailPage from '../features/patients/PatientDetailPage'
import AppointmentsPage from '../features/appointments/AppointmentsPage'
import ConsultationPage from '../features/clinical/ConsultationPage'
import BillingPage from '../features/billing/BillingPage'
import StaffPage from '../features/staff/StaffPage'
import ClinicSettingsPage from '../features/clinic/ClinicSettingsPage'
import AuditPage from '../features/audit/AuditPage'
import ReportsPage from '../features/reports/ReportsPage'

export default function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/forgot" element={<ForgotPasswordPage />} />
      <Route path="/suspended" element={<SuspendedPage />} />
      {/* Public legal/marketing pages (required for payment KYC) */}
      <Route path="/pricing" element={<PricingPage />} />
      <Route path="/privacy" element={<PrivacyPage />} />
      <Route path="/terms" element={<TermsPage />} />
      <Route path="/refund" element={<RefundPage />} />
      <Route path="/contact" element={<ContactPage />} />

      {/* Authenticated app shell */}
      <Route
        element={
          <ProtectedRoute>
            <AppShell />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="/appointments" element={<AppointmentsPage />} />
        <Route
          path="/appointments/:id/consult"
          element={
            <RoleRoute roles={[ROLES.DOCTOR, ROLES.NURSE, ROLES.ADMIN]}>
              <ConsultationPage />
            </RoleRoute>
          }
        />
        <Route path="/patients" element={<PatientsPage />} />
        <Route path="/patients/:id" element={<PatientDetailPage />} />
        <Route
          path="/billing"
          element={
            <RoleRoute roles={[ROLES.ADMIN, ROLES.RECEPTIONIST, ROLES.DOCTOR]}>
              <BillingPage />
            </RoleRoute>
          }
        />
        <Route
          path="/staff"
          element={
            <RoleRoute roles={[ROLES.ADMIN, ROLES.RECEPTIONIST, ROLES.DOCTOR, ROLES.NURSE]}>
              <StaffPage />
            </RoleRoute>
          }
        />
        <Route
          path="/clinic"
          element={
            <RoleRoute roles={[ROLES.ADMIN]}>
              <ClinicSettingsPage />
            </RoleRoute>
          }
        />
        <Route
          path="/audit"
          element={
            <RoleRoute roles={[ROLES.ADMIN]}>
              <AuditPage />
            </RoleRoute>
          }
        />
        <Route
          path="/reports"
          element={
            <RoleRoute roles={[ROLES.ADMIN]}>
              <ReportsPage />
            </RoleRoute>
          }
        />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
