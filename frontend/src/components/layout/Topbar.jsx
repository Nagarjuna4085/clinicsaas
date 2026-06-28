import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/auth'
import { Badge } from '../ui/Misc'

export default function Topbar({ onMenuClick }) {
  const { name, role, clinic, logout } = useAuthStore()
  const navigate = useNavigate()

  const onLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className="flex h-16 items-center justify-between gap-2 border-b border-slate-200 bg-white px-4 sm:px-6">
      <div className="flex min-w-0 items-center gap-2">
        <button
          onClick={onMenuClick}
          className="-ml-1 rounded-lg p-2 text-slate-600 hover:bg-slate-100 md:hidden"
          aria-label="Open menu"
        >
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <line x1="3" y1="6" x2="21" y2="6" />
            <line x1="3" y1="12" x2="21" y2="12" />
            <line x1="3" y1="18" x2="21" y2="18" />
          </svg>
        </button>
        <div className="min-w-0">
          <p className="truncate text-sm font-semibold text-slate-800">{clinic}</p>
          <p className="hidden text-xs text-slate-500 sm:block">Multi-tenant clinic workspace</p>
        </div>
      </div>
      <div className="flex shrink-0 items-center gap-3">
        <div className="hidden text-right sm:block">
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
