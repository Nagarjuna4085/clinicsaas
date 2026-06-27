import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getRevenueReport } from './api'
import { Card, CardHeader } from '../../components/ui/Card'
import { CenteredSpinner, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'

const inr = (n) => `₹${Number(n ?? 0).toLocaleString('en-IN')}`
const RANGES = [7, 30, 90]

export default function ReportsPage() {
  const [days, setDays] = useState(30)
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['reports', 'revenue', days],
    queryFn: () => getRevenueReport(days),
  })

  const series = data?.series || []
  const maxRev = Math.max(1, ...series.map((d) => Number(d.revenue ?? 0)))

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader
          title="Reports"
          subtitle="Revenue & visits over time"
          action={
            <div className="inline-flex overflow-hidden rounded-lg border border-slate-300 text-sm">
              {RANGES.map((r) => (
                <button
                  key={r}
                  className={`px-3 py-1.5 ${days === r ? 'bg-brand-600 text-white' : 'bg-white text-slate-600'}`}
                  onClick={() => setDays(r)}
                >
                  {r}d
                </button>
              ))}
            </div>
          }
        />
        {data && (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
            <Stat label={`Revenue (${days}d)`} value={inr(data.totalRevenue)} accent="text-green-600" />
            <Stat label={`Visits (${days}d)`} value={data.totalVisits} accent="text-blue-600" />
            <Stat
              label="Avg / day"
              value={inr(Number(data.totalRevenue ?? 0) / Math.max(1, days))}
            />
          </div>
        )}
      </Card>

      <Card>
        <p className="mb-3 text-sm font-medium text-slate-600">Daily revenue</p>
        {isLoading ? (
          <CenteredSpinner />
        ) : isError ? (
          <ErrorState message={apiErrorMessage(error)} />
        ) : series.length === 0 ? (
          <p className="py-8 text-center text-sm text-slate-400">No data yet.</p>
        ) : (
          <div className="flex h-48 items-end gap-[3px] overflow-x-auto">
            {series.map((d) => {
              const h = Math.round((Number(d.revenue ?? 0) / maxRev) * 100)
              return (
                <div key={d.date} className="group flex min-w-[6px] flex-1 flex-col items-center justify-end">
                  <div
                    className="w-full rounded-t bg-brand-400 transition-all group-hover:bg-brand-600"
                    style={{ height: `${h}%` }}
                    title={`${d.date}: ${inr(d.revenue)} · ${d.visits} visits`}
                  />
                </div>
              )
            })}
          </div>
        )}
        <p className="mt-2 text-xs text-slate-400">
          Hover a bar for the date, revenue and visit count. {series[0]?.date} → {series[series.length - 1]?.date}
        </p>
      </Card>
    </div>
  )
}

function Stat({ label, value, accent = 'text-slate-800' }) {
  return (
    <Card>
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-1 text-2xl font-bold ${accent}`}>{value}</p>
    </Card>
  )
}
