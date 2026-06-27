import api from '../../lib/apiClient'

export const getRevenueReport = (days = 30) =>
  api.get('/api/reports/revenue', { params: { days } }).then((r) => r.data)
