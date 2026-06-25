import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getTodayQueue, getDoctorQueue, updateAppointmentStatus } from './api'
import BookAppointmentModal from './BookAppointmentModal'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Badge, CenteredSpinner, EmptyState, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import { useAuthStore } from '../../store/auth'
import { ROLES } from '../../lib/constants'

const statusColor = {
  WAITING: 'amber',
  CONSULTING: 'blue',
  COMPLETED: 'green',
  CANCELLED: 'red',
}

const nextActions = {
  WAITING: [['CONSULTING', 'Start']],
  CONSULTING: [['COMPLETED', 'Complete']],
  COMPLETED: [],
  CANCELLED: [],
}

export default function AppointmentsPage() {
  const [open, setOpen] = useState(false)
  const toast = useToast()
  const qc = useQueryClient()
  const role = useAuthStore((s) => s.role)
  const staffId = useAuthStore((s) => s.staffId)
  const canBook = role === ROLES.ADMIN || role === ROLES.RECEPTIONIST
  const canConsult = [ROLES.ADMIN, ROLES.DOCTOR, ROLES.NURSE].includes(role)
  const isDoctor = role === ROLES.DOCTOR

  // Doctors default to their own queue; admin/reception/nurse see the full queue.
  const [scope, setScope] = useState(isDoctor ? 'mine' : 'all')
  const mine = isDoctor && scope === 'mine' && staffId

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['appointments', mine ? `doctor:${staffId}` : 'today'],
    queryFn: () => (mine ? getDoctorQueue(staffId) : getTodayQueue()),
  })

  const statusMutation = useMutation({
    mutationFn: ({ id, status }) => updateAppointmentStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['appointments'] })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not update status')),
  })

  const counts = (data || []).reduce((acc, a) => {
    acc[a.status] = (acc[a.status] || 0) + 1
    return acc
  }, {})

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader
          title={mine ? 'My queue today' : "Today's queue"}
          subtitle="Live token board for today"
          action={
            <div className="flex items-center gap-2">
              {isDoctor && (
                <div className="inline-flex overflow-hidden rounded-lg border border-slate-300 text-sm">
                  <button
                    className={`px-3 py-1.5 ${scope === 'mine' ? 'bg-brand-600 text-white' : 'bg-white text-slate-600'}`}
                    onClick={() => setScope('mine')}
                  >
                    My queue
                  </button>
                  <button
                    className={`px-3 py-1.5 ${scope === 'all' ? 'bg-brand-600 text-white' : 'bg-white text-slate-600'}`}
                    onClick={() => setScope('all')}
                  >
                    All
                  </button>
                </div>
              )}
              {canBook && <Button onClick={() => setOpen(true)}>+ Book appointment</Button>}
            </div>
          }
        />
        <div className="flex flex-wrap gap-2">
          {['WAITING', 'CONSULTING', 'COMPLETED', 'CANCELLED'].map((s) => (
            <Badge key={s} color={statusColor[s]}>
              {s}: {counts[s] || 0}
            </Badge>
          ))}
        </div>
      </Card>

      <Card>
        {isLoading ? (
          <CenteredSpinner />
        ) : isError ? (
          <ErrorState message={apiErrorMessage(error)} />
        ) : !data || data.length === 0 ? (
          <EmptyState title="No appointments today" hint={canBook ? 'Book the first patient.' : 'Nothing in the queue yet.'} />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-left text-slate-500">
                  <th className="px-3 py-2">Token</th>
                  <th className="px-3 py-2">Patient</th>
                  <th className="px-3 py-2">Age/Sex</th>
                  <th className="px-3 py-2">Type</th>
                  <th className="px-3 py-2">Status</th>
                  <th className="px-3 py-2 text-right">Action</th>
                </tr>
              </thead>
              <tbody>
                {data.map((a) => (
                  <tr key={a.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-3 py-2">
                      <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-slate-900 text-xs font-bold text-white">
                        {a.tokenNumber}
                      </span>
                    </td>
                    <td className="px-3 py-2 font-medium text-slate-700">{a.patientName}</td>
                    <td className="px-3 py-2">
                      {a.age ?? '—'} / {a.gender || '—'}
                    </td>
                    <td className="px-3 py-2">{a.visitType}</td>
                    <td className="px-3 py-2">
                      <Badge color={statusColor[a.status]}>{a.status}</Badge>
                    </td>
                    <td className="px-3 py-2">
                      <div className="flex items-center justify-end gap-2">
                        {canConsult && (
                          <Link
                            to={`/appointments/${a.id}/consult`}
                            state={{ appointment: a }}
                            className="rounded-lg px-2 py-1 text-sm font-medium text-brand-600 hover:bg-brand-50"
                          >
                            Consult
                          </Link>
                        )}
                        {nextActions[a.status]?.map(([status, label]) => (
                          <Button
                            key={status}
                            variant="secondary"
                            onClick={() => statusMutation.mutate({ id: a.id, status })}
                            loading={statusMutation.isPending && statusMutation.variables?.id === a.id}
                          >
                            {label}
                          </Button>
                        ))}
                        {(a.status === 'WAITING' || a.status === 'CONSULTING') && (
                          <Button
                            variant="ghost"
                            className="text-red-600"
                            onClick={() => statusMutation.mutate({ id: a.id, status: 'CANCELLED' })}
                          >
                            Cancel
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {canBook && <BookAppointmentModal open={open} onClose={() => setOpen(false)} />}
    </div>
  )
}
