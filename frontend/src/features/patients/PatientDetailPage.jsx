import { useQuery } from '@tanstack/react-query'
import { useParams, Link } from 'react-router-dom'
import { getPatient } from './api'
import { Card, CardHeader } from '../../components/ui/Card'
import { Badge, CenteredSpinner, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'

function Detail({ label, value }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-slate-400">{label}</p>
      <p className="text-sm font-medium text-slate-700">{value || '—'}</p>
    </div>
  )
}

export default function PatientDetailPage() {
  const { id } = useParams()
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['patients', id],
    queryFn: () => getPatient(id),
  })

  return (
    <div className="space-y-4">
      <Link to="/patients" className="text-sm text-brand-600 hover:underline">
        ← Back to patients
      </Link>
      <Card>
        {isLoading ? (
          <CenteredSpinner />
        ) : isError ? (
          <ErrorState message={apiErrorMessage(error)} />
        ) : (
          <>
            <CardHeader
              title={data.name}
              subtitle={`UHID ${data.uhid}`}
              action={<Badge color="blue">{data.totalVisits} visits</Badge>}
            />
            <div className="grid grid-cols-2 gap-5 sm:grid-cols-3">
              <Detail label="Phone" value={data.phone} />
              <Detail label="Age" value={data.age} />
              <Detail label="Gender" value={data.gender} />
              <Detail label="Blood group" value={data.bloodGroup} />
              <Detail label="Allergies" value={data.allergies} />
            </div>
          </>
        )}
      </Card>
    </div>
  )
}
