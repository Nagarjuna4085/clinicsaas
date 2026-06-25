import api from '../../lib/apiClient'

// Public clinic onboarding + phone-OTP auth.
export const registerClinic = (payload) =>
  api.post('/api/public/clinics/register', payload).then((r) => r.data)

export const sendOtp = (phone) =>
  api.post('/api/auth/send-otp', { phone }).then((r) => r.data)

export const verifyOtp = ({ phone, otp }) =>
  api.post('/api/auth/verify-otp', { phone, otp }).then((r) => r.data)
