package com.clinicflow.service;

import com.clinicflow.dto.BillDto;
import com.clinicflow.entity.tenant.Appointment;
import com.clinicflow.entity.tenant.Bill;
import com.clinicflow.repository.tenant.AppointmentRepository;
import com.clinicflow.repository.tenant.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final BillRepository billRepo;
    private final AppointmentRepository appointmentRepo;

    public DashboardService(BillRepository billRepo, AppointmentRepository appointmentRepo) {
        this.billRepo = billRepo;
        this.appointmentRepo = appointmentRepo;
    }

    @Transactional(readOnly = true)
    public BillDto.DashboardSummary summary() {
        List<Bill> bills = billRepo.findTodaysBills(BillService.startOfToday());

        BigDecimal total = BigDecimal.ZERO, cash = BigDecimal.ZERO,
                   upi = BigDecimal.ZERO, card = BigDecimal.ZERO;
        for (Bill b : bills) {
            BigDecimal amt = nz(b.getTotal());
            total = total.add(amt);
            switch (b.getPaymentMode() == null ? "" : b.getPaymentMode().toUpperCase()) {
                case "CASH" -> cash = cash.add(amt);
                case "UPI"  -> upi = upi.add(amt);
                case "CARD" -> card = card.add(amt);
                default -> { /* other/unspecified */ }
            }
        }

        List<Appointment> appts =
            appointmentRepo.findByVisitDateOrderByTokenNumber(LocalDate.now());
        int patientsToday = (int) appts.stream()
            .filter(a -> a.getPatient() != null)
            .map(a -> a.getPatient().getId())
            .distinct().count();
        int waiting = (int) appts.stream()
            .filter(a -> "WAITING".equals(a.getStatus())).count();

        return new BillDto.DashboardSummary(
            total, bills.size(), cash, upi, card, patientsToday, waiting);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
