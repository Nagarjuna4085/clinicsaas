package com.clinicflow.service;

import com.clinicflow.dto.PatientDto;
import com.clinicflow.entity.tenant.Patient;
import com.clinicflow.exception.BadRequestException;
import com.clinicflow.exception.NotFoundException;
import com.clinicflow.repository.tenant.AppointmentRepository;
import com.clinicflow.repository.tenant.BillRepository;
import com.clinicflow.repository.tenant.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepo;
    private final AppointmentRepository appointmentRepo;
    private final BillRepository billRepo;
    private final AuditService auditService;

    public PatientService(PatientRepository patientRepo,
                          AppointmentRepository appointmentRepo,
                          BillRepository billRepo,
                          AuditService auditService) {
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
        this.billRepo = billRepo;
        this.auditService = auditService;
    }

    @Transactional
    public PatientDto.Response registerOrUpdate(PatientDto.RegisterRequest req) {
        Optional<Patient> existing = (req.phone() != null)
            ? patientRepo.findByPhone(req.phone())
            : Optional.empty();
        boolean isNew = existing.isEmpty();

        Patient patient;
        if (existing.isPresent()) {
            patient = existing.get();
            if (req.age() != null) patient.setAge(req.age());
        } else {
            if (!req.consent()) {
                throw new BadRequestException("Patient consent is required to register");
            }
            long seq = patientRepo.nextUhidSeq();
            patient = Patient.builder()
                .uhid(generateUhid(seq))
                .name(req.name())
                .phone(req.phone())
                .age(req.age())
                .gender(req.gender())
                .bloodGroup(req.bloodGroup())
                .abhaId(req.abhaId())
                .allergies(req.allergies())
                .consentAt(OffsetDateTime.now())
                .build();
        }
        patient = patientRepo.save(patient);
        String pid = patient.getId() != null ? patient.getId().toString() : null;
        auditService.log(isNew ? "CREATE" : "UPDATE", "PATIENT", pid, null);
        return toResponse(patient);
    }

    public Optional<PatientDto.Response> findByPhone(String phone) {
        return patientRepo.findByPhone(phone).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PatientDto.Response> listAll() {
        return patientRepo.findAllByOrderByNameAsc()
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<PatientDto.Response> search(String query) {
        return patientRepo.search(query)
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    public PatientDto.Response getById(UUID id) {
        Patient p = patientRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Patient not found: " + id));
        auditService.log("VIEW", "PATIENT", id.toString(), null);
        return toResponse(p);
    }

    /** DPDP export: a full bundle of the patient's data. */
    @Transactional(readOnly = true)
    public Map<String, Object> export(UUID id) {
        Patient p = patientRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Patient not found: " + id));

        List<Map<String, Object>> appts = appointmentRepo.findByPatientIdOrderByVisitDateDesc(id)
            .stream().map(a -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("visitDate", a.getVisitDate());
                m.put("tokenNumber", a.getTokenNumber());
                m.put("status", a.getStatus());
                m.put("visitType", a.getVisitType());
                return m;
            }).collect(Collectors.toList());

        List<Map<String, Object>> bills = billRepo.findByPatientId(id)
            .stream().map(b -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("invoiceNumber", b.getInvoiceNumber());
                m.put("total", b.getTotal());
                m.put("paymentMode", b.getPaymentMode());
                m.put("billedAt", b.getBilledAt());
                return m;
            }).collect(Collectors.toList());

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("uhid", p.getUhid());
        profile.put("name", p.getName());
        profile.put("phone", p.getPhone());
        profile.put("age", p.getAge());
        profile.put("gender", p.getGender());
        profile.put("bloodGroup", p.getBloodGroup());
        profile.put("allergies", p.getAllergies());
        profile.put("abhaId", p.getAbhaId());
        profile.put("consentAt", p.getConsentAt());

        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("patient", profile);
        bundle.put("appointments", appts);
        bundle.put("bills", bills);

        auditService.log("EXPORT", "PATIENT", id.toString(), null);
        return bundle;
    }

    /** DPDP erasure: scrub personal data but keep the row (clinical/financial retention). */
    @Transactional
    public void anonymize(UUID id) {
        Patient p = patientRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Patient not found: " + id));
        p.setName("[deleted]");
        p.setPhone(null);
        p.setAddress(null);
        p.setAbhaId(null);
        p.setAllergies(null);
        p.setDob(null);
        p.setGender(null);
        p.setDeletedAt(OffsetDateTime.now());
        patientRepo.save(p);
        auditService.log("DELETE", "PATIENT", id.toString(), "PII anonymized");
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private String generateUhid(long seq) {
        return String.format("CF-%05d", seq);
    }

    private PatientDto.Response toResponse(Patient p) {
        long visits = appointmentRepo.findByPatientIdOrderByVisitDateDesc(p.getId()).size();
        return new PatientDto.Response(
            p.getId(), p.getUhid(), p.getName(), p.getPhone(),
            p.getAge(), p.getGender(), p.getBloodGroup(),
            p.getAllergies(), visits
        );
    }
}
