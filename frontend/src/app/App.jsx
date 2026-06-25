import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from '../routes/ProtectedRoute'
import RoleRoute from '../routes/RoleRoute'
import AppShell from '../components/layout/AppShell'
import { ROLES } from '../lib/constants'

import LoginPage from '../features/auth/LoginPage'
import SignupPage from '../features/auth/SignupPage'
import DashboardPage from '../features/dashboard/DashboardPage'
import PatientsPage from '../features/patients/PatientsPage'
import PatientDetailPage from '../features/patients/PatientDetailPage'
import AppointmentsPage from '../features/appointments/AppointmentsPage'
import StaffPage from '../features/staff/StaffPage'

export default function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

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
        <Route path="/patients" element={<PatientsPage />} />
        <Route path="/patients/:id" element={<PatientDetailPage />} />
        <Route
          path="/staff"
          element={
            <RoleRoute roles={[ROLES.ADMIN, ROLES.RECEPTIONIST, ROLES.DOCTOR, ROLES.NURSE]}>
              <StaffPage />
            </RoleRoute>
          }
        />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
