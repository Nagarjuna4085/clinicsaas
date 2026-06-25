import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { decodeJwt } from '../lib/jwt'

// Auth state lives in a persisted Zustand store so the axios interceptor and
// route guards can read it outside the React tree.
export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      role: null,
      name: null,
      clinic: null,
      staffId: null,

      // Called after a successful login / set-password / reset response.
      setAuth: ({ token, role, name, clinicName }) => {
        const claims = decodeJwt(token)
        set({
          token,
          role,
          name,
          clinic: clinicName,
          staffId: claims?.sub ?? null,
        })
      },

      logout: () =>
        set({ token: null, role: null, name: null, clinic: null, staffId: null }),
    }),
    { name: 'clinicflow-auth' }
  )
)

// Non-hook accessors for use in interceptors / plain modules.
export const getToken = () => useAuthStore.getState().token
export const doLogout = () => useAuthStore.getState().logout()
