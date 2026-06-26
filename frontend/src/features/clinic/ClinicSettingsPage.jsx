import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getClinic, updateClinic } from './api'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'
import { Badge, CenteredSpinner, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import { useAuthStore } from '../../store/auth'
import SubscriptionCard from '../subscription/SubscriptionCard'

const schema = z.object({
  clinicName: z.string().min(2, 'Clinic name is required'),
  city: z.string().optional(),
})

function ReadOnly({ label, value }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-slate-400">{label}</p>
      <p className="text-sm font-medium text-slate-700">{value || '—'}</p>
    </div>
  )
}

export default function ClinicSettingsPage() {
  const toast = useToast()
  const qc = useQueryClient()
  const { data, isLoading, isError, error } = useQuery({ queryKey: ['clinic'], queryFn: getClinic })

  const { register, handleSubmit, reset, formState: { errors } } = useForm({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (data) reset({ clinicName: data.clinicName || '', city: data.city || '' })
  }, [data, reset])

  const mutation = useMutation({
    mutationFn: updateClinic,
    onSuccess: (updated) => {
      toast.success('Clinic updated')
      useAuthStore.setState({ clinic: updated.clinicName })
      qc.invalidateQueries({ queryKey: ['clinic'] })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not update clinic')),
  })

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader title="Clinic settings" subtitle="Your clinic's profile and plan" />
        {isLoading ? (
          <CenteredSpinner />
        ) : isError ? (
          <ErrorState message={apiErrorMessage(error)} />
        ) : (
          <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="space-y-5">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Input label="Clinic name" error={errors.clinicName?.message} {...register('clinicName')} />
              <Input label="City" error={errors.city?.message} {...register('city')} />
            </div>
            <div className="grid grid-cols-2 gap-5 rounded-lg bg-slate-50 p-4 sm:grid-cols-4">
              <ReadOnly label="Owner phone" value={data.ownerPhone} />
              <div>
                <p className="text-xs uppercase tracking-wide text-slate-400">Plan</p>
                <Badge color="blue">{data.plan}</Badge>
              </div>
              <div>
                <p className="text-xs uppercase tracking-wide text-slate-400">Status</p>
                <Badge color={data.status === 'active' ? 'green' : 'amber'}>{data.status}</Badge>
              </div>
              <ReadOnly label="Trial ends" value={data.trialEndsAt ? new Date(data.trialEndsAt).toLocaleDateString() : '—'} />
            </div>
            <div className="flex justify-end">
              <Button type="submit" loading={mutation.isPending}>Save changes</Button>
            </div>
          </form>
        )}
      </Card>

      <SubscriptionCard />
    </div>
  )
}
