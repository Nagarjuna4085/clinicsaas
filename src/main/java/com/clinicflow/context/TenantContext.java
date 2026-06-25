package com.clinicflow.context;

/**
 * Thread-local store for the current clinic's schema name.
 * Set by JwtAuthFilter on every request. Cleared after response.
 * Example value: "tenant_ramesh_vjw"
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(String schemaName) { CURRENT.set(schemaName); }
    public static String get()               { return CURRENT.get(); }
    public static void clear()               { CURRENT.remove(); }
}
