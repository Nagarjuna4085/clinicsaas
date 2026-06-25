import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { registerClinic } from './api'
import { signupSchema } from './schemas'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import Button from '../../components/ui/Button'
import { Input, Select } from '../../components/ui/Field'

export default function SignupPage() {
  const navigate = useNavigate()
  const toast = useToast()
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(signupSchema),
    defaultValues: { plan: 'starter', role: 'ADMIN' },
  })

  const mutation = useMutation({
    mutationFn: registerClinic,
    onSuccess: (data) => {
      toast.success(`${data.clinicName} registered — log in with the owner phone`)
      navigate('/login', { replace: true })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Registration failed')),
  })

  const role = watch('role')

  return (
    <div className="flex min-h-full items-center justify-center bg-gradient-to-br from-slate-900 to-brand-900 p-4">
      <div className="w-full max-w-xl rounded-2xl bg-white p-8 shadow-xl">
        <h1 className="text-2xl font-bold text-slate-800">Register your clinic</h1>
        <p className="mb-6 text-sm text-slate-500">
          Provisions a dedicated, isolated workspace for your clinic.
        </p>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input label="Clinic name" error={errors.clinicName?.message} {...register('clinicName')} />
          <Input label="City" error={errors.city?.message} {...register('city')} />
          <Input label="Owner name" error={errors.ownerName?.message} {...register('ownerName')} />
          <Input
            label="Owner phone"
            placeholder="9876543210"
            maxLength={10}
            error={errors.ownerPhone?.message}
            {...register('ownerPhone')}
          />
          <Select label="Plan" error={errors.plan?.message} {...register('plan')}>
            <option value="starter">Starter</option>
            <option value="clinic">Clinic</option>
            <option value="pro">Pro</option>
            <option value="hospital">Hospital</option>
          </Select>
          <Select label="Owner role" error={errors.role?.message} {...register('role')}>
            <option value="ADMIN">Admin</option>
            <option value="DOCTOR">Doctor</option>
            <option value="RECEPTIONIST">Receptionist</option>
            <option value="NURSE">Nurse</option>
          </Select>
          {role === 'DOCTOR' && (
            <>
              <Input label="NMC reg. number" error={errors.regNumber?.message} {...register('regNumber')} />
              <Input label="Specialty" error={errors.specialty?.message} {...register('specialty')} />
            </>
          )}
          <div className="sm:col-span-2">
            <Button type="submit" className="w-full" loading={mutation.isPending}>
              Create clinic
            </Button>
          </div>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          Already registered?{' '}
          <Link to="/login" className="font-medium text-brand-600 hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
