import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/auth'
import { Badge } from '../ui/Misc'

export default function Topbar() {
  const { name, role, clinic, logout } = useAuthStore()
  const navigate = useNavigate()

  const onLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6">
      <div>
        <p className="text-sm font-semibold text-slate-800">{clinic}</p>
        <p className="text-xs text-slate-500">Multi-tenant clinic workspace</p>
      </div>
      <div className="flex items-center gap-3">
        <div className="text-right">
          <p className="text-sm font-medium text-slate-700">{name}</p>
          <Badge color="violet">{role}</Badge>
        </div>
        <button
          onClick={onLogout}
          className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50"
        >
          Logout
        </button>
      </div>
    </header>
  )
}
