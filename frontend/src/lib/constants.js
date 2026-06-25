export const ROLES = {
  ADMIN: 'ADMIN',
  RECEPTIONIST: 'RECEPTIONIST',
  DOCTOR: 'DOCTOR',
  NURSE: 'NURSE',
}

export const ALL_ROLES = Object.values(ROLES)

// Navigation items, each gated by the roles allowed to see it.
export const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', roles: ALL_ROLES },
  { to: '/appointments', label: 'Appointments', roles: ALL_ROLES },
  { to: '/patients', label: 'Patients', roles: ALL_ROLES },
  { to: '/staff', label: 'Staff', roles: ALL_ROLES },
]

export const APPOINTMENT_STATUSES = ['WAITING', 'CONSULTING', 'COMPLETED', 'CANCELLED']
export const VISIT_TYPES = ['WALKIN', 'SCHEDULED', 'FOLLOWUP']
export const PAYMENT_MODES = ['CASH', 'UPI', 'CARD']
