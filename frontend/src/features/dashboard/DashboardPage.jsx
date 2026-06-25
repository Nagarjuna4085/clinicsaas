import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getTodayQueue } from '../appointments/api'
import { listStaff } from '../staff/api'
import { Card } from '../../components/ui/Card'
import { CenteredSpinner } from '../../components/ui/Misc'
import { useAuthStore } from '../../store/auth'

function Stat({ label, value, accent = 'text-slate-800' }) {
  return (
    <Card>
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-1 text-3xl font-bold ${accent}`}>{value}</p>
    </Card>
  )
}

// The backend has no dedicated dashboard endpoint, so all metrics here are
// derived purely from existing endpoints (today's queue + staff list).
export default function DashboardPage() {
  const { name, clinic } = useAuthStore()

  const queue = useQuery({ queryKey: ['appointments', 'today'], queryFn: getTodayQueue })
  const staff = useQuery({ queryKey: ['staff'], queryFn: listStaff })

  const appts = queue.data || []
  const waiting = appts.filter((a) => a.status === 'WAITING').length
  const consulting = appts.filter((a) => a.status === 'CONSULTING').length
  const completed = appts.filter((a) => a.status === 'COMPLETED').length
  const doctors = (staff.data || []).filter((s) => s.role === 'DOCTOR').length

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Welcome, {name}</h1>
        <p className="text-slate-500">{clinic} · today at a glance</p>
      </div>

      {queue.isLoading || staff.isLoading ? (
        <CenteredSpinner />
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <Stat label="In queue today" value={appts.length} accent="text-brand-700" />
            <Stat label="Waiting" value={waiting} accent="text-amber-600" />
            <Stat label="Consulting" value={consulting} accent="text-blue-600" />
            <Stat label="Completed" value={completed} accent="text-green-600" />
          </div>
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <Stat label="Total staff" value={staff.data?.length ?? 0} />
            <Stat label="Doctors" value={doctors} />
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            <Link to="/appointments" className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm hover:border-brand-300">
              <p className="font-semibold text-slate-800">Today's queue →</p>
              <p className="text-sm text-slate-500">Manage tokens and consultations</p>
            </Link>
            <Link to="/patients" className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm hover:border-brand-300">
              <p className="font-semibold text-slate-800">Patients →</p>
              <p className="text-sm text-slate-500">Register and search patients</p>
            </Link>
            <Link to="/staff" className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm hover:border-brand-300">
              <p className="font-semibold text-slate-800">Staff →</p>
              <p className="text-sm text-slate-500">Manage your clinic team</p>
            </Link>
          </div>
        </>
      )}
    </div>
  )
}
