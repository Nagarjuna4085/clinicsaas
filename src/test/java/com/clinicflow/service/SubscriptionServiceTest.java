package com.clinicflow.service;

import com.clinicflow.context.TenantContext;
import com.clinicflow.dto.SubscriptionDto;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.repository.global.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {

    private final TenantRepository tenantRepo = mock(TenantRepository.class);
    private final RazorpayService razorpay = mock(RazorpayService.class);

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void devSetStatusDisabledThrows() {
        SubscriptionService svc = new SubscriptionService(tenantRepo, razorpay, false);
        assertThatThrownBy(() -> svc.devSetStatus("active"))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void devSetStatusRejectsInvalidStatus() {
        SubscriptionService svc = new SubscriptionService(tenantRepo, razorpay, true);
        assertThatThrownBy(() -> svc.devSetStatus("bogus"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void devSetStatusUpdatesTenant() {
        SubscriptionService svc = new SubscriptionService(tenantRepo, razorpay, true);
        TenantContext.set("tenant_9876543210");
        Tenant t = Tenant.builder()
            .schemaName("tenant_9876543210").clinicName("Clinic").plan("clinic").status("trial").build();
        when(tenantRepo.findBySchemaName("tenant_9876543210")).thenReturn(Optional.of(t));
        when(tenantRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        SubscriptionDto.Status status = svc.devSetStatus("active");

        assertThat(t.getStatus()).isEqualTo("active");
        assertThat(status.status()).isEqualTo("active");
        assertThat(status.devTools()).isTrue();
    }
}
