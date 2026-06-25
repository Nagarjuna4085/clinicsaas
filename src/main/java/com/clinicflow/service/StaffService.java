package com.clinicflow.service;

import com.clinicflow.context.TenantContext;
import com.clinicflow.dto.StaffDto;
import com.clinicflow.entity.global.StaffDirectory;
import com.clinicflow.entity.tenant.Staff;
import com.clinicflow.repository.global.StaffDirectoryRepository;
import com.clinicflow.repository.tenant.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffService {

    private final StaffRepository staffRepo;
    private final StaffDirectoryRepository directoryRepo;

    public StaffService(StaffRepository staffRepo,
                        StaffDirectoryRepository directoryRepo) {
        this.staffRepo = staffRepo;
        this.directoryRepo = directoryRepo;
    }

    /**
     * Creates a staff member in the current clinic's schema and registers their
     * phone in the global directory so they can OTP-login. The current schema is
     * taken from TenantContext (set by JwtAuthFilter from the caller's JWT).
     */
    @Transactional
    public StaffDto.Response create(StaffDto.CreateRequest req) {
        String schema = TenantContext.get();
        if (schema == null || schema.isBlank() || "global".equals(schema)) {
            throw new RuntimeException("No clinic context for staff creation");
        }

        // A phone can belong to only one clinic (it's the login identifier).
        if (directoryRepo.findByPhone(req.phone()).isPresent()) {
            throw new RuntimeException("Phone already registered to a clinic");
        }

        Staff staff = staffRepo.save(Staff.builder()
            .name(req.name())
            .phone(req.phone())
            .role(req.role().toUpperCase())
            .regNumber(req.regNumber())
            .specialty(req.specialty())
            .isActive(true)
            .build());

        // global.staff_directory is schema-qualified on the entity, so this
        // insert targets the global schema even though search_path is the tenant.
        directoryRepo.save(StaffDirectory.builder()
            .phone(req.phone())
            .schemaName(schema)
            .build());

        return toResponse(staff);
    }

    @Transactional(readOnly = true)
    public List<StaffDto.Response> listActive() {
        return staffRepo.findByIsActiveTrue()
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    private StaffDto.Response toResponse(Staff s) {
        return new StaffDto.Response(
            s.getId(), s.getName(), s.getPhone(),
            s.getRole(), s.getSpecialty(), s.isActive());
    }
}
