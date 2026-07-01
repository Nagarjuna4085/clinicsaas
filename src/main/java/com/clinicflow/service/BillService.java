package com.clinicflow.service;

import com.clinicflow.context.TenantContext;
import com.clinicflow.dto.BillDto;
import com.clinicflow.entity.global.Tenant;
import com.clinicflow.entity.tenant.Bill;
import com.clinicflow.exception.NotFoundException;
import com.clinicflow.repository.global.TenantRepository;
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
    private final TenantRepository tenantRepo;
    private final PdfService pdfService;

    public BillService(BillRepository billRepo, TenantRepository tenantRepo, PdfService pdfService) {
        this.billRepo = billRepo;
        this.tenantRepo = tenantRepo;
        this.pdfService = pdfService;
    }

    @Transactional(readOnly = true)
    public byte[] pdf(UUID id) {
        Bill b = billRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Bill not found: " + id));
        List<PdfService.BillLine> lines = b.getItems().stream()
            .map(i -> new PdfService.BillLine(i.getDescription(), i.getHsnSac(), i.getAmount()))
            .collect(Collectors.toList());
        return pdfService.invoicePdf(
            clinicName(),
            b.getInvoiceNumber(),
            b.getPatient() != null ? b.getPatient().getName() : null,
            b.getBilledAt() != null ? b.getBilledAt().toString() : null,
            lines, b.getSubtotal(), b.getCgst(), b.getSgst(), b.getTotal(), b.getPaymentMode());
    }

    private String clinicName() {
        String schema = TenantContext.get();
        if (schema == null) return "Clinic";
        return tenantRepo.findBySchemaName(schema).map(Tenant::getClinicName).orElse("Clinic");
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
            .orElseThrow(() -> new NotFoundException("Bill not found: " + id));
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
