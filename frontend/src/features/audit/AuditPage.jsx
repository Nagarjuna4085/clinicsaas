import { useQuery } from '@tanstack/react-query'
import { getAudit } from './api'
import { Card, CardHeader } from '../../components/ui/Card'
import { Badge, CenteredSpinner, EmptyState, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'

const actionColor = {
  VIEW: 'slate',
  CREATE: 'green',
  UPDATE: 'blue',
  EXPORT: 'violet',
  DELETE: 'red',
}

const fmt = (s) => (s ? new Date(s).toLocaleString() : '—')

export default function AuditPage() {
  const { data, isLoading, isError, error } = useQuery({ queryKey: ['audit'], queryFn: getAudit })

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader title="Audit log" subtitle="Who viewed or changed records (latest 200)" />
      </Card>
      <Card>
        {isLoading ? (
          <CenteredSpinner />
        ) : isError ? (
          <ErrorState message={apiErrorMessage(error)} />
        ) : !data || data.length === 0 ? (
          <EmptyState title="No activity yet" hint="Actions on patient records will appear here." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-left text-slate-500">
                  <th className="px-3 py-2">When</th>
                  <th className="px-3 py-2">Action</th>
                  <th className="px-3 py-2">Entity</th>
                  <th className="px-3 py-2">By</th>
                </tr>
              </thead>
              <tbody>
                {data.map((e, i) => (
                  <tr key={i} className="border-b border-slate-100">
                    <td className="px-3 py-2 text-slate-500">{fmt(e.loggedAt)}</td>
                    <td className="px-3 py-2"><Badge color={actionColor[e.action] || 'slate'}>{e.action}</Badge></td>
                    <td className="px-3 py-2">
                      {e.entityType} <span className="font-mono text-xs text-slate-400">{e.entityId?.slice(0, 8)}</span>
                    </td>
                    <td className="px-3 py-2">
                      <span className="font-mono text-xs">{e.actor?.slice(0, 8)}</span>{' '}
                      <span className="text-slate-400">{e.actorRole}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  )
}
