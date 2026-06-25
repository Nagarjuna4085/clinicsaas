package com.clinicflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 *
 * Exposes interactive docs at:
 *   - Swagger UI : http://localhost:8080/swagger-ui.html
 *   - JSON spec  : http://localhost:8080/v3/api-docs
 *
 * Declares a JWT bearer security scheme so the "Authorize" button in Swagger UI
 * lets you paste a token (from /api/auth/verify-otp) and call secured endpoints.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clinicFlowOpenAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("ClinicFlow API")
                .version("1.0.0")
                .description("""
                    Multi-tenant clinic management SaaS for India.

                    Each clinic lives in its own PostgreSQL schema (schema-per-tenant).
                    Authenticate via phone OTP at /api/auth, then send the returned JWT
                    as `Authorization: Bearer <token>` — the token's tenantId selects the
                    clinic schema automatically for every request.

                    Auth flow: register a clinic (POST /api/public/clinics/register) →
                    send-otp → verify-otp → use the JWT below via Authorize.""")
                .contact(new Contact().name("ClinicFlow").email("support@clinicflow.in")))
            // Apply the bearer scheme globally; public endpoints still work without it.
            .addSecurityItem(new SecurityRequirement().addList(schemeName))
            .components(new Components().addSecuritySchemes(schemeName,
                new SecurityScheme()
                    .name(schemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
