import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { sendOtp, resetPassword } from './api'
import { forgotPhoneSchema, resetPasswordSchema } from './schemas'
import { useAuthStore } from '../../store/auth'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'

export default function ForgotPasswordPage() {
  const [step, setStep] = useState('phone') // 'phone' | 'reset'
  const [phone, setPhone] = useState('')
  const navigate = useNavigate()
  const toast = useToast()
  const setAuth = useAuthStore((s) => s.setAuth)

  const phoneForm = useForm({ resolver: zodResolver(forgotPhoneSchema) })
  const resetForm = useForm({ resolver: zodResolver(resetPasswordSchema) })

  const otpMutation = useMutation({
    mutationFn: (v) => sendOtp(v.phone),
    onSuccess: (data, v) => {
      setPhone(v.phone)
      resetForm.reset({ otp: data?.devOtp || '', newPassword: '' })
      setStep('reset')
      toast.info(data?.devOtp ? `Dev OTP ${data.devOtp} (prefilled)` : 'OTP sent')
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not send OTP')),
  })

  const resetMutation = useMutation({
    mutationFn: (v) => resetPassword({ phone, otp: v.otp, newPassword: v.newPassword }),
    onSuccess: (data) => {
      setAuth(data)
      toast.success('Password reset — you are signed in')
      navigate('/', { replace: true })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not reset password')),
  })

  return (
    <div className="flex min-h-full items-center justify-center bg-gradient-to-br from-slate-900 to-brand-900 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl">
        <h1 className="text-2xl font-bold text-slate-800">Reset password</h1>
        <p className="mb-6 text-sm text-slate-500">
          {step === 'phone' ? 'We’ll send an OTP to your phone' : `Enter the OTP sent to ${phone}`}
        </p>

        {step === 'phone' ? (
          <form onSubmit={phoneForm.handleSubmit((v) => otpMutation.mutate(v))} className="space-y-4">
            <Input label="Phone" placeholder="9876543210" maxLength={10}
              error={phoneForm.formState.errors.phone?.message} {...phoneForm.register('phone')} />
            <Button type="submit" className="w-full" loading={otpMutation.isPending}>Send OTP</Button>
          </form>
        ) : (
          <form onSubmit={resetForm.handleSubmit((v) => resetMutation.mutate(v))} className="space-y-4">
            <Input label="6-digit OTP" maxLength={6} error={resetForm.formState.errors.otp?.message} {...resetForm.register('otp')} />
            <Input label="New password" type="password" error={resetForm.formState.errors.newPassword?.message} {...resetForm.register('newPassword')} />
            <Button type="submit" className="w-full" loading={resetMutation.isPending}>Reset &amp; sign in</Button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-slate-500">
          <Link to="/login" className="font-medium text-brand-600 hover:underline">Back to sign in</Link>
        </p>
      </div>
    </div>
  )
}
