package com.clinicflow.repository.tenant;

import com.clinicflow.entity.tenant.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {

    // Today's revenue
    @Query("SELECT COALESCE(SUM(b.total), 0) FROM Bill b WHERE b.billedAt >= :start AND b.billedAt < :end")
    BigDecimal sumTotalBetween(OffsetDateTime start, OffsetDateTime end);

    // Today's bills for display
    @Query("SELECT b FROM Bill b JOIN FETCH b.patient WHERE b.billedAt >= :start ORDER BY b.billedAt DESC")
    List<Bill> findTodaysBills(OffsetDateTime start);

    // Next invoice number for this clinic — atomic, concurrency-safe.
    // Uses a per-schema Postgres sequence (see V2 tenant migration) instead of
    // COUNT(*), which could produce duplicate invoice numbers under load.
    @Query(value = "SELECT nextval('bill_invoice_seq')", nativeQuery = true)
    long nextInvoiceSeq();

    List<Bill> findByPatientId(UUID patientId);
    List<Bill> findByAppointmentId(UUID appointmentId);
}
