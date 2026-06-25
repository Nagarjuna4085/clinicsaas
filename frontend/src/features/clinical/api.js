import api from '../../lib/apiClient'

// Vitals
export const getVitals = (appointmentId) =>
  api.get(`/api/vitals/by-appointment/${appointmentId}`).then((r) => r.data || null)

export const saveVitals = (payload) =>
  api.post('/api/vitals', payload).then((r) => r.data)

// Prescription + consultation
export const getPrescription = (appointmentId) =>
  api.get(`/api/prescriptions/by-appointment/${appointmentId}`).then((r) => r.data || null)

export const savePrescription = (payload) =>
  api.post('/api/prescriptions', payload).then((r) => r.data)
