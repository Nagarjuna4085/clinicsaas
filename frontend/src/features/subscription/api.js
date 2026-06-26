import api from '../../lib/apiClient'

export const getSubscription = () => api.get('/api/subscription').then((r) => r.data)

export const subscribe = (plan) =>
  api.post('/api/subscription', { plan }).then((r) => r.data)

// Dev-only: simulate a webhook status change (server rejects unless enabled).
export const devSetStatus = (status) =>
  api.post(`/api/subscription/dev/status?status=${status}`).then((r) => r.data)
