import api from '../../lib/apiClient'

export const getTodayQueue = () =>
  api.get('/api/appointments/today').then((r) => r.data)

export const getDoctorQueue = (doctorId) =>
  api.get(`/api/appointments/today/doctor/${doctorId}`).then((r) => r.data)

export const bookAppointment = (payload) =>
  api.post('/api/appointments', payload).then((r) => r.data)

export const updateAppointmentStatus = (id, status) =>
  api.patch(`/api/appointments/${id}/status`, { status }).then((r) => r.data)
