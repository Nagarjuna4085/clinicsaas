import { NavLink } from 'react-router-dom'
import { NAV_ITEMS } from '../../lib/constants'
import { useAuthStore } from '../../store/auth'

export default function Sidebar() {
  const role = useAuthStore((s) => s.role)
  const items = NAV_ITEMS.filter((i) => i.roles.includes(role))

  return (
    <aside className="hidden w-60 shrink-0 border-r border-slate-200 bg-white md:block">
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
            end={item.to === '/'}
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
    </aside>
  )
}
