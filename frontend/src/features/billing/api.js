import api from '../../lib/apiClient'

export const getTodayBills = () =>
  api.get('/api/bills/today').then((r) => r.data)

export const getBill = (id) =>
  api.get(`/api/bills/${id}`).then((r) => r.data)
