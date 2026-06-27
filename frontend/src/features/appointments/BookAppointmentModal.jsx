import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { bookAppointment } from './api'
import { bookingSchema } from './schemas'
import { searchPatients } from '../patients/api'
import { listStaff } from '../staff/api'
import { apiErrorMessage } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import Modal from '../../components/ui/Modal'
import Button from '../../components/ui/Button'
import { Input, Select } from '../../components/ui/Field'

export default function BookAppointmentModal({ open, onClose }) {
  const toast = useToast()
  const qc = useQueryClient()
  const [term, setTerm] = useState('')
  const [query, setQuery] = useState('')
  const [patient, setPatient] = useState(null)
  const [scheduled, setScheduled] = useState(false)

  const {
    register,
    handleSubmit,
    setValue,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(bookingSchema),
    defaultValues: { visitType: 'WALKIN', opFee: 300, paymentMode: 'CASH' },
  })

  const patientResults = useQuery({
    queryKey: ['patients', 'search', query],
    queryFn: () => searchPatients(query),
    enabled: open && query.length > 0,
  })

  const doctors = useQuery({
    queryKey: ['staff'],
    queryFn: listStaff,
    enabled: open,
    // Providers = doctors, plus admins who are also practising (have a specialty).
    select: (list) => list.filter((s) => s.role === 'DOCTOR' || (s.role === 'ADMIN' && s.specialty)),
  })

  const mutation = useMutation({
    mutationFn: (values) =>
      bookAppointment({
        ...values,
        // Only send scheduledAt when scheduling for later; otherwise it's a walk-in.
        scheduledAt: scheduled && values.scheduledAt ? values.scheduledAt : undefined,
      }),
    onSuccess: (data) => {
      const msg = data.scheduledAt
        ? `Scheduled for ${new Date(data.scheduledAt).toLocaleString()}`
        : `Token #${data.tokenNumber} issued for ${data.patientName}`
      toast.success(msg)
      qc.invalidateQueries({ queryKey: ['appointments'] })
      handleClose()
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not book appointment')),
  })

  const handleClose = () => {
    reset()
    setPatient(null)
    setScheduled(false)
    setTerm('')
    setQuery('')
    onClose()
  }

  const pickPatient = (p) => {
    setPatient(p)
    setValue('patientId', p.id, { shouldValidate: true })
  }

  const clearPatient = () => {
    setPatient(null)
    setValue('patientId', '', { shouldValidate: false })
  }

  return (
    <Modal open={open} onClose={handleClose} title="Book appointment">
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="space-y-4">
        {/* Patient picker */}
        <div>
          <span className="mb-1 block text-sm font-medium text-slate-700">Patient</span>
          {patient ? (
            <div className="flex items-center justify-between rounded-lg border border-brand-200 bg-brand-50 px-3 py-2 text-sm">
              <span>
                <b>{patient.name}</b> · {patient.uhid} · {patient.phone || 'no phone'}
              </span>
              <button type="button" className="text-brand-600 hover:underline" onClick={clearPatient}>
                change
              </button>
            </div>
          ) : (
            <>
              <div className="flex gap-2">
                <Input
                  placeholder="Search patient by name/phone…"
                  value={term}
                  onChange={(e) => setTerm(e.target.value)}
                />
                <Button type="button" variant="secondary" onClick={() => setQuery(term.trim())}>
                  Search
                </Button>
              </div>
              {patientResults.data?.length > 0 && (
                <div className="mt-2 max-h-40 overflow-auto rounded-lg border border-slate-200">
                  {patientResults.data.map((p) => (
                    <button
                      type="button"
                      key={p.id}
                      onClick={() => pickPatient(p)}
                      className="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-slate-50"
                    >
                      <span>{p.name}</span>
                      <span className="text-slate-400">{p.uhid}</span>
                    </button>
                  ))}
                </div>
              )}
            </>
          )}
          {errors.patientId && <span className="mt-1 block text-xs text-red-600">{errors.patientId.message}</span>}
        </div>

        <Select label="Doctor" error={errors.doctorId?.message} {...register('doctorId')}>
          <option value="">Select doctor…</option>
          {doctors.data?.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name} {d.specialty ? `· ${d.specialty}` : ''}
            </option>
          ))}
        </Select>
        {doctors.data && doctors.data.length === 0 && (
          <p className="text-xs text-amber-600">
            No bookable providers yet. Add a doctor under Staff (or mark the owner as a practising doctor).
          </p>
        )}

        <div className="grid grid-cols-3 gap-3">
          <Select label="Visit type" {...register('visitType')}>
            <option value="WALKIN">Walk-in</option>
            <option value="SCHEDULED">Scheduled</option>
            <option value="FOLLOWUP">Follow-up</option>
          </Select>
          <Input label="OP fee (₹)" type="number" {...register('opFee')} />
          <Select label="Payment" {...register('paymentMode')}>
            <option value="CASH">Cash</option>
            <option value="UPI">UPI</option>
            <option value="CARD">Card</option>
          </Select>
        </div>

        <div>
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input type="checkbox" className="h-4 w-4 rounded border-slate-300"
              checked={scheduled} onChange={(e) => setScheduled(e.target.checked)} />
            Schedule for a later date/time
          </label>
          {scheduled && (
            <Input type="datetime-local" className="mt-2 max-w-xs" {...register('scheduledAt')} />
          )}
        </div>

        <div className="flex justify-end gap-2 pt-2">
          <Button variant="secondary" onClick={handleClose}>
            Cancel
          </Button>
          <Button type="submit" loading={mutation.isPending}>
            Book &amp; issue token
          </Button>
        </div>
      </form>
    </Modal>
  )
}
