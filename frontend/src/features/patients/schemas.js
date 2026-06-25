import { z } from 'zod'

export const patientSchema = z.object({
  name: z.string().min(2, 'Name is required'),
  phone: z
    .string()
    .regex(/^\d{10}$/, 'Enter a 10-digit phone')
    .optional()
    .or(z.literal('')),
  age: z.coerce.number().int().min(0).max(120).optional().or(z.literal('')),
  gender: z.enum(['M', 'F', 'O', '']).optional(),
  bloodGroup: z.string().optional(),
  abhaId: z.string().optional(),
  allergies: z.string().optional(),
})
