package com.clinicflow.service;

import com.clinicflow.dto.PatientDto;
import com.clinicflow.entity.tenant.Patient;
import com.clinicflow.repository.tenant.AppointmentRepository;
import com.clinicflow.repository.tenant.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepo;
    private final AppointmentRepository appointmentRepo;

    public PatientService(PatientRepository patientRepo,
                          AppointmentRepository appointmentRepo) {
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
    }

    @Transactional
    public PatientDto.Response registerOrUpdate(PatientDto.RegisterRequest req) {
        // Check if patient already exists by phone
        Optional<Patient> existing = (req.phone() != null)
            ? patientRepo.findByPhone(req.phone())
            : Optional.empty();

        Patient patient;
        if (existing.isPresent()) {
            // Update name/age if changed
            patient = existing.get();
            if (req.age() != null) patient.setAge(req.age());
        } else {
            // New patient — generate UHID from an atomic per-clinic sequence
            long seq = patientRepo.nextUhidSeq();
            String uhid = generateUhid(seq);

            patient = Patient.builder()
                .uhid(uhid)
                .name(req.name())
                .phone(req.phone())
                .age(req.age())
                .gender(req.gender())
                .bloodGroup(req.bloodGroup())
                .abhaId(req.abhaId())
                .allergies(req.allergies())
                .build();
        }
        patient = patientRepo.save(patient);
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
            .orElseThrow(() -> new RuntimeException("Patient not found: " + id));
        return toResponse(p);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Generates UHID like VJW-00042.
     * Prefix can be made configurable per clinic later.
     */
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
