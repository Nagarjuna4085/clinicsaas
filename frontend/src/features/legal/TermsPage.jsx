import LegalLayout, { H2, P, UL } from './LegalLayout'
import { company } from './companyInfo'

export default function TermsPage() {
  return (
    <LegalLayout title="Terms of Service">
      <P>
        These Terms of Service (“Terms”) govern your use of {company.product}, operated by
        {' '}{company.legalName} (“we”, “us”). By creating an account or using the service, you agree to
        these Terms.
      </P>

      <H2>Eligibility and accounts</H2>
      <UL items={[
        'You must be 18 or older and authorised to register and operate the clinic account.',
        'You are responsible for the security of your password and for all activity by staff you add.',
        'You must provide accurate information and keep it up to date.',
      ]} />

      <H2>The service</H2>
      <P>
        {company.product} is a subscription-based software platform that helps clinics manage patients,
        appointments, prescriptions, and billing. We may add, change, or remove features over time.
      </P>

      <H2>Your data and responsibilities</H2>
      <UL items={[
        'You (the clinic) own the data you enter and remain responsible for it.',
        'You grant us a limited licence to process that data solely to provide the service.',
        'You are responsible for obtaining patient consent and complying with applicable laws (including the DPDP Act) when using the service.',
        'You will not use the service for any unlawful purpose, or attempt to disrupt, reverse engineer, or gain unauthorised access to it.',
      ]} />

      <H2>Subscriptions, fees, and taxes</H2>
      <UL items={[
        'Paid plans are billed in advance on a recurring basis through our payment partner.',
        'Fees are exclusive of applicable taxes unless stated otherwise.',
        'Subscriptions renew automatically until cancelled. See our Refund & Cancellation Policy.',
        'We may offer a free trial; access may be limited or suspended if the trial ends without an active subscription.',
      ]} />

      <H2>Availability and disclaimers</H2>
      <P>
        The service is provided on an “as is” and “as available” basis. We do not guarantee
        uninterrupted availability and may perform maintenance. {company.product} is a record-keeping
        and workflow tool, not a medical device; all clinical decisions are the sole responsibility of
        the treating clinician.
      </P>

      <H2>Limitation of liability</H2>
      <P>
        To the maximum extent permitted by law, we are not liable for any indirect, incidental, or
        consequential damages, or for any loss arising from clinical decisions, data entered by users,
        or service interruptions. Our total liability for any claim is limited to the fees you paid for
        the service in the three months preceding the claim.
      </P>

      <H2>Termination</H2>
      <P>
        You may cancel at any time. We may suspend or terminate access for breach of these Terms or
        non-payment. On termination, you may request an export of your data within a reasonable period.
      </P>

      <H2>Governing law</H2>
      <P>
        These Terms are governed by the laws of India, and the courts at {company.jurisdiction} have
        exclusive jurisdiction.
      </P>

      <H2>Changes</H2>
      <P>
        We may update these Terms from time to time; continued use after changes constitutes acceptance.
      </P>

      <H2>Contact</H2>
      <P>{company.legalName}. Email: {company.email}.</P>
    </LegalLayout>
  )
}
