import api from '../../lib/apiClient'

export const listPatients = () =>
  api.get('/api/patients').then((r) => r.data)

export const registerPatient = (payload) =>
  api.post('/api/patients', payload).then((r) => r.data)

export const searchPatients = (q) =>
  api.get('/api/patients/search', { params: { q } }).then((r) => r.data)

export const getPatientByPhone = (phone) =>
  api.get(`/api/patients/by-phone/${phone}`).then((r) => r.data)

export const getPatient = (id) =>
  api.get(`/api/patients/${id}`).then((r) => r.data)

export const getPatientHistory = (id) =>
  api.get(`/api/patients/${id}/history`).then((r) => r.data)

export const exportPatient = (id) =>
  api.get(`/api/patients/${id}/export`).then((r) => r.data)

export const erasePatient = (id) =>
  api.delete(`/api/patients/${id}`).then((r) => r.data)
