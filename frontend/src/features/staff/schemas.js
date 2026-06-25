import { z } from 'zod'

export const staffSchema = z.object({
  name: z.string().min(2, 'Name is required'),
  phone: z.string().regex(/^\d{10}$/, 'Enter a 10-digit phone'),
  role: z.enum(['DOCTOR', 'RECEPTIONIST', 'NURSE', 'ADMIN']),
  regNumber: z.string().optional(),
  specialty: z.string().optional(),
})
