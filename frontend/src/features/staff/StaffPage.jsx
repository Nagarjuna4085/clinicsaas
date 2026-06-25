import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { listStaff } from './api'
import AddStaffModal from './AddStaffModal'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'
import { Badge, CenteredSpinner, EmptyState, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'
import { useAuthStore } from '../../store/auth'
import { ROLES } from '../../lib/constants'

const roleColor = {
  ADMIN: 'violet',
  DOCTOR: 'blue',
  RECEPTIONIST: 'green',
  NURSE: 'amber',
}

export default function StaffPage() {
  const [open, setOpen] = useState(false)
  const [filter, setFilter] = useState('')
  const role = useAuthStore((s) => s.role)
  const isAdmin = role === ROLES.ADMIN

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['staff'],
    queryFn: listStaff,
  })

  // Client-side search + pagination (no server endpoint for these).
  const [page, setPage] = useState(0)
  const pageSize = 8
  const filtered = useMemo(() => {
    const list = data || []
    const q = filter.trim().toLowerCase()
    if (!q) return list
    return list.filter(
      (s) =>
        s.name.toLowerCase().includes(q) ||
        s.phone.includes(q) ||
        s.role.toLowerCase().includes(q)
    )
  }, [data, filter])

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize))
  const current = Math.min(page, pageCount - 1)
  const rows = filtered.slice(current * pageSize, current * pageSize + pageSize)

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader
          title="Staff"
          subtitle="Doctors, receptionists and nurses in your clinic"
          action={
            isAdmin ? (
              <Button onClick={() => setOpen(true)}>+ Add staff</Button>
            ) : (
              <Badge>Read-only ({role})</Badge>
            )
          }
        />
        <Input
          placeholder="Filter by name, phone or role…"
          value={filter}
          onChange={(e) => {
            setFilter(e.target.value)
            setPage(0)
          }}
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
                  </tr>
                </thead>
                <tbody>
                  {rows.map((s) => (
                    <tr key={s.id} className="border-b border-slate-100 hover:bg-slate-50">
                      <td className="px-3 py-2 font-medium text-slate-700">{s.name}</td>
                      <td className="px-3 py-2">{s.phone}</td>
                      <td className="px-3 py-2">
                        <Badge color={roleColor[s.role]}>{s.role}</Badge>
                      </td>
                      <td className="px-3 py-2">{s.specialty || '—'}</td>
                      <td className="px-3 py-2">
                        {s.active ? <Badge color="green">Active</Badge> : <Badge color="red">Inactive</Badge>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {pageCount > 1 && (
              <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
                <span>
                  Page {current + 1} of {pageCount}
                </span>
                <div className="flex gap-2">
                  <Button variant="secondary" disabled={current === 0} onClick={() => setPage(current - 1)}>
                    Prev
                  </Button>
                  <Button
                    variant="secondary"
                    disabled={current >= pageCount - 1}
                    onClick={() => setPage(current + 1)}
                  >
                    Next
                  </Button>
                </div>
              </div>
            )}
          </>
        )}
      </Card>

      {isAdmin && <AddStaffModal open={open} onClose={() => setOpen(false)} />}
    </div>
  )
}
