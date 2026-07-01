import { Link } from 'react-router-dom'
import { plans, company } from '../legal/companyInfo'

const features = [
  {
    title: 'Token queue & appointments',
    desc: 'Walk-in tokens and scheduled slots in one live board. Every doctor sees their own queue in real time.',
  },
  {
    title: 'Patients & visit history',
    desc: 'A clean record for every patient — demographics, allergies, and a full timeline of past visits.',
  },
  {
    title: 'Prescriptions & GST bills',
    desc: 'Write prescriptions, generate printable PDFs, and raise GST-compliant invoices in a couple of clicks.',
  },
  {
    title: 'WhatsApp reminders',
    desc: 'Follow-up reminders go out automatically, so patients come back and no-shows drop.',
  },
  {
    title: 'Reports & analytics',
    desc: 'Daily revenue and visit trends over 7, 30, or 90 days — know how your clinic is really doing.',
  },
  {
    title: 'Private & secure',
    desc: 'Each clinic’s data is isolated, and the most sensitive fields are encrypted at rest.',
  },
]

export default function LandingPage() {
  return (
    <div className="min-h-full bg-white text-slate-800">
      {/* Nav */}
      <header className="sticky top-0 z-10 border-b border-slate-100 bg-white/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 font-bold text-white">
              C
            </div>
            <span className="text-lg font-semibold text-slate-800">ClinicFlow</span>
          </div>
          <div className="flex items-center gap-2 sm:gap-3">
            <a href="#pricing" className="hidden text-sm font-medium text-slate-600 hover:text-brand-600 sm:block">
              Pricing
            </a>
            <Link to="/login" className="text-sm font-medium text-slate-600 hover:text-brand-600">
              Sign in
            </Link>
            <Link
              to="/signup"
              className="rounded-lg bg-brand-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-brand-700"
            >
              Start free trial
            </Link>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="bg-gradient-to-br from-slate-900 to-brand-900 text-white">
        <div className="mx-auto max-w-6xl px-4 py-20 text-center sm:px-6 sm:py-28">
          <span className="inline-block rounded-full bg-white/10 px-3 py-1 text-xs font-medium text-brand-100">
            Clinic management, made simple
          </span>
          <h1 className="mx-auto mt-5 max-w-3xl text-3xl font-bold leading-tight sm:text-5xl">
            Run your clinic without the paperwork
          </h1>
          <p className="mx-auto mt-4 max-w-2xl text-base text-slate-300 sm:text-lg">
            ClinicFlow handles your patient queue, prescriptions, billing, and follow-ups — so you can
            focus on patients, not admin. Built for Indian clinics.
          </p>
          <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link
              to="/signup"
              className="w-full rounded-lg bg-brand-600 px-6 py-3 text-center font-medium text-white hover:bg-brand-700 sm:w-auto"
            >
              Start your free trial
            </Link>
            <Link
              to="/login"
              className="w-full rounded-lg border border-white/30 px-6 py-3 text-center font-medium text-white hover:bg-white/10 sm:w-auto"
            >
              Sign in
            </Link>
          </div>
          <p className="mt-4 text-xs text-slate-400">No card required to start · Cancel anytime</p>
        </div>
      </section>

      {/* Features */}
      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-20">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-slate-900 sm:text-3xl">Everything your front desk needs</h2>
          <p className="mx-auto mt-2 max-w-2xl text-slate-500">
            One tidy app for the whole clinic — reception, doctors, and admin.
          </p>
        </div>
        <div className="mt-10 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {features.map((f) => (
            <div key={f.title} className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="font-semibold text-slate-800">{f.title}</h3>
              <p className="mt-2 text-sm text-slate-500">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Pricing */}
      <section id="pricing" className="border-t border-slate-100 bg-slate-50">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-20">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-slate-900 sm:text-3xl">Simple, transparent pricing</h2>
            <p className="mx-auto mt-2 max-w-2xl text-slate-500">
              Every plan starts with a free trial. Cancel anytime.
            </p>
          </div>
          <div className="mt-10 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
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
                    plan.highlight
                      ? 'bg-brand-600 text-white hover:bg-brand-700'
                      : 'border border-slate-300 text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  {plan.name === 'Hospital' ? 'Contact us' : 'Start free trial'}
                </Link>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="mx-auto max-w-6xl px-4 py-16 text-center sm:px-6">
        <h2 className="text-2xl font-bold text-slate-900 sm:text-3xl">Ready to tidy up your clinic?</h2>
        <p className="mx-auto mt-2 max-w-xl text-slate-500">
          Set up in minutes. Start free and see if it fits your practice.
        </p>
        <Link
          to="/signup"
          className="mt-6 inline-block rounded-lg bg-brand-600 px-6 py-3 font-medium text-white hover:bg-brand-700"
        >
          Start your free trial
        </Link>
      </section>

      {/* Footer */}
      <footer className="border-t border-slate-100 bg-white">
        <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
          <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">
            <div className="flex items-center gap-2 text-sm text-slate-500">
              <div className="flex h-6 w-6 items-center justify-center rounded bg-brand-600 text-xs font-bold text-white">
                C
              </div>
              <span>© {new Date().getFullYear()} {company.legalName}</span>
            </div>
            <div className="flex flex-wrap justify-center gap-x-4 gap-y-1 text-sm text-slate-500">
              <Link to="/pricing" className="hover:text-brand-600">Pricing</Link>
              <Link to="/privacy" className="hover:text-brand-600">Privacy</Link>
              <Link to="/terms" className="hover:text-brand-600">Terms</Link>
              <Link to="/refund" className="hover:text-brand-600">Refunds</Link>
              <Link to="/contact" className="hover:text-brand-600">Contact</Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
