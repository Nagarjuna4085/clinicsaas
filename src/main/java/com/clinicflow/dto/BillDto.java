package com.clinicflow.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class BillDto {

    public record CreateRequest(
        @NotNull UUID appointmentId,
        String paymentMode,
        List<LineItem> items
    ) {
        record LineItem(
            String description,
            String hsnSac,
            BigDecimal amount,
            BigDecimal gstRate
        ) {}
    }

    public record DashboardSummary(
        BigDecimal totalRevenue,
        long billCount,
        BigDecimal cashTotal,
        BigDecimal upiTotal,
        BigDecimal cardTotal,
        int patientsToday,
        int waitingCount
    ) {}
}
