package com.clinicflow.service;

import com.clinicflow.context.TenantContext;
import com.clinicflow.dto.ClinicDto;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.repository.global.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read/update the current clinic's profile (the global.tenants row). */
@Service
public class ClinicService {

    private final TenantRepository tenantRepo;

    public ClinicService(TenantRepository tenantRepo) {
        this.tenantRepo = tenantRepo;
    }

    @Transactional(readOnly = true)
    public ClinicDto.Profile getProfile() {
        return toProfile(current());
    }

    @Transactional
    public ClinicDto.Profile update(ClinicDto.UpdateRequest req) {
        Tenant t = current();
        t.setClinicName(req.clinicName());
        t.setCity(req.city());
        return toProfile(tenantRepo.save(t));
    }

    private Tenant current() {
        String schema = TenantContext.get();
        return tenantRepo.findBySchemaName(schema)
            .orElseThrow(() -> new RuntimeException("Clinic not found"));
    }

    private ClinicDto.Profile toProfile(Tenant t) {
        return new ClinicDto.Profile(
            t.getClinicName(), t.getOwnerPhone(), t.getCity(), t.getPlan(),
            t.getStatus(), t.getSchemaName(),
            t.getTrialEndsAt() != null ? t.getTrialEndsAt().toString() : null);
    }
}
