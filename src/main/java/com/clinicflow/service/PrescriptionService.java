package com.clinicflow.service;

import com.clinicflow.context.TenantContext;
import com.clinicflow.dto.PrescriptionDto;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.entity.tenant.Appointment;
import com.clinicflow.entity.tenant.Consultation;
import com.clinicflow.entity.tenant.Prescription;
import com.clinicflow.entity.tenant.PrescriptionItem;
import com.clinicflow.exception.NotFoundException;
import com.clinicflow.repository.global.TenantRepository;
import com.clinicflow.repository.tenant.AppointmentRepository;
import com.clinicflow.repository.tenant.ConsultationRepository;
import com.clinicflow.repository.tenant.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepo;
    private final ConsultationRepository consultationRepo;
    private final AppointmentRepository appointmentRepo;
    private final TenantRepository tenantRepo;
    private final PdfService pdfService;
    private final WhatsAppService whatsAppService;

    public PrescriptionService(PrescriptionRepository prescriptionRepo,
                               ConsultationRepository consultationRepo,
                               AppointmentRepository appointmentRepo,
                               TenantRepository tenantRepo,
                               PdfService pdfService,
                               WhatsAppService whatsAppService) {
        this.prescriptionRepo = prescriptionRepo;
        this.consultationRepo = consultationRepo;
        this.appointmentRepo = appointmentRepo;
        this.tenantRepo = tenantRepo;
        this.pdfService = pdfService;
        this.whatsAppService = whatsAppService;
    }

    /** Sends a WhatsApp note that the prescription is ready, and flags it sent. */
    @Transactional
    public PrescriptionDto.Response sendWhatsapp(UUID appointmentId) {
        Prescription p = prescriptionRepo.findByAppointmentId(appointmentId)
            .orElseThrow(() -> new NotFoundException("No prescription for this appointment"));
        Appointment appt = p.getAppointment();
        String phone = appt != null && appt.getPatient() != null ? appt.getPatient().getPhone() : null;
        String name = appt != null && appt.getPatient() != null ? appt.getPatient().getName() : "";

        if (phone != null && !phone.isBlank()) {
            whatsAppService.sendText(phone,
                "Hi " + name + ", your prescription from " + clinicName() + " is ready.");
        }
        p.setWhatsappSent(true);
        p.setWhatsappSentAt(OffsetDateTime.now());
        prescriptionRepo.save(p);

        Consultation c = consultationRepo.findByAppointmentId(appointmentId).orElse(null);
        return toResponse(p, c, appt);
    }

    @Transactional(readOnly = true)
    public byte[] pdf(UUID appointmentId) {
        Prescription p = prescriptionRepo.findByAppointmentId(appointmentId)
            .orElseThrow(() -> new NotFoundException("No prescription for this appointment"));
        Consultation c = consultationRepo.findByAppointmentId(appointmentId).orElse(null);
        Appointment appt = p.getAppointment();
        String doctorName = appt != null && appt.getDoctor() != null ? appt.getDoctor().getName() : null;
        String patientName = appt != null && appt.getPatient() != null ? appt.getPatient().getName() : null;
        String followup = appt != null && appt.getFollowupDate() != null ? appt.getFollowupDate().toString() : null;

        List<PdfService.RxLine> meds = p.getItems().stream()
            .map(i -> new PdfService.RxLine(i.getMedicineName(), i.getDosage(),
                i.getFrequency(), i.getDuration(), i.getInstructions()))
            .collect(Collectors.toList());

        return pdfService.prescriptionPdf(
            clinicName(), doctorName, patientName, LocalDate.now().toString(),
            c != null ? c.getChiefComplaint() : null,
            c != null ? c.getDiagnosis() : null,
            c != null ? c.getAdvice() : null,
            followup, meds);
    }

    private String clinicName() {
        String schema = TenantContext.get();
        if (schema == null) return "Clinic";
        return tenantRepo.findBySchemaName(schema).map(Tenant::getClinicName).orElse("Clinic");
    }

    /**
     * Saves the consultation notes + prescription for a visit and sets the
     * appointment's follow-up date (which the reminder scheduler then picks up).
     * Idempotent per appointment — re-posting updates the existing records.
     */
    @Transactional
    public PrescriptionDto.Response create(PrescriptionDto.CreateRequest req) {
        Appointment appt = appointmentRepo.findById(req.appointmentId())
            .orElseThrow(() -> new NotFoundException("Appointment not found"));

        // Consultation notes (upsert)
        Consultation c = consultationRepo.findByAppointmentId(req.appointmentId())
            .orElseGet(() -> Consultation.builder().appointment(appt).build());
        c.setChiefComplaint(req.chiefComplaint());
        c.setDiagnosis(req.diagnosis());
        c.setExamination(req.examination());
        c.setAdvice(req.advice());
        consultationRepo.save(c);

        // Prescription + items (upsert; replace items)
        Prescription p = prescriptionRepo.findByAppointmentId(req.appointmentId())
            .orElseGet(() -> Prescription.builder().appointment(appt).build());
        p.getItems().clear();
        if (req.medicines() != null) {
            short order = 0;
            for (PrescriptionDto.CreateRequest.ItemRequest m : req.medicines()) {
                p.getItems().add(PrescriptionItem.builder()
                    .prescription(p)
                    .medicineName(m.medicineName())
                    .dosage(m.dosage())
                    .frequency(m.frequency())
                    .duration(m.duration())
                    .instructions(m.instructions())
                    .sortOrder(order++)
                    .build());
            }
        }
        Prescription saved = prescriptionRepo.save(p);

        // Follow-up date on the appointment (drives WhatsApp reminders)
        appt.setFollowupDate(req.followupDate());
        appointmentRepo.save(appt);

        return toResponse(saved, c, appt);
    }

    @Transactional(readOnly = true)
    public PrescriptionDto.Response get(UUID appointmentId) {
        Prescription p = prescriptionRepo.findByAppointmentId(appointmentId).orElse(null);
        if (p == null) return null;
        Consultation c = consultationRepo.findByAppointmentId(appointmentId).orElse(null);
        return toResponse(p, c, p.getAppointment());
    }

    private PrescriptionDto.Response toResponse(Prescription p, Consultation c, Appointment appt) {
        List<PrescriptionDto.Response.ItemResponse> items = p.getItems().stream()
            .map(i -> new PrescriptionDto.Response.ItemResponse(
                i.getMedicineName(), i.getDosage(), i.getFrequency(),
                i.getDuration(), i.getInstructions()))
            .collect(Collectors.toList());

        return new PrescriptionDto.Response(
            p.getId(),
            c != null ? c.getChiefComplaint() : null,
            c != null ? c.getDiagnosis() : null,
            c != null ? c.getExamination() : null,
            c != null ? c.getAdvice() : null,
            appt != null ? appt.getFollowupDate() : null,
            p.getPdfUrl(),
            p.isWhatsappSent(),
            items
        );
    }
}
