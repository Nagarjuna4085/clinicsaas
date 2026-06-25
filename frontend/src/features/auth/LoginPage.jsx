import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { sendOtp, verifyOtp } from './api'
import { phoneSchema, otpSchema } from './schemas'
import { useAuthStore } from '../../store/auth'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'

export default function LoginPage() {
  const [step, setStep] = useState('phone') // 'phone' | 'otp'
  const [phone, setPhone] = useState('')
  const navigate = useNavigate()
  const toast = useToast()
  const setAuth = useAuthStore((s) => s.setAuth)

  const phoneForm = useForm({ resolver: zodResolver(phoneSchema) })
  const otpForm = useForm({ resolver: zodResolver(otpSchema) })

  const sendMutation = useMutation({
    mutationFn: (p) => sendOtp(p),
    onSuccess: (_data, p) => {
      setPhone(p)
      otpForm.reset({ phone: p, otp: '' })
      setStep('otp')
      toast.info('OTP sent — check the backend console for the code')
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not send OTP')),
  })

  const verifyMutation = useMutation({
    mutationFn: verifyOtp,
    onSuccess: (data) => {
      setAuth(data)
      toast.success(`Welcome, ${data.name}`)
      navigate('/', { replace: true })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Invalid or expired OTP')),
  })

  return (
    <div className="flex min-h-full items-center justify-center bg-gradient-to-br from-slate-900 to-brand-900 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl">
        <div className="mb-6 text-center">
          <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-xl font-bold text-white">
            C
          </div>
          <h1 className="text-2xl font-bold text-slate-800">ClinicFlow</h1>
          <p className="text-sm text-slate-500">Sign in with your phone</p>
        </div>

        {step === 'phone' ? (
          <form onSubmit={phoneForm.handleSubmit((v) => sendMutation.mutate(v.phone))} className="space-y-4">
            <Input
              label="Phone number"
              placeholder="9876543210"
              maxLength={10}
              error={phoneForm.formState.errors.phone?.message}
              {...phoneForm.register('phone')}
            />
            <Button type="submit" className="w-full" loading={sendMutation.isPending}>
              Send OTP
            </Button>
          </form>
        ) : (
          <form onSubmit={otpForm.handleSubmit((v) => verifyMutation.mutate(v))} className="space-y-4">
            <p className="text-sm text-slate-500">
              OTP sent to <span className="font-medium text-slate-700">{phone}</span>.{' '}
              <button
                type="button"
                className="text-brand-600 hover:underline"
                onClick={() => setStep('phone')}
              >
                Change
              </button>
            </p>
            <Input
              label="6-digit OTP"
              placeholder="123456"
              maxLength={6}
              error={otpForm.formState.errors.otp?.message}
              {...otpForm.register('otp')}
            />
            <Button type="submit" className="w-full" loading={verifyMutation.isPending}>
              Verify &amp; sign in
            </Button>
            <Button
              type="button"
              variant="ghost"
              className="w-full"
              onClick={() => sendMutation.mutate(phone)}
              loading={sendMutation.isPending}
            >
              Resend OTP
            </Button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-slate-500">
          New clinic?{' '}
          <Link to="/signup" className="font-medium text-brand-600 hover:underline">
            Register here
          </Link>
        </p>
      </div>
    </div>
  )
}
