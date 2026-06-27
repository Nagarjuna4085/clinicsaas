package com.clinicflow;

import com.clinicflow.context.TenantContext;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.entity.tenant.Patient;
import com.clinicflow.repository.tenant.PatientRepository;
import com.clinicflow.service.TenantProvisioningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.OffsetDateTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test against a real PostgreSQL (Testcontainers): proves that the
 * schema-per-tenant routing actually isolates clinics — a patient saved in one
 * clinic's schema is invisible in another's. Runs in `mvn verify` (needs Docker).
 */
@SpringBootTest
@Testcontainers
class MultiTenantRoutingIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("clinicflow");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired TenantProvisioningService provisioning;
    @Autowired PatientRepository patientRepo;

    @Test
    void patientsAreIsolatedPerClinic() {
        Tenant a = provisioning.provisionNewClinic("Clinic A", "9111111111", "CityA", "starter");
        Tenant b = provisioning.provisionNewClinic("Clinic B", "9222222222", "CityB", "starter");

        // Save a patient into Clinic A's schema.
        runIn(a.getSchemaName(), () ->
            patientRepo.save(Patient.builder()
                .uhid("A-0001").name("Alice").consentAt(OffsetDateTime.now()).build()));

        long inA = countIn(a.getSchemaName());
        long inB = countIn(b.getSchemaName());

        assertThat(inA).isEqualTo(1);  // visible in its own clinic
        assertThat(inB).isEqualTo(0);  // NOT visible in the other clinic
    }

    private long countIn(String schema) {
        TenantContext.set(schema);
        try {
            return patientRepo.count();
        } finally {
            TenantContext.clear();
        }
    }

    private void runIn(String schema, Runnable action) {
        TenantContext.set(schema);
        try {
            action.run();
        } finally {
            TenantContext.clear();
        }
    }
}
