import { Link } from 'react-router-dom'
import LegalLayout, { P } from './LegalLayout'
import { plans } from './companyInfo'

export default function PricingPage() {
  return (
    <LegalLayout title="Pricing">
      <P>Simple, transparent pricing for clinics of every size. All plans start with a free trial. Cancel anytime.</P>

      <div className="mt-6 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {plans.map((plan) => (
          <div
            key={plan.name}
            className={`flex flex-col rounded-xl border bg-white p-5 shadow-sm ${
              plan.highlight ? 'border-brand-500 ring-1 ring-brand-200' : 'border-slate-200'
            }`}
          >
            {plan.highlight && (
              <span className="mb-2 inline-block w-fit rounded-full bg-brand-50 px-2 py-0.5 text-xs font-medium text-brand-700">
                Most popular
              </span>
            )}
            <h3 className="text-lg font-semibold text-slate-800">{plan.name}</h3>
            <div className="mt-1">
              <span className="text-2xl font-bold text-slate-900">{plan.price}</span>
              <span className="text-sm text-slate-500">{plan.period}</span>
            </div>
            <p className="mt-1 text-sm text-slate-500">{plan.blurb}</p>
            <ul className="mt-4 flex-1 space-y-2 text-sm text-slate-600">
              {plan.features.map((f) => (
                <li key={f} className="flex gap-2">
                  <span className="text-brand-600">✓</span> {f}
                </li>
              ))}
            </ul>
            <Link
              to={plan.name === 'Hospital' ? '/contact' : '/signup'}
              className={`mt-5 rounded-lg px-4 py-2 text-center text-sm font-medium ${
                plan.highlight ? 'bg-brand-600 text-white hover:bg-brand-700' : 'border border-slate-300 text-slate-700 hover:bg-slate-50'
              }`}
            >
              {plan.name === 'Hospital' ? 'Contact us' : 'Start free trial'}
            </Link>
          </div>
        ))}
      </div>

      <p className="mt-6 text-sm text-slate-500">
        Prices are in INR and exclusive of applicable taxes (e.g. GST) where applicable. Subscriptions
        renew automatically until cancelled — see our{' '}
        <Link to="/refund" className="text-brand-600 hover:underline">Refund &amp; Cancellation Policy</Link>{' '}
        and <Link to="/terms" className="text-brand-600 hover:underline">Terms</Link>.
      </p>
    </LegalLayout>
  )
}
