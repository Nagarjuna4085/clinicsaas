import api from '../../lib/apiClient'

export const getAudit = () => api.get('/api/audit').then((r) => r.data)
