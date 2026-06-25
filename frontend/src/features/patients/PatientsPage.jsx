import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listPatients } from './api'
import RegisterPatientModal from './RegisterPatientModal'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'
import { Badge, CenteredSpinner, EmptyState, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'

export default function PatientsPage() {
  const [term, setTerm] = useState('')
  const [open, setOpen] = useState(false)

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['patients', 'all'],
    queryFn: listPatients,
  })

  // Client-side filter over the full list (clinics are small; instant search).
  const filtered = useMemo(() => {
    const list = data || []
    const q = term.trim().toLowerCase()
    if (!q) return list
    return list.filter(
      (p) =>
        p.name?.toLowerCase().includes(q) ||
        p.phone?.includes(q) ||
        p.uhid?.toLowerCase().includes(q)
    )
  }, [data, term])

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader
          title="Patients"
          subtitle="All registered patients"
          action={<Button onClick={() => setOpen(true)}>+ Register patient</Button>}
        />
        <Input
          placeholder="Filter by name, phone or UHID…"
          value={term}
          onChange={(e) => setTerm(e.target.value)}
        />
      </Card>

      <Card>
        {isLoading ? (
          <CenteredSpinner />
        ) : isError ? (
          <ErrorState message={apiErrorMessage(error)} />
        ) : filtered.length === 0 ? (
          <EmptyState
            title={data?.length ? 'No matches' : 'No patients yet'}
            hint={data?.length ? `Nothing matches “${term}”.` : 'Register your first patient to get started.'}
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
                {filtered.map((p) => (
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
