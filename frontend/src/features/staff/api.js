import api from '../../lib/apiClient'

export const listStaff = () => api.get('/api/staff').then((r) => r.data)

export const addStaff = (payload) =>
  api.post('/api/staff', payload).then((r) => r.data)

export const updateStaff = (id, payload) =>
  api.put(`/api/staff/${id}`, payload).then((r) => r.data)

export const deactivateStaff = (id) =>
  api.delete(`/api/staff/${id}`).then((r) => r.data)
