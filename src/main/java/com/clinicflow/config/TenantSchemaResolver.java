package com.clinicflow.config;

import com.clinicflow.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Tells Hibernate which PostgreSQL schema to use for the current request.
 * Hibernate calls this before every DB operation.
 */
@Component
public class TenantSchemaResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_SCHEMA = "global";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.get();
        return (tenant != null && !tenant.isBlank()) ? tenant : DEFAULT_SCHEMA;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
