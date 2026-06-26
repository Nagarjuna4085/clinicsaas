package com.clinicflow.service;

import com.clinicflow.dto.PatientDto;
import com.clinicflow.entity.tenant.Patient;
import com.clinicflow.repository.tenant.AppointmentRepository;
import com.clinicflow.repository.tenant.BillRepository;
import com.clinicflow.repository.tenant.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    private PatientRepository patientRepo;
    private AppointmentRepository appointmentRepo;
    private BillRepository billRepo;
    private PatientService service;

    @BeforeEach
    void setup() {
        patientRepo = mock(PatientRepository.class);
        appointmentRepo = mock(AppointmentRepository.class);
        billRepo = mock(BillRepository.class);
        var audit = mock(com.clinicflow.service.AuditService.class);
        service = new PatientService(patientRepo, appointmentRepo, billRepo, audit);
        when(appointmentRepo.findByPatientIdOrderByVisitDateDesc(any())).thenReturn(List.of());
        when(patientRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void newPatientGetsSequencedUhid() {
        when(patientRepo.findByPhone("9000000001")).thenReturn(Optional.empty());
        when(patientRepo.nextUhidSeq()).thenReturn(1L);

        var req = new PatientDto.RegisterRequest("Sita Devi", "9000000001", (short) 34, "F", null, null, null, true);
        PatientDto.Response resp = service.registerOrUpdate(req);

        assertThat(resp.uhid()).isEqualTo("CF-00001");
        assertThat(resp.name()).isEqualTo("Sita Devi");
        assertThat(resp.totalVisits()).isEqualTo(0);
    }

    @Test
    void existingPatientKeepsUhid() {
        Patient existing = Patient.builder()
            .uhid("CF-00009").name("Old Name").phone("9000000001").build();
        when(patientRepo.findByPhone("9000000001")).thenReturn(Optional.of(existing));

        var req = new PatientDto.RegisterRequest("Old Name", "9000000001", (short) 40, "F", null, null, null, true);
        PatientDto.Response resp = service.registerOrUpdate(req);

        assertThat(resp.uhid()).isEqualTo("CF-00009");
        verify(patientRepo, never()).nextUhidSeq();
    }
}
