package com.clinicflow.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    private final TenantConnectionProvider connectionProvider;
    private final TenantSchemaResolver schemaResolver;

    public HibernateConfig(TenantConnectionProvider connectionProvider,
                           TenantSchemaResolver schemaResolver) {
        this.connectionProvider = connectionProvider;
        this.schemaResolver = schemaResolver;
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return props -> {
            props.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
            props.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, schemaResolver);
        };
    }
}
