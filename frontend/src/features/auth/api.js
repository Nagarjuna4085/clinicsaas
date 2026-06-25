import api from '../../lib/apiClient'

// OTP (signup verification + forgot-password only)
export const sendOtp = (phone) =>
  api.post('/api/auth/send-otp', { phone }).then((r) => r.data)

// Clinic signup (requires OTP + owner password)
export const registerClinic = (payload) =>
  api.post('/api/public/clinics/register', payload).then((r) => r.data)

// Password login → { token, role, name, clinicName, mustReset }
export const login = (payload) =>
  api.post('/api/auth/login', payload).then((r) => r.data)

// First-login: set a new password using the temporary one
export const setPassword = (payload) =>
  api.post('/api/auth/set-password', payload).then((r) => r.data)

// Forgot password: OTP + new password
export const resetPassword = (payload) =>
  api.post('/api/auth/reset-password', payload).then((r) => r.data)
