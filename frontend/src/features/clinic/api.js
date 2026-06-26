import api from '../../lib/apiClient'

export const getClinic = () => api.get('/api/clinic').then((r) => r.data)

export const updateClinic = (payload) =>
  api.put('/api/clinic', payload).then((r) => r.data)
