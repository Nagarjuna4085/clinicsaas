import LegalLayout, { P } from './LegalLayout'
import { company } from './companyInfo'

function Row({ label, value }) {
  return (
    <div className="flex flex-col sm:flex-row sm:gap-3">
      <span className="w-32 shrink-0 text-sm font-medium text-slate-500">{label}</span>
      <span className="text-[15px] text-slate-700">{value}</span>
    </div>
  )
}

export default function ContactPage() {
  return (
    <LegalLayout title="Contact Us">
      <P>Questions about {company.product}, billing, or your account? We’re happy to help.</P>

      <div className="mt-6 space-y-3 rounded-xl border border-slate-200 bg-white p-6">
        <Row label="Business" value={company.legalName} />
        <Row label="Proprietor" value={company.proprietor} />
        <Row label="Email" value={<a className="text-brand-600 hover:underline" href={`mailto:${company.email}`}>{company.email}</a>} />
        <Row label="Phone" value={company.phone} />
        <Row label="Address" value={company.addressLines.join(', ')} />
        <Row label="Support hours" value={company.supportHours} />
      </div>

      <P>
        For data-protection or privacy requests, please email us at {company.email} and we’ll respond
        within a reasonable time.
      </P>
    </LegalLayout>
  )
}
