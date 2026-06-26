package com.clinicflow.config;

import com.clinicflow.entity.global.Tenant;
import com.clinicflow.repository.global.TenantRepository;
import com.clinicflow.service.TenantProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * On startup, applies the tenant Flyway migrations to EVERY existing clinic
 * schema — so new migrations (e.g. added columns) reach already-provisioned
 * clinics automatically, not just new sign-ups. Idempotent and resilient: a
 * failure on one clinic is logged and doesn't stop the others or app startup.
 */
@Component
@Order(1)
public class TenantMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantMigrationRunner.class);

    private final TenantRepository tenantRepository;
    private final TenantProvisioningService provisioningService;

    public TenantMigrationRunner(TenantRepository tenantRepository,
                                 TenantProvisioningService provisioningService) {
        this.tenantRepository = tenantRepository;
        this.provisioningService = provisioningService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Tenant> tenants = tenantRepository.findAll();
        if (tenants.isEmpty()) {
            log.info("No tenant schemas to migrate.");
            return;
        }
        log.info("Applying tenant migrations to {} clinic schema(s)...", tenants.size());
        int ok = 0, failed = 0;
        for (Tenant t : tenants) {
            try {
                provisioningService.migrate(t.getSchemaName());
                ok++;
            } catch (Exception e) {
                failed++;
                log.error("Tenant migration failed for schema {}: {}", t.getSchemaName(), e.getMessage());
            }
        }
        log.info("Tenant migrations complete: {} succeeded, {} failed.", ok, failed);
    }
}
