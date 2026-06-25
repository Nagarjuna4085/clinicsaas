import { z } from 'zod'

export const bookingSchema = z.object({
  patientId: z.string().uuid('Select a patient'),
  doctorId: z.string().uuid('Select a doctor'),
  visitType: z.enum(['WALKIN', 'SCHEDULED', 'FOLLOWUP']).default('WALKIN'),
  opFee: z.coerce.number().int().min(0).default(0),
  paymentMode: z.enum(['CASH', 'UPI', 'CARD']).default('CASH'),
})
