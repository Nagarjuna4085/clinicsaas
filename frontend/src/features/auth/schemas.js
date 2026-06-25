import { z } from 'zod'

const phone = z.string().regex(/^\d{10}$/, 'Enter a 10-digit phone number')

export const signupSchema = z.object({
  clinicName: z.string().min(2, 'Clinic name is required'),
  ownerName: z.string().min(2, 'Owner name is required'),
  ownerPhone: phone,
  city: z.string().optional(),
  plan: z.enum(['starter', 'clinic', 'pro', 'hospital']).default('starter'),
  role: z.enum(['DOCTOR', 'ADMIN', 'RECEPTIONIST', 'NURSE']).default('ADMIN'),
  regNumber: z.string().optional(),
  specialty: z.string().optional(),
})

export const phoneSchema = z.object({ phone })

export const otpSchema = z.object({
  phone,
  otp: z.string().regex(/^\d{6}$/, 'Enter the 6-digit OTP'),
})
