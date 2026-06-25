package com.clinicflow.service;

import com.clinicflow.entity.global.Tenant;
import com.clinicflow.repository.global.TenantRepository;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Provisions a new clinic:
 * 1. Creates a PostgreSQL schema (tenant_xyz)
 * 2. Runs Flyway migrations on that schema (creates all 10 tables)
 * 3. Saves the tenant in global.tenants
 */
@Service
public class TenantProvisioningService {

    private final DataSource dataSource;
    private final TenantRepository tenantRepository;

    public TenantProvisioningService(DataSource dataSource,
                                     TenantRepository tenantRepository) {
        this.dataSource = dataSource;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant provisionNewClinic(String clinicName, String ownerPhone,
                                     String city, String plan) {
        // 1. Generate a safe schema name from clinic name
        String schemaName = generateSchemaName(ownerPhone);

        if (tenantRepository.existsBySchemaName(schemaName)) {
            throw new RuntimeException("Clinic already registered with this phone");
        }

        // 2. Create PostgreSQL schema
        createSchema(schemaName);

        // 3. Run Flyway migrations on the new schema
        runMigrations(schemaName);

        // 4. Save tenant record in global.tenants
        Tenant tenant = Tenant.builder()
            .schemaName(schemaName)
            .clinicName(clinicName)
            .ownerPhone(ownerPhone)
            .city(city)
            .plan(plan)
            .status("trial")
            .build();

        return tenantRepository.save(tenant);
    }

    private void createSchema(String schemaName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Safe: schemaName is generated internally, not user input
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create schema: " + schemaName, e);
        }
    }

    private void runMigrations(String schemaName) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .schemas(schemaName)
            .locations("classpath:db/migration/tenant") // tenant-specific SQL files
            .baselineOnMigrate(true)
            .load();
        flyway.migrate();
    }

    /**
     * Generates schema name from phone number.
     * Phone 9876543210 → tenant_9876543210
     * Safe, unique, predictable.
     */
    private String generateSchemaName(String phone) {
        return "tenant_" + phone.replaceAll("[^a-zA-Z0-9]", "_");
    }
}
