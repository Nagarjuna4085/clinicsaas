import api from '../../lib/apiClient'

export const getDashboardSummary = () =>
  api.get('/api/dashboard/summary').then((r) => r.data)
