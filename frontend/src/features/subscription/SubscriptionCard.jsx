import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getSubscription, subscribe, devSetStatus } from './api'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Select } from '../../components/ui/Field'
import { Badge, CenteredSpinner } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'

const statusColor = { active: 'green', trial: 'blue', suspended: 'red' }

export default function SubscriptionCard() {
  const toast = useToast()
  const qc = useQueryClient()
  const [plan, setPlan] = useState('clinic')

  const { data, isLoading } = useQuery({ queryKey: ['subscription'], queryFn: getSubscription })

  const mutation = useMutation({
    mutationFn: () => subscribe(plan),
    onSuccess: (d) => {
      if (d.shortUrl) window.open(d.shortUrl, '_blank', 'noopener')
      toast.success('Checkout opened — complete payment to activate')
      qc.invalidateQueries({ queryKey: ['subscription'] })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not start subscription')),
  })

  const devMutation = useMutation({
    mutationFn: (status) => devSetStatus(status),
    onSuccess: (d) => {
      toast.success(`Status set to ${d.status}`)
      qc.invalidateQueries({ queryKey: ['subscription'] })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not change status')),
  })

  return (
    <Card>
      <CardHeader
        title="Subscription"
        subtitle="Plan & billing"
        action={data ? <Badge color={statusColor[data.status] || 'slate'}>{data.status}</Badge> : null}
      />
      {isLoading ? (
        <CenteredSpinner />
      ) : (
        <div className="space-y-4">
          {data?.status === 'suspended' && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              Your subscription is inactive. Subscribe to restore full access.
            </div>
          )}
          {data?.status === 'trial' && data?.trialEndsAt && (
            <p className="text-sm text-slate-500">
              Free trial ends {new Date(data.trialEndsAt).toLocaleDateString()}.
            </p>
          )}
          <div className="flex items-end gap-3">
            <Select label="Plan" value={plan} onChange={(e) => setPlan(e.target.value)} className="max-w-xs">
              <option value="starter">Starter</option>
              <option value="clinic">Clinic</option>
              <option value="pro">Pro</option>
              <option value="hospital">Hospital</option>
            </Select>
            <Button onClick={() => mutation.mutate()} loading={mutation.isPending}>
              {data?.status === 'active' ? 'Change plan' : 'Subscribe'}
            </Button>
          </div>
          <p className="text-xs text-slate-400">
            Opens Razorpay checkout. Activation happens automatically once payment is confirmed.
          </p>

          {data?.devTools && (
            <div className="mt-2 rounded-lg border border-dashed border-amber-300 bg-amber-50 p-3">
              <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-amber-700">
                Dev tools — simulate webhook
              </p>
              <div className="flex flex-wrap gap-2">
                <Button variant="secondary" onClick={() => devMutation.mutate('active')} loading={devMutation.isPending}>
                  Activate
                </Button>
                <Button variant="secondary" onClick={() => devMutation.mutate('suspended')} loading={devMutation.isPending}>
                  Suspend
                </Button>
                <Button variant="secondary" onClick={() => devMutation.mutate('trial')} loading={devMutation.isPending}>
                  Reset to trial
                </Button>
              </div>
            </div>
          )}
        </div>
      )}
    </Card>
  )
}
