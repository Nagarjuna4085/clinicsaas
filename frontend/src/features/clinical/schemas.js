import { z } from 'zod'

// Empty string / null → undefined, otherwise a number.
const optNum = z.preprocess(
  (v) => (v === '' || v === null || v === undefined ? undefined : Number(v)),
  z.number().optional()
)

export const vitalsSchema = z.object({
  bpSystolic: optNum,
  bpDiastolic: optNum,
  pulse: optNum,
  temperature: optNum,
  spo2: optNum,
  weightKg: optNum,
  heightCm: optNum,
})

export const prescriptionSchema = z.object({
  chiefComplaint: z.string().optional(),
  diagnosis: z.string().optional(),
  examination: z.string().optional(),
  advice: z.string().optional(),
  followupDate: z.string().optional(),
  medicines: z
    .array(
      z.object({
        medicineName: z.string().optional(),
        dosage: z.string().optional(),
        frequency: z.string().optional(),
        duration: z.string().optional(),
        instructions: z.string().optional(),
      })
    )
    .optional(),
})
