import api from '../../lib/apiClient'

export const listStaff = () => api.get('/api/staff').then((r) => r.data)

export const addStaff = (payload) =>
  api.post('/api/staff', payload).then((r) => r.data)
