package com.clinicflow.service;

import com.clinicflow.context.TenantContext;
import com.clinicflow.dto.StaffDto;
import com.clinicflow.entity.global.StaffDirectory;
import com.clinicflow.entity.tenant.Staff;
import com.clinicflow.exception.BadRequestException;
import com.clinicflow.exception.NotFoundException;
import com.clinicflow.repository.global.StaffDirectoryRepository;
import com.clinicflow.repository.tenant.StaffRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StaffService {

    private static final String PW_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789abcdefghijkmnpqrstuvwxyz";
    private static final SecureRandom RAND = new SecureRandom();

    private final StaffRepository staffRepo;
    private final StaffDirectoryRepository directoryRepo;
    private final PasswordEncoder passwordEncoder;

    public StaffService(StaffRepository staffRepo,
                        StaffDirectoryRepository directoryRepo,
                        PasswordEncoder passwordEncoder) {
        this.staffRepo = staffRepo;
        this.directoryRepo = directoryRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a staff member in the current clinic's schema with a temporary
     * password (returned once so the admin can share it) and registers their
     * phone in the global directory so they can log in.
     */
    @Transactional
    public StaffDto.CreatedResponse create(StaffDto.CreateRequest req) {
        String schema = TenantContext.get();
        if (schema == null || schema.isBlank() || "global".equals(schema)) {
            throw new BadRequestException("No clinic context for staff creation");
        }
        if (directoryRepo.findByPhone(req.phone()).isPresent()) {
            throw new BadRequestException("Phone already registered to a clinic");
        }

        String tempPassword = randomTempPassword();
        Staff staff = staffRepo.save(Staff.builder()
            .name(req.name())
            .phone(req.phone())
            .role(req.role().toUpperCase())
            .regNumber(req.regNumber())
            .specialty(req.specialty())
            .passwordHash(passwordEncoder.encode(tempPassword))
            .mustResetPassword(true)
            .isActive(true)
            .build());

        directoryRepo.save(StaffDirectory.builder()
            .phone(req.phone())
            .schemaName(schema)
            .build());

        return new StaffDto.CreatedResponse(
            staff.getId(), staff.getName(), staff.getPhone(),
            staff.getRole(), staff.getSpecialty(), tempPassword);
    }

    @Transactional(readOnly = true)
    public List<StaffDto.Response> listActive() {
        return staffRepo.findByIsActiveTrue()
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public StaffDto.Response update(UUID id, StaffDto.UpdateRequest req) {
        Staff s = staffRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Staff not found"));
        s.setName(req.name());
        s.setRole(req.role().toUpperCase());
        s.setRegNumber(req.regNumber());
        s.setSpecialty(req.specialty());
        return toResponse(staffRepo.save(s));
    }

    /** Soft-deactivate: marks inactive and frees the phone (removes the login directory entry). */
    @Transactional
    public void deactivate(UUID id) {
        Staff s = staffRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Staff not found"));
        s.setActive(false);
        staffRepo.save(s);
        directoryRepo.deleteByPhone(s.getPhone());
    }

    private StaffDto.Response toResponse(Staff s) {
        return new StaffDto.Response(
            s.getId(), s.getName(), s.getPhone(),
            s.getRole(), s.getSpecialty(), s.isActive());
    }

    private static String randomTempPassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(PW_CHARS.charAt(RAND.nextInt(PW_CHARS.length())));
        return sb.toString();
    }
}
