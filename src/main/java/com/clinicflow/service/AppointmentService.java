package com.clinicflow.service;

import com.clinicflow.dto.AppointmentDto;
import com.clinicflow.entity.tenant.Appointment;
import com.clinicflow.entity.tenant.Bill;
import com.clinicflow.entity.tenant.BillItem;
import com.clinicflow.entity.tenant.Patient;
import com.clinicflow.entity.tenant.Staff;
import com.clinicflow.repository.tenant.AppointmentRepository;
import com.clinicflow.repository.tenant.BillRepository;
import com.clinicflow.repository.tenant.PatientRepository;
import com.clinicflow.repository.tenant.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;
    private final StaffRepository staffRepo;
    private final BillRepository billRepo;

    public AppointmentService(AppointmentRepository appointmentRepo,
                               PatientRepository patientRepo,
                               StaffRepository staffRepo,
                               BillRepository billRepo) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.staffRepo = staffRepo;
        this.billRepo = billRepo;
    }

    /**
     * Register patient for today's queue.
     * Issues token number and creates the OP fee bill in one transaction.
     */
    @Transactional
    public AppointmentDto.QueueItem createAppointment(AppointmentDto.CreateRequest req) {
        Patient patient = patientRepo.findById(req.patientId())
            .orElseThrow(() -> new RuntimeException("Patient not found"));

        Staff doctor = staffRepo.findById(req.doctorId())
            .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Get next token for today
        short token = appointmentRepo.nextTokenNumber(LocalDate.now(), req.doctorId());

        Appointment appt = Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .tokenNumber(token)
            .visitDate(LocalDate.now())
            .visitType(req.visitType() != null ? req.visitType() : "WALKIN")
            .status("WAITING")
            .build();

        appt = appointmentRepo.save(appt);

        // Create OP fee bill immediately
        if (req.opFee() > 0) {
            createOpFeeBill(appt, patient, req.opFee(), req.paymentMode());
        }

        return toQueueItem(appt);
    }

    // readOnly transaction keeps the session open while we map lazy patient
    // fields in toQueueItem (required now that Open-Session-In-View is disabled).
    @Transactional(readOnly = true)
    public List<AppointmentDto.QueueItem> getTodaysQueue(UUID doctorId) {
        return appointmentRepo
            .findByVisitDateAndDoctorIdOrderByTokenNumber(LocalDate.now(), doctorId)
            .stream().map(this::toQueueItem)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto.QueueItem> getAllTodaysAppointments() {
        return appointmentRepo
            .findByVisitDateOrderByTokenNumber(LocalDate.now())
            .stream().map(this::toQueueItem)
            .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentDto.QueueItem updateStatus(UUID id, String status) {
        Appointment appt = appointmentRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appt.setStatus(status);
        return toQueueItem(appointmentRepo.save(appt));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void createOpFeeBill(Appointment appt, Patient patient, int fee, String mode) {
        long seq = billRepo.nextInvoiceSeq();
        String invoiceNo = String.format("INV-%d-%05d",
            LocalDate.now().getYear(), seq);

        BillItem item = BillItem.builder()
            .description("Consultation fee")
            .hsnSac("999312")           // SAC code for medical consultation
            .amount(BigDecimal.valueOf(fee))
            .gstRate(BigDecimal.ZERO)   // Consultation is GST exempt
            .build();

        Bill bill = Bill.builder()
            .invoiceNumber(invoiceNo)
            .appointment(appt)
            .patient(patient)
            .subtotal(BigDecimal.valueOf(fee))
            .cgst(BigDecimal.ZERO)
            .sgst(BigDecimal.ZERO)
            .total(BigDecimal.valueOf(fee))
            .paymentMode(mode != null ? mode.toUpperCase() : "CASH")
            .status("PAID")
            .build();

        item.setBill(bill);
        bill.getItems().add(item);
        billRepo.save(bill);
    }

    private AppointmentDto.QueueItem toQueueItem(Appointment a) {
        return new AppointmentDto.QueueItem(
            a.getId(),
            a.getTokenNumber(),
            a.getPatient().getName(),
            a.getPatient().getAge(),
            a.getPatient().getGender(),
            a.getStatus(),
            a.getVisitType(),
            a.getCreatedAt().toString()
        );
    }
}
