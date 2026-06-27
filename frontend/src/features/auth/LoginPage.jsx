import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { login, setPassword } from './api'
import { loginSchema, setPasswordSchema } from './schemas'
import { useAuthStore } from '../../store/auth'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'

export default function LoginPage() {
  const [mode, setMode] = useState('login') // 'login' | 'setPassword'
  const [phone, setPhone] = useState('')
  const navigate = useNavigate()
  const toast = useToast()
  const setAuth = useAuthStore((s) => s.setAuth)

  const loginForm = useForm({ resolver: zodResolver(loginSchema) })
  const resetForm = useForm({ resolver: zodResolver(setPasswordSchema) })

  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: (data, vars) => {
      if (data.mustReset) {
        setPhone(vars.phone)
        resetForm.reset({ currentPassword: vars.password, newPassword: '' })
        setMode('setPassword')
        toast.info('First login — please set a new password')
      } else {
        setAuth(data)
        toast.success(`Welcome, ${data.name}`)
        navigate('/', { replace: true })
      }
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Login failed')),
  })

  const resetMutation = useMutation({
    mutationFn: (v) => setPassword({ phone, currentPassword: v.currentPassword, newPassword: v.newPassword }),
    onSuccess: (data) => {
      setAuth(data)
      toast.success('Password set — you are signed in')
      navigate('/', { replace: true })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not set password')),
  })

  return (
    <div className="flex min-h-full items-center justify-center bg-gradient-to-br from-slate-900 to-brand-900 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl">
        <div className="mb-6 text-center">
          <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-xl font-bold text-white">
            C
          </div>
          <h1 className="text-2xl font-bold text-slate-800">ClinicFlow</h1>
          <p className="text-sm text-slate-500">
            {mode === 'login' ? 'Sign in to your clinic' : 'Set a new password'}
          </p>
        </div>

        {mode === 'login' ? (
          <form onSubmit={loginForm.handleSubmit((v) => loginMutation.mutate(v))} className="space-y-4">
            <Input label="Phone" placeholder="9876543210" maxLength={10}
              error={loginForm.formState.errors.phone?.message} {...loginForm.register('phone')} />
            <Input label="Password" type="password"
              error={loginForm.formState.errors.password?.message} {...loginForm.register('password')} />
            <Button type="submit" className="w-full" loading={loginMutation.isPending}>Sign in</Button>
            <div className="text-right">
              <Link to="/forgot" className="text-sm text-brand-600 hover:underline">Forgot password?</Link>
            </div>
          </form>
        ) : (
          <form onSubmit={resetForm.handleSubmit((v) => resetMutation.mutate(v))} className="space-y-4">
            <p className="text-sm text-slate-500">For <span className="font-medium text-slate-700">{phone}</span></p>
            <Input label="Temporary password" type="password"
              error={resetForm.formState.errors.currentPassword?.message} {...resetForm.register('currentPassword')} />
            <Input label="New password" type="password"
              error={resetForm.formState.errors.newPassword?.message} {...resetForm.register('newPassword')} />
            <Button type="submit" className="w-full" loading={resetMutation.isPending}>Set password &amp; sign in</Button>
            <Button type="button" variant="ghost" className="w-full" onClick={() => setMode('login')}>Back</Button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-slate-500">
          New clinic?{' '}
          <Link to="/signup" className="font-medium text-brand-600 hover:underline">Register here</Link>
        </p>

        <div className="mt-6 flex flex-wrap justify-center gap-x-4 gap-y-1 border-t border-slate-100 pt-4 text-xs text-slate-400">
          <Link to="/pricing" className="hover:text-brand-600">Pricing</Link>
          <Link to="/privacy" className="hover:text-brand-600">Privacy</Link>
          <Link to="/terms" className="hover:text-brand-600">Terms</Link>
          <Link to="/refund" className="hover:text-brand-600">Refunds</Link>
          <Link to="/contact" className="hover:text-brand-600">Contact</Link>
        </div>
      </div>
    </div>
  )
}
