import { useQuery, useMutation } from '@tanstack/react-query'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { getPatient, getPatientHistory, exportPatient, erasePatient } from './api'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Badge, CenteredSpinner, EmptyState, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import { useAuthStore } from '../../store/auth'
import { ROLES } from '../../lib/constants'

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
  const navigate = useNavigate()
  const toast = useToast()
  const isAdmin = useAuthStore((s) => s.role) === ROLES.ADMIN

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['patients', id],
    queryFn: () => getPatient(id),
  })

  const history = useQuery({
    queryKey: ['patients', id, 'history'],
    queryFn: () => getPatientHistory(id),
  })

  const onExport = async () => {
    try {
      const bundle = await exportPatient(id)
      const blob = new Blob([JSON.stringify(bundle, null, 2)], { type: 'application/json' })
      const href = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = href
      a.download = `patient-${data?.uhid || id}.json`
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(href)
      toast.success('Patient data exported')
    } catch (e) {
      toast.error(apiErrorMessage(e, 'Export failed'))
    }
  }

  const eraseMutation = useMutation({
    mutationFn: () => erasePatient(id),
    onSuccess: () => {
      toast.success('Patient personal data erased')
      navigate('/patients', { replace: true })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Erase failed')),
  })

  const onErase = () => {
    if (window.confirm('Erase this patient’s personal data? Clinical/billing records are kept for legal retention. This cannot be undone.')) {
      eraseMutation.mutate()
    }
  }

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

            {isAdmin && (
              <div className="mt-6 flex flex-wrap items-center gap-3 border-t border-slate-200 pt-4">
                <span className="text-sm text-slate-500">Data rights (DPDP):</span>
                <Button variant="secondary" onClick={onExport}>Export data</Button>
                <Button variant="danger" onClick={onErase} loading={eraseMutation.isPending}>
                  Erase personal data
                </Button>
              </div>
            )}
          </>
        )}
      </Card>

      <Card>
        <CardHeader title="Visit history" subtitle="Past appointments, newest first" />
        {history.isLoading ? (
          <CenteredSpinner />
        ) : history.isError ? (
          <ErrorState message={apiErrorMessage(history.error)} />
        ) : !history.data || history.data.length === 0 ? (
          <EmptyState title="No visits yet" hint="Booked appointments will appear here." />
        ) : (
          <ol className="relative ml-3 border-l border-slate-200">
            {history.data.map((v, i) => (
              <li key={i} className="mb-5 ml-5">
                <span className="absolute -left-[7px] mt-1 h-3 w-3 rounded-full border-2 border-white bg-brand-500" />
                <div className="flex flex-wrap items-center gap-2">
                  <span className="text-sm font-semibold text-slate-700">{v.visitDate || '—'}</span>
                  {v.tokenNumber != null && <Badge color="slate">Token {v.tokenNumber}</Badge>}
                  <Badge color={statusColor[v.status] || 'slate'}>{v.status}</Badge>
                  {v.visitType && <span className="text-xs text-slate-400">{v.visitType}</span>}
                </div>
                <div className="mt-1 text-sm text-slate-600">
                  {v.doctorName && <span>Dr. {v.doctorName}</span>}
                  {v.diagnosis && <span> · {v.diagnosis}</span>}
                  {v.invoiceNumber && (
                    <span className="text-slate-400">
                      {' '}· {v.invoiceNumber}
                      {v.billTotal != null ? ` (₹${Number(v.billTotal).toLocaleString('en-IN')})` : ''}
                    </span>
                  )}
                </div>
              </li>
            ))}
          </ol>
        )}
      </Card>
    </div>
  )
}

const statusColor = {
  WAITING: 'amber',
  CONSULTING: 'blue',
  COMPLETED: 'green',
  CANCELLED: 'red',
}
