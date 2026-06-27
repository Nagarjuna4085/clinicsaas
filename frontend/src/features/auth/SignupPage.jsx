import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { registerClinic, sendOtp, checkPhoneAvailable } from './api'
import { signupDetailsSchema, signupVerifySchema } from './schemas'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import Button from '../../components/ui/Button'
import { Input, Select } from '../../components/ui/Field'

export default function SignupPage() {
  const [step, setStep] = useState('details') // 'details' | 'verify'
  const [details, setDetails] = useState(null)
  const navigate = useNavigate()
  const toast = useToast()

  const detailsForm = useForm({
    resolver: zodResolver(signupDetailsSchema),
    defaultValues: { plan: 'starter', alsoDoctor: false, consent: false },
  })
  const verifyForm = useForm({ resolver: zodResolver(signupVerifySchema) })
  const alsoDoctor = detailsForm.watch('alsoDoctor')

  const otpMutation = useMutation({
    mutationFn: async (values) => {
      // Fail fast if the phone is already tied to a clinic — before wasting an OTP.
      const { available } = await checkPhoneAvailable(values.ownerPhone)
      if (!available) {
        throw new Error('This phone is already registered with a clinic. Please use a different number.')
      }
      return sendOtp(values.ownerPhone)
    },
    onSuccess: (data, values) => {
      setDetails(values)
      verifyForm.reset({ otp: data?.devOtp || '', password: '' })
      setStep('verify')
      toast.info(data?.devOtp ? `Dev OTP ${data.devOtp} (prefilled)` : 'OTP sent to the owner phone')
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not send OTP')),
  })

  const registerMutation = useMutation({
    mutationFn: (v) => registerClinic({ ...details, otp: v.otp, password: v.password }),
    onSuccess: (data) => {
      toast.success(`${data.clinicName} registered — sign in with your phone & password`)
      navigate('/login', { replace: true })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Registration failed')),
  })

  return (
    <div className="flex min-h-full items-center justify-center bg-gradient-to-br from-slate-900 to-brand-900 p-4">
      <div className="w-full max-w-xl rounded-2xl bg-white p-8 shadow-xl">
        <h1 className="text-2xl font-bold text-slate-800">Register your clinic</h1>
        <p className="mb-6 text-sm text-slate-500">
          {step === 'details'
            ? 'Step 1 of 2 — clinic & owner details'
            : 'Step 2 of 2 — verify phone and set your password'}
        </p>

        {step === 'details' ? (
          <form onSubmit={detailsForm.handleSubmit((v) => otpMutation.mutate(v))}
                className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input label="Clinic name" error={detailsForm.formState.errors.clinicName?.message} {...detailsForm.register('clinicName')} />
            <Input label="City" error={detailsForm.formState.errors.city?.message} {...detailsForm.register('city')} />
            <Input label="Owner name" error={detailsForm.formState.errors.ownerName?.message} {...detailsForm.register('ownerName')} />
            <Input label="Owner phone" placeholder="9876543210" maxLength={10}
              error={detailsForm.formState.errors.ownerPhone?.message} {...detailsForm.register('ownerPhone')} />
            <Select label="Plan" {...detailsForm.register('plan')}>
              <option value="starter">Starter</option>
              <option value="clinic">Clinic</option>
              <option value="pro">Pro</option>
              <option value="hospital">Hospital</option>
            </Select>
            <div className="sm:col-span-2">
              <label className="flex items-center gap-2 text-sm text-slate-700">
                <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...detailsForm.register('alsoDoctor')} />
                I'm also a practising doctor (makes me bookable for consultations)
              </label>
            </div>
            {alsoDoctor && (
              <>
                <Input label="NMC reg. number" {...detailsForm.register('regNumber')} />
                <Input label="Specialty" placeholder="e.g. General Physician" {...detailsForm.register('specialty')} />
              </>
            )}
            <div className="sm:col-span-2">
              <label className="flex items-start gap-2 text-sm text-slate-700">
                <input type="checkbox" className="mt-0.5 h-4 w-4 rounded border-slate-300" {...detailsForm.register('consent')} />
                <span>I agree to the Terms of Service and Privacy Policy, and I'm authorised to register this clinic.</span>
              </label>
              {detailsForm.formState.errors.consent && (
                <p className="mt-1 text-xs text-red-600">{detailsForm.formState.errors.consent.message}</p>
              )}
            </div>
            <div className="sm:col-span-2">
              <Button type="submit" className="w-full" loading={otpMutation.isPending}>Send OTP</Button>
            </div>
          </form>
        ) : (
          <form onSubmit={verifyForm.handleSubmit((v) => registerMutation.mutate(v))} className="space-y-4">
            <p className="text-sm text-slate-500">
              OTP sent to <span className="font-medium text-slate-700">{details?.ownerPhone}</span>.{' '}
              <button type="button" className="text-brand-600 hover:underline" onClick={() => setStep('details')}>Change</button>
            </p>
            <Input label="6-digit OTP" maxLength={6} error={verifyForm.formState.errors.otp?.message} {...verifyForm.register('otp')} />
            <Input label="Choose a password" type="password" error={verifyForm.formState.errors.password?.message} {...verifyForm.register('password')} />
            <Button type="submit" className="w-full" loading={registerMutation.isPending}>Create clinic</Button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-slate-500">
          Already registered?{' '}
          <Link to="/login" className="font-medium text-brand-600 hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
