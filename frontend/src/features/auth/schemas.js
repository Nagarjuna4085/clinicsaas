import { z } from 'zod'

const phone = z.string().regex(/^\d{10}$/, 'Enter a 10-digit phone number')
const otp = z.string().regex(/^\d{6}$/, 'Enter the 6-digit OTP')
const newPassword = z.string().min(6, 'Password must be at least 6 characters')

export const loginSchema = z.object({
  phone,
  password: z.string().min(1, 'Enter your password'),
})

export const setPasswordSchema = z.object({
  currentPassword: z.string().min(1, 'Enter the temporary password'),
  newPassword,
})

export const forgotPhoneSchema = z.object({ phone })

export const resetPasswordSchema = z.object({
  otp,
  newPassword,
})

// Step 1 of signup — clinic + owner details (no OTP/password yet).
// The owner is always the ADMIN; "alsoDoctor" marks them as a bookable provider.
export const signupDetailsSchema = z.object({
  clinicName: z.string().min(2, 'Clinic name is required'),
  ownerName: z.string().min(2, 'Owner name is required'),
  ownerPhone: phone,
  city: z.string().optional(),
  plan: z.enum(['starter', 'clinic', 'pro', 'hospital']).default('starter'),
  alsoDoctor: z.boolean().optional(),
  regNumber: z.string().optional(),
  specialty: z.string().optional(),
  consent: z.boolean().refine((v) => v === true, 'You must accept the Terms & Privacy Policy'),
})

// Step 2 of signup — OTP + chosen password.
export const signupVerifySchema = z.object({
  otp,
  password: newPassword,
})
