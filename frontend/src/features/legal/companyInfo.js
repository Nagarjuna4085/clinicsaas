// ── Edit your business details here (used across all policy pages) ──
// TODO: replace placeholders with your final registered details before
// submitting for Razorpay KYC.
export const company = {
  product: 'ClinicFlow',
  // Your Udyam enterprise / proprietorship name (the umbrella brand):
  legalName: 'Sanam Labs', // TODO: set to your registered Udyam enterprise name
  proprietor: 'Nagarjuna Sanam',
  email: 'nagarjun.sanam@gmail.com', // TODO: ideally a support@ address
  phone: '+91-7842401280', // TODO
  addressLines: [
    '1-38, Enakandla',
    'Banaganapalli',
    'Nandyal, Andhra Pradesh 518124',
    'India',
  ],
  jurisdiction: 'Nandyal, Andhra Pradesh, India',
  supportHours: 'Mon–Sat, 10:00–18:00 IST',
  lastUpdated: 'June 2026',
}

// Shown on the Pricing page. TODO: set your real prices.
export const plans = [
  {
    name: 'Starter',
    price: '₹499',
    period: '/month',
    blurb: 'For a single-doctor clinic getting started.',
    features: ['1 doctor + reception', 'Patients & token queue', 'Prescriptions & GST bills', 'Email support'],
  },
  {
    name: 'Clinic',
    price: '₹999',
    period: '/month',
    blurb: 'For a busy multi-staff clinic.',
    features: ['Up to 5 staff', 'Everything in Starter', 'WhatsApp follow-up reminders', 'Priority support'],
    highlight: true,
  },
  {
    name: 'Pro',
    price: '₹1,999',
    period: '/month',
    blurb: 'For multi-doctor practices.',
    features: ['Up to 15 staff', 'Everything in Clinic', 'Reports & analytics', 'Data export'],
  },
  {
    name: 'Hospital',
    price: 'Custom',
    period: '',
    blurb: 'For larger setups.',
    features: ['Unlimited staff', 'Everything in Pro', 'Onboarding assistance', 'Dedicated support'],
  },
]
