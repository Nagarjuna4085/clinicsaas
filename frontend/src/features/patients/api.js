import api from '../../lib/apiClient'

export const registerPatient = (payload) =>
  api.post('/api/patients', payload).then((r) => r.data)

export const searchPatients = (q) =>
  api.get('/api/patients/search', { params: { q } }).then((r) => r.data)

export const getPatientByPhone = (phone) =>
  api.get(`/api/patients/by-phone/${phone}`).then((r) => r.data)

export const getPatient = (id) =>
  api.get(`/api/patients/${id}`).then((r) => r.data)
