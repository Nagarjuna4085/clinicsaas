// Minimal client-side JWT payload decoder (no verification — display only).
export function decodeJwt(token) {
  try {
    const payload = token.split('.')[1]
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(json)
  } catch {
    return null
  }
}

export function isExpired(token) {
  const claims = decodeJwt(token)
  if (!claims?.exp) return false
  return Date.now() >= claims.exp * 1000
}
