package com.clinicflow.service;

import com.clinicflow.dto.VitalsDto;
import com.clinicflow.entity.tenant.Appointment;
import com.clinicflow.entity.tenant.Staff;
import com.clinicflow.entity.tenant.Vitals;
import com.clinicflow.repository.tenant.AppointmentRepository;
import com.clinicflow.repository.tenant.StaffRepository;
import com.clinicflow.repository.tenant.VitalsRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class VitalsService {

    private final VitalsRepository vitalsRepo;
    private final AppointmentRepository appointmentRepo;
    private final StaffRepository staffRepo;

    public VitalsService(VitalsRepository vitalsRepo,
                         AppointmentRepository appointmentRepo,
                         StaffRepository staffRepo) {
        this.vitalsRepo = vitalsRepo;
        this.appointmentRepo = appointmentRepo;
        this.staffRepo = staffRepo;
    }

    /** Records (or updates) the vitals for an appointment. */
    @Transactional
    public VitalsDto.Response record(VitalsDto.CreateRequest req) {
        Appointment appt = appointmentRepo.findById(req.appointmentId())
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Vitals v = vitalsRepo.findByAppointmentId(req.appointmentId())
            .orElseGet(() -> Vitals.builder().appointment(appt).build());

        v.setBpSystolic(req.bpSystolic());
        v.setBpDiastolic(req.bpDiastolic());
        v.setPulse(req.pulse());
        v.setTemperature(req.temperature());
        v.setSpo2(req.spo2());
        v.setWeightKg(req.weightKg());
        v.setHeightCm(req.heightCm());
        v.setRecordedBy(currentStaff());
        v.setRecordedAt(OffsetDateTime.now());

        return toResponse(vitalsRepo.save(v));
    }

    @Transactional(readOnly = true)
    public VitalsDto.Response get(UUID appointmentId) {
        return vitalsRepo.findByAppointmentId(appointmentId).map(this::toResponse).orElse(null);
    }

    private Staff currentStaff() {
        try {
            String id = SecurityContextHolder.getContext().getAuthentication().getName();
            return staffRepo.findById(UUID.fromString(id)).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private VitalsDto.Response toResponse(Vitals v) {
        return new VitalsDto.Response(
            v.getId(), v.getBpSystolic(), v.getBpDiastolic(), v.getPulse(),
            v.getTemperature(), v.getSpo2(), v.getWeightKg(), v.getHeightCm(),
            v.getRecordedBy() != null ? v.getRecordedBy().getName() : null,
            v.getRecordedAt() != null ? v.getRecordedAt().toString() : null
        );
    }
}
