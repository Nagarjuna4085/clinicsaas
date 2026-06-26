package com.clinicflow.service;

import com.clinicflow.context.TenantContext;
import com.clinicflow.dto.StaffDto;
import com.clinicflow.entity.global.StaffDirectory;
import com.clinicflow.entity.tenant.Staff;
import com.clinicflow.repository.global.StaffDirectoryRepository;
import com.clinicflow.repository.tenant.StaffRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StaffServiceTest {

    private StaffRepository staffRepo;
    private StaffDirectoryRepository directoryRepo;
    private StaffService service;

    @BeforeEach
    void setup() {
        staffRepo = mock(StaffRepository.class);
        directoryRepo = mock(StaffDirectoryRepository.class);
        service = new StaffService(staffRepo, directoryRepo, new BCryptPasswordEncoder());
        when(staffRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        TenantContext.set("tenant_9876543210");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createsStaffAndDirectoryEntry() {
        when(directoryRepo.findByPhone("9000000050")).thenReturn(Optional.empty());

        var req = new StaffDto.CreateRequest("Lakshmi", "9000000050", "RECEPTIONIST", null, null);
        StaffDto.CreatedResponse resp = service.create(req);

        assertThat(resp.name()).isEqualTo("Lakshmi");
        assertThat(resp.role()).isEqualTo("RECEPTIONIST");
        assertThat(resp.temporaryPassword()).isNotBlank();
        verify(directoryRepo).save(any(StaffDirectory.class));
    }

    @Test
    void rejectsDuplicatePhone() {
        when(directoryRepo.findByPhone("9000000050"))
            .thenReturn(Optional.of(StaffDirectory.builder().phone("9000000050").schemaName("tenant_x").build()));

        var req = new StaffDto.CreateRequest("Lakshmi", "9000000050", "RECEPTIONIST", null, null);
        assertThatThrownBy(() -> service.create(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("already");
    }

    @Test
    void deactivateMarksInactiveAndRemovesDirectory() {
        Staff s = Staff.builder().name("Gone").phone("9000000099").role("NURSE").isActive(true).build();
        UUID id = UUID.randomUUID();
        when(staffRepo.findById(id)).thenReturn(Optional.of(s));

        service.deactivate(id);

        assertThat(s.isActive()).isFalse();
        verify(directoryRepo).deleteByPhone("9000000099");
    }
}
