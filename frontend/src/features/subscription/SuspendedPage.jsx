import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/auth'
import { ROLES } from '../../lib/constants'

// Static page (makes no API calls) shown when the clinic's subscription is
// suspended. Admins can jump to billing; others are told to contact their admin.
export default function SuspendedPage() {
  const navigate = useNavigate()
  const { role, clinic, name, logout } = useAuthStore()
  const isAdmin = role === ROLES.ADMIN

  const onLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex min-h-full items-center justify-center bg-slate-100 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-xl">
        <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-red-100 text-xl">
          ⏸️
        </div>
        <h1 className="text-xl font-bold text-slate-800">Subscription inactive</h1>
        <p className="mt-2 text-sm text-slate-500">
          {clinic ? `${clinic}'s ` : 'Your '} access is paused because the subscription is not active.
        </p>

        {isAdmin ? (
          <button
            onClick={() => navigate('/clinic')}
            className="mt-6 w-full rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700"
          >
            Go to billing &amp; renew
          </button>
        ) : (
          <p className="mt-6 rounded-lg bg-slate-50 px-4 py-3 text-sm text-slate-600">
            Please ask your clinic admin ({name ? `not ${name}` : 'an ADMIN user'}) to renew the subscription.
          </p>
        )}

        <button onClick={onLogout} className="mt-3 w-full rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-50">
          Log out
        </button>
      </div>
    </div>
  )
}
