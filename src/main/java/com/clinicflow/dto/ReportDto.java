package com.clinicflow.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReportDto {

    public record DailyPoint(
        String date,        // yyyy-MM-dd
        BigDecimal revenue,
        int visits
    ) {}

    public record RevenueReport(
        int days,
        BigDecimal totalRevenue,
        int totalVisits,
        List<DailyPoint> series
    ) {}
}
