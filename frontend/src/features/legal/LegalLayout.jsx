import { Link } from 'react-router-dom'
import { company } from './companyInfo'

const NAV = [
  { to: '/pricing', label: 'Pricing' },
  { to: '/privacy', label: 'Privacy' },
  { to: '/terms', label: 'Terms' },
  { to: '/refund', label: 'Refunds' },
  { to: '/contact', label: 'Contact' },
]

export default function LegalLayout({ title, children }) {
  return (
    <div className="min-h-full bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-4xl items-center justify-between px-6 py-4">
          <Link to="/" className="flex items-center gap-2">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 font-bold text-white">C</span>
            <span className="text-lg font-semibold text-slate-800">{company.product}</span>
          </Link>
          <nav className="hidden gap-4 text-sm text-slate-600 sm:flex">
            {NAV.map((n) => (
              <Link key={n.to} to={n.to} className="hover:text-brand-600">{n.label}</Link>
            ))}
            <Link to="/login" className="font-medium text-brand-600 hover:underline">Sign in</Link>
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-4xl px-6 py-10">
        {title && <h1 className="mb-1 text-3xl font-bold text-slate-800">{title}</h1>}
        <p className="mb-8 text-sm text-slate-500">Last updated: {company.lastUpdated}</p>
        <div className="prose-clinic space-y-5 text-slate-700">{children}</div>
      </main>

      <footer className="border-t border-slate-200 bg-white">
        <div className="mx-auto max-w-4xl px-6 py-6 text-sm text-slate-500">
          <div className="mb-2 flex flex-wrap gap-4">
            {NAV.map((n) => (
              <Link key={n.to} to={n.to} className="hover:text-brand-600">{n.label}</Link>
            ))}
          </div>
          <p>© {new Date().getFullYear()} {company.legalName}. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}

// Section heading + paragraph helpers for consistent styling.
export function H2({ children }) {
  return <h2 className="mt-8 text-xl font-semibold text-slate-800">{children}</h2>
}
export function P({ children }) {
  return <p className="text-[15px] leading-relaxed text-slate-600">{children}</p>
}
export function UL({ items }) {
  return (
    <ul className="list-disc space-y-1 pl-6 text-[15px] text-slate-600">
      {items.map((it, i) => (
        <li key={i}>{it}</li>
      ))}
    </ul>
  )
}
