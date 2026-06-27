import LegalLayout, { H2, P, UL } from './LegalLayout'
import { company } from './companyInfo'

export default function RefundPage() {
  return (
    <LegalLayout title="Refund & Cancellation Policy">
      <P>
        This policy explains how subscriptions to {company.product}, operated by {company.legalName},
        are billed, cancelled, and refunded.
      </P>

      <H2>Free trial</H2>
      <P>
        New clinics start with a free trial. You are not charged during the trial and can choose a paid
        plan at any time. If you do not subscribe before the trial ends, access may be paused until you
        subscribe.
      </P>

      <H2>Billing</H2>
      <UL items={[
        'Paid plans are billed in advance for the chosen billing cycle (monthly or annual).',
        'Subscriptions renew automatically at the end of each cycle until you cancel.',
        'Prices are shown on our Pricing page and are in Indian Rupees (INR).',
      ]} />

      <H2>Cancellation</H2>
      <UL items={[
        'You can cancel your subscription at any time from your account settings or by emailing us.',
        'On cancellation, your plan remains active until the end of the current paid period; it does not renew thereafter.',
        'You can continue to export your data during the paid period.',
      ]} />

      <H2>Refunds</H2>
      <UL items={[
        'If you are charged in error or experience a billing issue, contact us within 7 days and we will investigate and refund any incorrect charge.',
        'As a generous gesture for first-time subscribers, you may request a full refund within 7 days of your first payment if the service does not meet your needs.',
        'Beyond the above, fees for the current billing period are generally non-refundable, as access continues until the end of the period.',
        'Approved refunds are processed to the original payment method, typically within 5–7 business days.',
      ]} />

      <H2>Failed payments</H2>
      <P>
        If a renewal payment fails, we may retry and temporarily suspend access until payment succeeds.
      </P>

      <H2>How to request</H2>
      <P>
        For cancellations or refund requests, email {company.email} from your registered address with
        your clinic name and details. Support hours: {company.supportHours}.
      </P>
    </LegalLayout>
  )
}
