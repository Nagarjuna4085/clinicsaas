import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listPatients, searchPatients } from './api'
import RegisterPatientModal from './RegisterPatientModal'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'
import { Badge, CenteredSpinner, EmptyState, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'

const RECENT_COUNT = 8

function useDebounce(value, ms = 350) {
  const [v, setV] = useState(value)
  useEffect(() => {
    const t = setTimeout(() => setV(value), ms)
    return () => clearTimeout(t)
  }, [value, ms])
  return v
}

export default function PatientsPage() {
  const [term, setTerm] = useState('')
  const [open, setOpen] = useState(false)
  const debounced = useDebounce(term.trim())
  const searching = debounced.length >= 2

  // While searching: server-side search (scales, doesn't load everyone).
  const search = useQuery({
    queryKey: ['patients', 'search', debounced],
    queryFn: () => searchPatients(debounced),
    enabled: searching,
  })

  // Idle state: show a few patients as a starting point.
  const recent = useQuery({
    queryKey: ['patients', 'recent'],
    queryFn: listPatients,
    enabled: !searching,
    select: (list) => list.slice(0, RECENT_COUNT),
  })

  const active = searching ? search : recent
  const rows = active.data || []
  const loading = searching ? search.isFetching : recent.isLoading

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader
          title="Patients"
          subtitle="Search by name or phone to find a patient"
          action={<Button onClick={() => setOpen(true)}>+ Register patient</Button>}
        />
        <Input
          placeholder="Search name or phone… (type 2+ characters)"
          value={term}
          onChange={(e) => setTerm(e.target.value)}
        />
      </Card>

      <Card>
        <p className="mb-3 text-sm font-medium text-slate-500">
          {searching ? `Results for “${debounced}”` : 'Recent patients'}
        </p>
        {loading ? (
          <CenteredSpinner />
        ) : active.isError ? (
          <ErrorState message={apiErrorMessage(active.error)} />
        ) : rows.length === 0 ? (
          <EmptyState
            title={searching ? 'No patients found' : 'No patients yet'}
            hint={searching ? `Nothing matches “${debounced}”.` : 'Register your first patient to get started.'}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-left text-slate-500">
                  <th className="px-3 py-2">UHID</th>
                  <th className="px-3 py-2">Name</th>
                  <th className="px-3 py-2">Phone</th>
                  <th className="px-3 py-2">Age/Sex</th>
                  <th className="px-3 py-2">Allergies</th>
                  <th className="px-3 py-2">Visits</th>
                  <th className="px-3 py-2"></th>
                </tr>
              </thead>
              <tbody>
                {rows.map((p) => (
                  <tr key={p.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-3 py-2 font-mono text-xs">{p.uhid}</td>
                    <td className="px-3 py-2 font-medium text-slate-700">{p.name}</td>
                    <td className="px-3 py-2">{p.phone || '—'}</td>
                    <td className="px-3 py-2">
                      {p.age ?? '—'} / {p.gender || '—'}
                    </td>
                    <td className="px-3 py-2">
                      {p.allergies ? <Badge color="red">{p.allergies}</Badge> : '—'}
                    </td>
                    <td className="px-3 py-2">{p.totalVisits}</td>
                    <td className="px-3 py-2 text-right">
                      <Link to={`/patients/${p.id}`} className="text-brand-600 hover:underline">
                        View
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <RegisterPatientModal open={open} onClose={() => setOpen(false)} />
    </div>
  )
}
