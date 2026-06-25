import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { listStaff, deactivateStaff } from './api'
import StaffModal from './StaffModal'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'
import { Badge, CenteredSpinner, EmptyState, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import { useAuthStore } from '../../store/auth'
import { ROLES } from '../../lib/constants'

const roleColor = { ADMIN: 'violet', DOCTOR: 'blue', RECEPTIONIST: 'green', NURSE: 'amber' }

export default function StaffPage() {
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [filter, setFilter] = useState('')
  const [page, setPage] = useState(0)
  const toast = useToast()
  const qc = useQueryClient()
  const role = useAuthStore((s) => s.role)
  const isAdmin = role === ROLES.ADMIN
  const pageSize = 8

  const { data, isLoading, isError, error } = useQuery({ queryKey: ['staff'], queryFn: listStaff })

  const deactivate = useMutation({
    mutationFn: (id) => deactivateStaff(id),
    onSuccess: () => {
      toast.success('Staff deactivated')
      qc.invalidateQueries({ queryKey: ['staff'] })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not deactivate')),
  })

  const filtered = useMemo(() => {
    const list = data || []
    const q = filter.trim().toLowerCase()
    if (!q) return list
    return list.filter(
      (s) => s.name.toLowerCase().includes(q) || s.phone.includes(q) || s.role.toLowerCase().includes(q)
    )
  }, [data, filter])

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize))
  const current = Math.min(page, pageCount - 1)
  const rows = filtered.slice(current * pageSize, current * pageSize + pageSize)

  const openAdd = () => { setEditing(null); setOpen(true) }
  const openEdit = (s) => { setEditing(s); setOpen(true) }
  const onDeactivate = (s) => {
    if (window.confirm(`Deactivate ${s.name}? They will no longer be able to log in.`)) {
      deactivate.mutate(s.id)
    }
  }

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader
          title="Staff"
          subtitle="Doctors, receptionists and nurses in your clinic"
          action={isAdmin ? <Button onClick={openAdd}>+ Add staff</Button> : <Badge>Read-only ({role})</Badge>}
        />
        <Input
          placeholder="Filter by name, phone or role…"
          value={filter}
          onChange={(e) => { setFilter(e.target.value); setPage(0) }}
        />
      </Card>

      <Card>
        {isLoading ? (
          <CenteredSpinner />
        ) : isError ? (
          <ErrorState message={apiErrorMessage(error)} />
        ) : filtered.length === 0 ? (
          <EmptyState title="No staff found" hint={filter ? 'Try a different search.' : 'Add your first staff member.'} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-200 text-left text-slate-500">
                    <th className="px-3 py-2">Name</th>
                    <th className="px-3 py-2">Phone</th>
                    <th className="px-3 py-2">Role</th>
                    <th className="px-3 py-2">Specialty</th>
                    <th className="px-3 py-2">Status</th>
                    {isAdmin && <th className="px-3 py-2 text-right">Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {rows.map((s) => (
                    <tr key={s.id} className="border-b border-slate-100 hover:bg-slate-50">
                      <td className="px-3 py-2 font-medium text-slate-700">{s.name}</td>
                      <td className="px-3 py-2">{s.phone}</td>
                      <td className="px-3 py-2"><Badge color={roleColor[s.role]}>{s.role}</Badge></td>
                      <td className="px-3 py-2">{s.specialty || '—'}</td>
                      <td className="px-3 py-2">
                        {s.active ? <Badge color="green">Active</Badge> : <Badge color="red">Inactive</Badge>}
                      </td>
                      {isAdmin && (
                        <td className="px-3 py-2">
                          <div className="flex justify-end gap-2">
                            <button className="text-brand-600 hover:underline" onClick={() => openEdit(s)}>Edit</button>
                            {s.active && (
                              <button className="text-red-600 hover:underline" onClick={() => onDeactivate(s)}>
                                Deactivate
                              </button>
                            )}
                          </div>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {pageCount > 1 && (
              <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
                <span>Page {current + 1} of {pageCount}</span>
                <div className="flex gap-2">
                  <Button variant="secondary" disabled={current === 0} onClick={() => setPage(current - 1)}>Prev</Button>
                  <Button variant="secondary" disabled={current >= pageCount - 1} onClick={() => setPage(current + 1)}>Next</Button>
                </div>
              </div>
            )}
          </>
        )}
      </Card>

      {isAdmin && <StaffModal open={open} staff={editing} onClose={() => { setOpen(false); setEditing(null) }} />}
    </div>
  )
}
