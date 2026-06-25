package com.clinicflow.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Switches the PostgreSQL search_path to the tenant's schema before handing a
 * connection to Hibernate.
 *
 * NOTE: this class is deliberately NOT named MultiTenantConnectionProvider —
 * that would clash with the Hibernate interface of the same simple name it
 * implements (you cannot import a type whose simple name matches a top-level
 * class in the same file).
 */
@Component
public class TenantConnectionProvider
        implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public TenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection conn = getAnyConnection();
        // Set search_path so Hibernate finds the right schema.
        // tenantIdentifier is an internally-generated schema name (never raw
        // user input), but we still guard against unexpected values.
        String schema = sanitizeSchema(tenantIdentifier);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO " + schema + ", public");
        }
        return conn;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection)
            throws SQLException {
        // Reset to global before returning to pool
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO global, public");
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    // ── Wrapped contract (required by Hibernate 6's MultiTenantConnectionProvider) ──

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return unwrapType != null && unwrapType.isInstance(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> unwrapType) {
        return isUnwrappableAs(unwrapType) ? (T) this : null;
    }

    /**
     * Defensive whitelist: schema names are generated as tenant_<digits> or
     * 'global', so anything outside [A-Za-z0-9_] is rejected to prevent any
     * possibility of search_path injection via a forged tenant identifier.
     */
    private String sanitizeSchema(String schema) {
        if (schema == null || !schema.matches("[A-Za-z0-9_]+")) {
            return "global";
        }
        return schema;
    }
}
