import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getTodayBills } from './api'
import { Card, CardHeader } from '../../components/ui/Card'
import Modal from '../../components/ui/Modal'
import Button from '../../components/ui/Button'
import { Badge, CenteredSpinner, EmptyState, ErrorState } from '../../components/ui/Misc'
import { apiErrorMessage, downloadPdf } from '../../lib/apiClient'
import { useToast } from '../../components/ui/Toast'

const inr = (n) => `₹${Number(n ?? 0).toLocaleString('en-IN')}`
const fmtTime = (s) => (s ? new Date(s).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }) : '—')

const modeColor = { CASH: 'green', UPI: 'blue', CARD: 'violet' }

export default function BillingPage() {
  const [selected, setSelected] = useState(null)
  const toast = useToast()

  const downloadInvoice = async (bill) => {
    try {
      await downloadPdf(`/api/bills/${bill.id}/pdf`, `${bill.invoiceNumber || 'invoice'}.pdf`)
    } catch (e) {
      toast.error(apiErrorMessage(e, 'Could not download invoice'))
    }
  }

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['bills', 'today'],
    queryFn: getTodayBills,
  })

  const total = (data || []).reduce((s, b) => s + Number(b.total ?? 0), 0)

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader
          title="Billing"
          subtitle="Invoices generated today"
          action={
            <div className="text-right">
              <p className="text-xs text-slate-400">Today's revenue</p>
              <p className="text-xl font-bold text-green-600">{inr(total)}</p>
            </div>
          }
        />
      </Card>

      <Card>
        {isLoading ? (
          <CenteredSpinner />
        ) : isError ? (
          <ErrorState message={apiErrorMessage(error)} />
        ) : !data || data.length === 0 ? (
          <EmptyState title="No bills today" hint="Bills are created automatically when an appointment is booked with an OP fee." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-left text-slate-500">
                  <th className="px-3 py-2">Invoice</th>
                  <th className="px-3 py-2">Patient</th>
                  <th className="px-3 py-2">Total</th>
                  <th className="px-3 py-2">Mode</th>
                  <th className="px-3 py-2">Status</th>
                  <th className="px-3 py-2">Time</th>
                  <th className="px-3 py-2"></th>
                </tr>
              </thead>
              <tbody>
                {data.map((b) => (
                  <tr key={b.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-3 py-2 font-mono text-xs">{b.invoiceNumber}</td>
                    <td className="px-3 py-2 font-medium text-slate-700">{b.patientName || '—'}</td>
                    <td className="px-3 py-2 font-semibold">{inr(b.total)}</td>
                    <td className="px-3 py-2">
                      <Badge color={modeColor[b.paymentMode] || 'slate'}>{b.paymentMode}</Badge>
                    </td>
                    <td className="px-3 py-2">
                      <Badge color={b.status === 'PAID' ? 'green' : 'amber'}>{b.status}</Badge>
                    </td>
                    <td className="px-3 py-2 text-slate-500">{fmtTime(b.billedAt)}</td>
                    <td className="px-3 py-2 text-right">
                      <button className="text-brand-600 hover:underline" onClick={() => setSelected(b)}>
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <Modal open={!!selected} onClose={() => setSelected(null)} title={selected?.invoiceNumber || 'Invoice'}>
        {selected && (
          <div className="space-y-4">
            <div className="flex justify-between text-sm">
              <span className="text-slate-500">Patient</span>
              <span className="font-medium">{selected.patientName || '—'}</span>
            </div>
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-left text-slate-500">
                  <th className="py-1">Item</th>
                  <th className="py-1">HSN/SAC</th>
                  <th className="py-1 text-right">Amount</th>
                </tr>
              </thead>
              <tbody>
                {selected.items?.map((it, i) => (
                  <tr key={i} className="border-b border-slate-100">
                    <td className="py-1.5">{it.description}</td>
                    <td className="py-1.5 text-slate-500">{it.hsnSac || '—'}</td>
                    <td className="py-1.5 text-right">{inr(it.amount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="space-y-1 border-t border-slate-200 pt-3 text-sm">
              <Row label="Subtotal" value={inr(selected.subtotal)} />
              <Row label="CGST" value={inr(selected.cgst)} />
              <Row label="SGST" value={inr(selected.sgst)} />
              <Row label="Total" value={inr(selected.total)} bold />
              <Row label="Payment" value={selected.paymentMode} />
            </div>
            <div className="flex justify-end pt-2">
              <Button variant="secondary" onClick={() => downloadInvoice(selected)}>
                Download PDF
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}

function Row({ label, value, bold }) {
  return (
    <div className={`flex justify-between ${bold ? 'font-bold text-slate-800' : 'text-slate-600'}`}>
      <span>{label}</span>
      <span>{value}</span>
    </div>
  )
}
