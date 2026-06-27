package com.clinicflow.service;

import com.clinicflow.dto.ReportDto;
import com.clinicflow.entity.tenant.Appointment;
import com.clinicflow.entity.tenant.Bill;
import com.clinicflow.repository.tenant.AppointmentRepository;
import com.clinicflow.repository.tenant.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final BillRepository billRepo;
    private final AppointmentRepository appointmentRepo;

    public ReportService(BillRepository billRepo, AppointmentRepository appointmentRepo) {
        this.billRepo = billRepo;
        this.appointmentRepo = appointmentRepo;
    }

    /** Daily revenue + visit counts for the last {@code days} days (inclusive of today). */
    @Transactional(readOnly = true)
    public ReportDto.RevenueReport revenue(int days) {
        int n = Math.max(1, Math.min(days, 365));
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(n - 1L);
        OffsetDateTime fromTs = from.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());

        // Seed every day in range with zeros so the series has no gaps.
        Map<String, BigDecimal> revByDay = new LinkedHashMap<>();
        Map<String, Integer> visByDay = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            String key = from.plusDays(i).toString();
            revByDay.put(key, BigDecimal.ZERO);
            visByDay.put(key, 0);
        }

        var localOffset = OffsetDateTime.now().getOffset();
        for (Bill b : billRepo.findByBilledAtGreaterThanEqual(fromTs)) {
            if (b.getBilledAt() == null) continue;
            String key = b.getBilledAt().withOffsetSameInstant(localOffset).toLocalDate().toString();
            // Only count keys we seeded (guards against edge off-by-one).
            revByDay.computeIfPresent(key, (k, v) -> v.add(nz(b.getTotal())));
        }
        for (Appointment a : appointmentRepo.findByVisitDateGreaterThanEqual(from)) {
            if (a.getVisitDate() == null) continue;
            visByDay.computeIfPresent(a.getVisitDate().toString(), (k, v) -> v + 1);
        }

        List<ReportDto.DailyPoint> series = new ArrayList<>();
        BigDecimal totalRev = BigDecimal.ZERO;
        int totalVis = 0;
        for (String key : revByDay.keySet()) {
            BigDecimal rev = revByDay.get(key);
            int vis = visByDay.getOrDefault(key, 0);
            series.add(new ReportDto.DailyPoint(key, rev, vis));
            totalRev = totalRev.add(rev);
            totalVis += vis;
        }
        return new ReportDto.RevenueReport(n, totalRev, totalVis, series);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
