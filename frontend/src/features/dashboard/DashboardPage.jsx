import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getDashboardSummary } from './api'
import { listStaff } from '../staff/api'
import { Card } from '../../components/ui/Card'
import { CenteredSpinner, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'
import { useAuthStore } from '../../store/auth'
import { ROLES } from '../../lib/constants'

const inr = (n) => `₹${Number(n ?? 0).toLocaleString('en-IN')}`

function Stat({ label, value, accent = 'text-slate-800' }) {
  return (
    <Card>
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-1 text-3xl font-bold ${accent}`}>{value}</p>
    </Card>
  )
}

export default function DashboardPage() {
  const { name, clinic, role } = useAuthStore()
  const isAdmin = role === ROLES.ADMIN

  const summary = useQuery({ queryKey: ['dashboard', 'summary'], queryFn: getDashboardSummary })
  // Team counts are an admin-only concern, so only fetch staff for admins.
  const staff = useQuery({ queryKey: ['staff'], queryFn: listStaff, enabled: isAdmin })

  const s = summary.data
  const doctors = (staff.data || []).filter((x) => x.role === 'DOCTOR').length

  // Quick links tailored to the role.
  const links = [
    { to: '/appointments', title: "Today's queue", hint: 'Tokens & consultations', roles: 'all' },
    { to: '/patients', title: 'Patients', hint: 'Register & search', roles: 'all' },
    { to: '/billing', title: 'Billing', hint: 'Invoices & revenue', roles: [ROLES.ADMIN, ROLES.RECEPTIONIST] },
    { to: '/staff', title: 'Staff', hint: 'Manage your team', roles: [ROLES.ADMIN] },
  ].filter((l) => l.roles === 'all' || l.roles.includes(role))

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Welcome, {name}</h1>
        <p className="text-slate-500">{clinic} · today at a glance</p>
      </div>

      {summary.isLoading ? (
        <CenteredSpinner />
      ) : summary.isError ? (
        <ErrorState message={apiErrorMessage(summary.error)} />
      ) : (
        <>
          {/* Everyone sees the operational metrics */}
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <Stat label="Patients today" value={s.patientsToday} accent="text-blue-600" />
            <Stat label="Waiting now" value={s.waitingCount} accent="text-amber-600" />
            {isAdmin && <Stat label="Revenue today" value={inr(s.totalRevenue)} accent="text-green-600" />}
            {isAdmin && <Stat label="Bills today" value={s.billCount} accent="text-brand-700" />}
          </div>

          {/* Financial detail — admin only */}
          {isAdmin && (
            <>
              <Card>
                <p className="mb-3 text-sm font-medium text-slate-600">Payments by mode (today)</p>
                <div className="grid grid-cols-3 gap-4">
                  <Split label="Cash" value={inr(s.cashTotal)} />
                  <Split label="UPI" value={inr(s.upiTotal)} />
                  <Split label="Card" value={inr(s.cardTotal)} />
                </div>
              </Card>

              <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
                <Stat label="Total staff" value={staff.data?.length ?? 0} />
                <Stat label="Doctors" value={doctors} />
              </div>
            </>
          )}
        </>
      )}

      <div className="grid gap-4 sm:grid-cols-4">
        {links.map((l) => (
          <QuickLink key={l.to} to={l.to} title={l.title} hint={l.hint} />
        ))}
      </div>
    </div>
  )
}

function Split({ label, value }) {
  return (
    <div className="rounded-lg bg-slate-50 p-4 text-center">
      <p className="text-xs uppercase tracking-wide text-slate-400">{label}</p>
      <p className="mt-1 text-lg font-semibold text-slate-700">{value}</p>
    </div>
  )
}

function QuickLink({ to, title, hint }) {
  return (
    <Link to={to} className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm hover:border-brand-300">
      <p className="font-semibold text-slate-800">{title} →</p>
      <p className="text-sm text-slate-500">{hint}</p>
    </Link>
  )
}
