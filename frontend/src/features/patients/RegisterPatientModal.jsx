import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { registerPatient } from './api'
import { patientSchema } from './schemas'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import Modal from '../../components/ui/Modal'
import Button from '../../components/ui/Button'
import { Input, Select } from '../../components/ui/Field'

export default function RegisterPatientModal({ open, onClose, onCreated }) {
  const toast = useToast()
  const qc = useQueryClient()
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: zodResolver(patientSchema), defaultValues: { consent: false } })

  const mutation = useMutation({
    mutationFn: (values) => {
      // Drop empty strings so optional fields are sent as null/undefined.
      const payload = Object.fromEntries(
        Object.entries(values).filter(([, v]) => v !== '' && v !== undefined)
      )
      return registerPatient(payload)
    },
    onSuccess: (data) => {
      toast.success(`Patient ${data.name} (${data.uhid}) saved`)
      qc.invalidateQueries({ queryKey: ['patients'] })
      reset()
      onCreated?.(data)
      onClose()
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not save patient')),
  })

  return (
    <Modal open={open} onClose={onClose} title="Register patient">
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid grid-cols-2 gap-4">
        <div className="col-span-2">
          <Input label="Full name" error={errors.name?.message} {...register('name')} />
        </div>
        <Input label="Phone" maxLength={10} error={errors.phone?.message} {...register('phone')} />
        <Input label="Age" type="number" error={errors.age?.message} {...register('age')} />
        <Select label="Gender" error={errors.gender?.message} {...register('gender')}>
          <option value="">—</option>
          <option value="M">Male</option>
          <option value="F">Female</option>
          <option value="O">Other</option>
        </Select>
        <Input label="Blood group" error={errors.bloodGroup?.message} {...register('bloodGroup')} />
        <Input label="ABHA ID" error={errors.abhaId?.message} {...register('abhaId')} />
        <div className="col-span-2">
          <Input label="Allergies" error={errors.allergies?.message} {...register('allergies')} />
        </div>
        <div className="col-span-2">
          <label className="flex items-start gap-2 text-sm text-slate-700">
            <input type="checkbox" className="mt-0.5 h-4 w-4 rounded border-slate-300" {...register('consent')} />
            <span>Patient consents to storing their health information for treatment and records.</span>
          </label>
          {errors.consent && <p className="mt-1 text-xs text-red-600">{errors.consent.message}</p>}
        </div>
        <div className="col-span-2 flex justify-end gap-2 pt-2">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={mutation.isPending}>
            Save patient
          </Button>
        </div>
      </form>
    </Modal>
  )
}
