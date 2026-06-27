import LegalLayout, { H2, P, UL } from './LegalLayout'
import { company } from './companyInfo'

export default function PrivacyPage() {
  return (
    <LegalLayout title="Privacy Policy">
      <P>
        This Privacy Policy explains how {company.legalName} (“we”, “us”), the operator of
        {' '}{company.product}, collects, uses, stores, and protects information when you use our
        software. We are committed to handling personal and health data responsibly and in line with
        India’s Digital Personal Data Protection Act, 2023 (DPDP Act).
      </P>

      <H2>Information we collect</H2>
      <UL items={[
        'Account data: clinic name, owner/staff name, phone number, email, and city, used to create and manage your account.',
        'Patient data entered by clinics: patient name, contact details, age/gender, allergies, vitals, diagnoses, prescriptions, and billing records. This is entered and controlled by the clinic.',
        'Payment information: processed securely by our payment partner (Razorpay). We do not store your card or bank credentials.',
        'Technical data: log data, IP address, and basic usage information needed to operate and secure the service.',
      ]} />

      <H2>Our role and the clinic’s role</H2>
      <P>
        For patient data, the clinic using {company.product} is the data fiduciary (controller) and is
        responsible for obtaining patient consent and using the data lawfully. We act as a data
        processor, handling that data only to provide the service to the clinic. Patients who wish to
        access, correct, export, or delete their data should contact the clinic that holds their record;
        the clinic can action these requests through the app, and we support them in doing so.
      </P>

      <H2>How we use information</H2>
      <UL items={[
        'To provide, maintain, and improve the service.',
        'To process subscriptions and payments.',
        'To send OTPs and service notifications.',
        'To provide customer support.',
        'To keep the service secure and comply with legal obligations.',
      ]} />

      <H2>Sharing</H2>
      <P>
        We do not sell personal data. We share data only with service providers who help us operate,
        under appropriate safeguards: payment processing (Razorpay), SMS/OTP (MSG91), WhatsApp
        messaging (Gupshup), and cloud hosting. We may disclose data where required by law.
      </P>

      <H2>Storage and security</H2>
      <UL items={[
        'Data is transmitted over encrypted (HTTPS) connections.',
        'Each clinic’s data is isolated in its own database schema.',
        'Access is restricted by role and protected by authentication.',
        'Sensitive operations on patient records are audit-logged.',
      ]} />

      <H2>Data retention and your rights</H2>
      <P>
        We retain data for as long as a clinic’s account is active or as required by law. On request,
        a clinic admin can export a patient’s data or erase their personal information (we retain the
        minimum clinical/financial records required for legal retention). To exercise DPDP rights or
        raise a grievance, contact us at {company.email}.
      </P>

      <H2>Cookies</H2>
      <P>
        We use only essential storage needed to keep you signed in. We do not use advertising or
        tracking cookies.
      </P>

      <H2>Children</H2>
      <P>
        The service is intended for use by clinics and their staff. Where a patient is a minor, the
        clinic is responsible for obtaining consent from a parent or guardian.
      </P>

      <H2>Changes</H2>
      <P>
        We may update this policy from time to time. Material changes will be reflected by updating the
        “Last updated” date above.
      </P>

      <H2>Contact</H2>
      <P>
        {company.legalName}, {company.addressLines.join(', ')}. Email: {company.email}.
      </P>
    </LegalLayout>
  )
}
