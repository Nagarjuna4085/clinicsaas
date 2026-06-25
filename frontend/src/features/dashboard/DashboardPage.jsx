import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getDashboardSummary } from './api'
import { listStaff } from '../staff/api'
import { Card } from '../../components/ui/Card'
import { CenteredSpinner, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'
import { useAuthStore } from '../../store/auth'

const inr = (n) => `₹${Number(n ?? 0).toLocaleString('en-IN')}`

function Stat({ label, value, accent = 'text-slate-800' }) {
  return (
    <Card>
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-1 text-3xl font-bold ${accent}`}>{value}</p>
    </Card>
  )
}

// Metrics now come from the real /api/dashboard/summary endpoint; team counts
// come from /api/staff.
export default function DashboardPage() {
  const { name, clinic } = useAuthStore()

  const summary = useQuery({ queryKey: ['dashboard', 'summary'], queryFn: getDashboardSummary })
  const staff = useQuery({ queryKey: ['staff'], queryFn: listStaff })

  const s = summary.data
  const doctors = (staff.data || []).filter((x) => x.role === 'DOCTOR').length

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
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <Stat label="Revenue today" value={inr(s.totalRevenue)} accent="text-green-600" />
            <Stat label="Bills today" value={s.billCount} accent="text-brand-700" />
            <Stat label="Patients today" value={s.patientsToday} accent="text-blue-600" />
            <Stat label="Waiting now" value={s.waitingCount} accent="text-amber-600" />
          </div>

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

      <div className="grid gap-4 sm:grid-cols-4">
        <QuickLink to="/appointments" title="Today's queue" hint="Tokens & consultations" />
        <QuickLink to="/patients" title="Patients" hint="Register & search" />
        <QuickLink to="/billing" title="Billing" hint="Invoices & revenue" />
        <QuickLink to="/staff" title="Staff" hint="Manage your team" />
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
