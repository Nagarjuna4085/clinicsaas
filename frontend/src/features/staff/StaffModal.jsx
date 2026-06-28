import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { addStaff, updateStaff } from './api'
import { staffSchema } from './schemas'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import Modal from '../../components/ui/Modal'
import Button from '../../components/ui/Button'
import { Input, Select } from '../../components/ui/Field'

const BLANK = { name: '', phone: '', role: 'RECEPTIONIST', regNumber: '', specialty: '' }

// Handles both create (no `staff`) and edit (`staff` provided).
export default function StaffModal({ open, onClose, staff }) {
  const toast = useToast()
  const qc = useQueryClient()
  const isEdit = !!staff
  const [created, setCreated] = useState(null) // holds {name, phone, temporaryPassword}

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm({ resolver: zodResolver(staffSchema), defaultValues: BLANK })

  useEffect(() => {
    if (open) {
      setCreated(null)
      reset(staff ? { ...BLANK, ...staff } : BLANK)
    }
  }, [open, staff, reset])

  const mutation = useMutation({
    mutationFn: (values) => {
      const payload = Object.fromEntries(
        Object.entries(values).filter(([, v]) => v !== '' && v !== undefined)
      )
      if (isEdit) {
        // phone is fixed on edit
        const { phone, ...rest } = payload
        return updateStaff(staff.id, rest)
      }
      return addStaff(payload)
    },
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ['staff'] })
      if (isEdit) {
        toast.success(`${data.name} updated`)
        onClose()
      } else {
        // Show the one-time temp password for the admin to share.
        setCreated(data)
        reset(BLANK)
      }
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not save staff')),
  })

  const role = watch('role')

  if (created) {
    return (
      <Modal open={open} onClose={onClose} title="Staff added">
        <div className="space-y-4">
          <p className="text-sm text-slate-600">
            <span className="font-medium">{created.name}</span> ({created.phone}) was added. Share this
            one-time password — they’ll set their own on first login.
          </p>
          <div className="rounded-lg border border-amber-300 bg-amber-50 p-4 text-center">
            <p className="text-xs uppercase tracking-wide text-amber-700">Temporary password</p>
            <p className="mt-1 select-all font-mono text-lg font-bold text-slate-800">
              {created.temporaryPassword}
            </p>
          </div>
          <div className="flex justify-end">
            <Button onClick={onClose}>Done</Button>
          </div>
        </div>
      </Modal>
    )
  }

  return (
    <Modal open={open} onClose={onClose} title={isEdit ? 'Edit staff member' : 'Add staff member'}>
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <Input label="Full name" error={errors.name?.message} {...register('name')} />
        </div>
        <Input label="Phone" maxLength={10} disabled={isEdit} error={errors.phone?.message} {...register('phone')} />
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
        <div className="flex justify-end gap-2 pt-2 sm:col-span-2">
          <Button variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" loading={mutation.isPending}>{isEdit ? 'Save changes' : 'Add staff'}</Button>
        </div>
      </form>
    </Modal>
  )
}
