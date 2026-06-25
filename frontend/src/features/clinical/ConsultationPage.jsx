import { useEffect } from 'react'
import { useForm, useFieldArray } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useParams, useLocation, Link } from 'react-router-dom'
import { getVitals, saveVitals, getPrescription, savePrescription, sendPrescriptionWhatsapp } from './api'
import { vitalsSchema, prescriptionSchema } from './schemas'
import { Card, CardHeader } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { Input, Textarea } from '../../components/ui/Field'
import { Badge, CenteredSpinner } from '../../components/ui/Misc'
import { apiErrorMessage, downloadPdf } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'
import { useAuthStore } from '../../store/auth'
import { ROLES } from '../../lib/constants'

const EMPTY_MED = { medicineName: '', dosage: '', frequency: '', duration: '', instructions: '' }

export default function ConsultationPage() {
  const { id } = useParams()
  const { state } = useLocation()
  const appt = state?.appointment

  return (
    <div className="space-y-4">
      <Link to="/appointments" className="text-sm text-brand-600 hover:underline">
        ← Back to queue
      </Link>
      <Card>
        <CardHeader
          title={appt ? `Token #${appt.tokenNumber} · ${appt.patientName}` : 'Consultation'}
          subtitle={appt ? `${appt.age ?? '—'} / ${appt.gender || '—'} · ${appt.visitType}` : `Appointment ${id}`}
          action={appt ? <Badge color="blue">{appt.status}</Badge> : null}
        />
      </Card>
      <VitalsCard appointmentId={id} />
      <PrescriptionCard appointmentId={id} />
    </div>
  )
}

