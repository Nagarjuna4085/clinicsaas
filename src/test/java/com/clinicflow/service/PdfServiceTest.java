package com.clinicflow.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class PdfServiceTest {

    private final PdfService pdf = new PdfService();

    @Test
    void buildsPrescriptionPdf() {
        byte[] out = pdf.prescriptionPdf(
            "Dr. Ramesh Clinic", "Dr. Ramesh", "Sita Devi", "2026-06-25",
            "Fever", "Viral fever", "Rest and fluids", "2026-07-01",
            List.of(new PdfService.RxLine("Paracetamol 500mg", "1 tab", "TDS", "5 days", "After food")));

        assertThat(out).isNotEmpty();
        assertThat(new String(out, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void buildsInvoicePdf() {
        byte[] out = pdf.invoicePdf(
            "Dr. Ramesh Clinic", "INV-2026-00001", "Sita Devi", "2026-06-25",
            List.of(new PdfService.BillLine("Consultation fee", "999312", new BigDecimal("300"))),
            new BigDecimal("300"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("300"), "UPI");

        assertThat(out).isNotEmpty();
        assertThat(new String(out, 0, 4)).isEqualTo("%PDF");
    }
}
