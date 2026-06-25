import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth'

// Guards a route by role. Redirects to dashboard if the current role is not allowed.
export default function RoleRoute({ roles, children }) {
  const role = useAuthStore((s) => s.role)
  if (!roles.includes(role)) {
    return <Navigate to="/" replace />
  }
  return children
}