function VitalsCard({ appointmentId }) {
  const toast = useToast()
  const qc = useQueryClient()
  const role = useAuthStore((s) => s.role)
  const canEdit = [ROLES.NURSE, ROLES.DOCTOR, ROLES.ADMIN].includes(role)

  const { data, isLoading } = useQuery({
    queryKey: ['vitals', appointmentId],
    queryFn: () => getVitals(appointmentId),
  })

  const { register, handleSubmit, reset } = useForm({ resolver: zodResolver(vitalsSchema) })

  useEffect(() => {
    reset({
      bpSystolic: data?.bpSystolic ?? '',
      bpDiastolic: data?.bpDiastolic ?? '',
      pulse: data?.pulse ?? '',
      temperature: data?.temperature ?? '',
      spo2: data?.spo2 ?? '',
      weightKg: data?.weightKg ?? '',
      heightCm: data?.heightCm ?? '',
    })
  }, [data, reset])

  const mutation = useMutation({
    mutationFn: (values) => saveVitals({ appointmentId, ...values }),
    onSuccess: () => {
      toast.success('Vitals saved')
      qc.invalidateQueries({ queryKey: ['vitals', appointmentId] })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not save vitals')),
  })

  if (isLoading) return <Card><CenteredSpinner /></Card>

  return (
    <Card>
      <CardHeader
        title="Vitals"
        subtitle={data?.recordedAt ? `Last recorded by ${data.recordedByName || 'staff'}` : 'Not recorded yet'}
      />
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="space-y-4">
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <Input label="BP Systolic" type="number" disabled={!canEdit} {...register('bpSystolic')} />
          <Input label="BP Diastolic" type="number" disabled={!canEdit} {...register('bpDiastolic')} />
          <Input label="Pulse" type="number" disabled={!canEdit} {...register('pulse')} />
          <Input label="SpO₂ %" type="number" disabled={!canEdit} {...register('spo2')} />
          <Input label="Temp °F" type="number" step="0.1" disabled={!canEdit} {...register('temperature')} />
          <Input label="Weight kg" type="number" step="0.1" disabled={!canEdit} {...register('weightKg')} />
          <Input label="Height cm" type="number" disabled={!canEdit} {...register('heightCm')} />
        </div>
        {canEdit && (
          <div className="flex justify-end">
            <Button type="submit" loading={mutation.isPending}>Save vitals</Button>
          </div>
        )}
      </form>
    </Card>
  )
}

function PrescriptionCard({ appointmentId }) {
  const toast = useToast()
  const qc = useQueryClient()
  const role = useAuthStore((s) => s.role)
  const canPrescribe = [ROLES.DOCTOR, ROLES.ADMIN].includes(role)

  const { data, isLoading } = useQuery({
    queryKey: ['prescription', appointmentId],
    queryFn: () => getPrescription(appointmentId),
  })

  const { register, handleSubmit, control, reset } = useForm({
    resolver: zodResolver(prescriptionSchema),
    defaultValues: { medicines: [EMPTY_MED] },
  })
  const { fields, append, remove } = useFieldArray({ control, name: 'medicines' })

  useEffect(() => {
    reset({
      chiefComplaint: data?.chiefComplaint ?? '',
      diagnosis: data?.diagnosis ?? '',
      examination: data?.examination ?? '',
      advice: data?.advice ?? '',
      followupDate: data?.followupDate ?? '',
      medicines: data?.medicines?.length ? data.medicines : [EMPTY_MED],
    })
  }, [data, reset])

  const mutation = useMutation({
    mutationFn: (values) => {
      const medicines = (values.medicines || []).filter((m) => m.medicineName?.trim())
      return savePrescription({
        appointmentId,
        chiefComplaint: values.chiefComplaint || null,
        diagnosis: values.diagnosis || null,
        examination: values.examination || null,
        advice: values.advice || null,
        followupDate: values.followupDate || null,
        medicines,
      })
    },
    onSuccess: () => {
      toast.success('Prescription saved')
      qc.invalidateQueries({ queryKey: ['prescription', appointmentId] })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not save prescription')),
  })

  const whatsappMutation = useMutation({
    mutationFn: () => sendPrescriptionWhatsapp(appointmentId),
    onSuccess: () => {
      toast.success('Prescription sent on WhatsApp')
      qc.invalidateQueries({ queryKey: ['prescription', appointmentId] })
    },
    onError: (e) => toast.error(apiErrorMessage(e, 'Could not send WhatsApp')),
  })

  const onDownload = async () => {
    try {
      await downloadPdf(`/api/prescriptions/by-appointment/${appointmentId}/pdf`, `prescription-${appointmentId}.pdf`)
    } catch (e) {
      toast.error(apiErrorMessage(e, 'No prescription to download yet'))
    }
  }

  if (isLoading) return <Card><CenteredSpinner /></Card>

  const hasSaved = !!data

  return (
    <Card>
      <CardHeader
        title="Consultation & Prescription"
        subtitle="Diagnosis, medicines and follow-up"
        action={
          hasSaved ? (
            <div className="flex gap-2">
              <Button variant="secondary" onClick={onDownload}>Download PDF</Button>
              {canPrescribe && (
                <Button variant="secondary" onClick={() => whatsappMutation.mutate()} loading={whatsappMutation.isPending}>
                  Send WhatsApp
                </Button>
              )}
            </div>
          ) : null
        }
      />
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="space-y-4">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <Textarea label="Chief complaint" disabled={!canPrescribe} {...register('chiefComplaint')} />
          <Textarea label="Examination" disabled={!canPrescribe} {...register('examination')} />
          <Textarea label="Diagnosis" disabled={!canPrescribe} {...register('diagnosis')} />
          <Textarea label="Advice" disabled={!canPrescribe} {...register('advice')} />
        </div>

        <div>
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm font-medium text-slate-700">Medicines</span>
            {canPrescribe && (
              <Button type="button" variant="secondary" onClick={() => append(EMPTY_MED)}>
                + Add medicine
              </Button>
            )}
          </div>
          <div className="space-y-2">
            {fields.map((f, i) => (
              <div key={f.id} className="grid grid-cols-12 gap-2">
                <Input className="col-span-12 sm:col-span-4" placeholder="Medicine" disabled={!canPrescribe} {...register(`medicines.${i}.medicineName`)} />
                <Input className="col-span-4 sm:col-span-2" placeholder="Dosage" disabled={!canPrescribe} {...register(`medicines.${i}.dosage`)} />
                <Input className="col-span-4 sm:col-span-2" placeholder="Freq" disabled={!canPrescribe} {...register(`medicines.${i}.frequency`)} />
                <Input className="col-span-4 sm:col-span-2" placeholder="Duration" disabled={!canPrescribe} {...register(`medicines.${i}.duration`)} />
                <Input className="col-span-10 sm:col-span-1" placeholder="Notes" disabled={!canPrescribe} {...register(`medicines.${i}.instructions`)} />
                {canPrescribe && (
                  <button type="button" onClick={() => remove(i)} className="col-span-2 sm:col-span-1 rounded text-red-500 hover:bg-red-50">
                    ✕
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="flex items-end justify-between gap-4">
          <Input label="Follow-up date" type="date" disabled={!canPrescribe} className="max-w-xs" {...register('followupDate')} />
          {canPrescribe && <Button type="submit" loading={mutation.isPending}>Save prescription</Button>}
        </div>
      </form>
    </Card>
  )
}
