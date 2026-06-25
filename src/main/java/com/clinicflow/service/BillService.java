package com.clinicflow.service;

import com.clinicflow.dto.BillDto;
import com.clinicflow.entity.tenant.Bill;
import com.clinicflow.repository.tenant.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepo;

    public BillService(BillRepository billRepo) {
        this.billRepo = billRepo;
    }

    @Transactional(readOnly = true)
    public List<BillDto.Response> today() {
        return billRepo.findTodaysBills(startOfToday())
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillDto.Response get(UUID id) {
        Bill bill = billRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Bill not found: " + id));
        return toResponse(bill);
    }

    private BillDto.Response toResponse(Bill b) {
        List<BillDto.Response.Item> items = b.getItems().stream()
            .map(i -> new BillDto.Response.Item(
                i.getDescription(), i.getHsnSac(), i.getAmount(), i.getGstRate()))
            .collect(Collectors.toList());

        return new BillDto.Response(
            b.getId(),
            b.getInvoiceNumber(),
            b.getPatient() != null ? b.getPatient().getName() : null,
            b.getSubtotal(),
            b.getCgst(),
            b.getSgst(),
            b.getTotal(),
            b.getPaymentMode(),
            b.getStatus(),
            b.getBilledAt() != null ? b.getBilledAt().toString() : null,
            items
        );
    }

    /** Start of the current day in the server's offset (bills use TIMESTAMPTZ). */
    static OffsetDateTime startOfToday() {
        return LocalDate.now().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
    }
}
