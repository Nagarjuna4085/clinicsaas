import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { addStaff } from './api'
import { staffSchema } from './schemas'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import Modal from '../../components/ui/Modal'
import Button from '../../components/ui/Button'
import { Input, Select } from '../../components/ui/Field'

export default function AddStaffModal({ open, onClose }) {
  const toast = useToast()
  const qc = useQueryClient()
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm({ resolver: zodResolver(staffSchema), defaultValues: { role: 'RECEPTIONIST' } })

  const mutation = useMutation({
    mutationFn: (values) => {
      const payload = Object.fromEntries(
        Object.entries(values).filter(([, v]) => v !== '' && v !== undefined)
      )
      return addStaff(payload)
    },
    onSuccess: (data) => {
      toast.success(`${data.name} added as ${data.role}`)
      qc.invalidateQueries({ queryKey: ['staff'] })
      reset()
      onClose()
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not add staff')),
  })

  const role = watch('role')

  return (
    <Modal open={open} onClose={onClose} title="Add staff member">
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid grid-cols-2 gap-4">
        <div className="col-span-2">
          <Input label="Full name" error={errors.name?.message} {...register('name')} />
        </div>
        <Input label="Phone" maxLength={10} error={errors.phone?.message} {...register('phone')} />
        <Select label="Role" error={errors.role?.message} {...register('role')}>
          <option value="RECEPTIONIST">Receptionist</option>
          <option value="DOCTOR">Doctor</option>
          <option value="NURSE">Nurse</option>
          <option value="ADMIN">Admin</option>
        </Select>
        {role === 'DOCTOR' && (
          <>
            <Input label="NMC reg. number" error={errors.regNumber?.message} {...register('regNumber')} />
            <Input label="Specialty" error={errors.specialty?.message} {...register('specialty')} />
          </>
        )}
        <div className="col-span-2 flex justify-end gap-2 pt-2">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={mutation.isPending}>
            Add staff
          </Button>
        </div>
      </form>
    </Modal>
  )
}
