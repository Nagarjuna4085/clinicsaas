import { NavLink } from 'react-router-dom'
import { NAV_ITEMS } from '../../lib/constants'
import { useAuthStore } from '../../store/auth'

function NavContent({ onNavigate }) {
  const role = useAuthStore((s) => s.role)
  const items = NAV_ITEMS.filter((i) => i.roles.includes(role))

  return (
    <>
      <div className="flex h-16 items-center gap-2 border-b border-slate-200 px-6">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 font-bold text-white">
          C
        </div>
        <span className="text-lg font-semibold text-slate-800">ClinicFlow</span>
      </div>
      <nav className="flex flex-col gap-1 p-3">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/dashboard'}
            onClick={onNavigate}
            className={({ isActive }) =>
              `rounded-lg px-3 py-2 text-sm font-medium transition ${
                isActive
                  ? 'bg-brand-50 text-brand-700'
                  : 'text-slate-600 hover:bg-slate-100'
              }`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </>
  )
}

export default function Sidebar({ open, onClose }) {
  return (
    <>
      {/* Desktop: static sidebar */}
      <aside className="hidden w-60 shrink-0 border-r border-slate-200 bg-white md:block">
        <NavContent />
      </aside>

      {/* Mobile: slide-in drawer with backdrop */}
      {open && (
        <div className="fixed inset-0 z-40 md:hidden">
          <div
            className="absolute inset-0 bg-slate-900/40"
            onClick={onClose}
            aria-hidden="true"
          />
          <aside className="absolute left-0 top-0 h-full w-60 bg-white shadow-xl">
            <NavContent onNavigate={onClose} />
          </aside>
        </div>
      )}
    </>
  )
}
