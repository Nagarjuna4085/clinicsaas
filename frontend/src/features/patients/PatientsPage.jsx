import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { searchPatients } from './api'
import RegisterPatientModal from './RegisterPatientModal'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'
import { Badge, CenteredSpinner, EmptyState, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'

export default function PatientsPage() {
  const [term, setTerm] = useState('')
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)

  const { data, isFetching, isError, error } = useQuery({
    queryKey: ['patients', 'search', query],
    queryFn: () => searchPatients(query),
    enabled: query.length > 0,
  })

  const submit = (e) => {
    e.preventDefault()
    setQuery(term.trim())
  }

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader
          title="Patients"
          subtitle="Search by name or phone, or register a new patient"
          action={<Button onClick={() => setOpen(true)}>+ Register patient</Button>}
        />
        <form onSubmit={submit} className="flex gap-2">
          <Input
            placeholder="Search name or phone…"
            value={term}
            onChange={(e) => setTerm(e.target.value)}
          />
          <Button type="submit" variant="secondary">
            Search
          </Button>
        </form>
      </Card>

      <Card>
        {query.length === 0 ? (
          <EmptyState title="Start by searching" hint="Type a name or phone number above." />
        ) : isFetching ? (
          <CenteredSpinner />
        ) : isError ? (
          <ErrorState message={apiErrorMessage(error)} />
        ) : !data || data.length === 0 ? (
          <EmptyState title="No patients found" hint={`No results for “${query}”.`} />
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
                {data.map((p) => (
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
