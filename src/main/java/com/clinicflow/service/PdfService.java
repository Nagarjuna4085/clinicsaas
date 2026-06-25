package com.clinicflow.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * Generates PDF documents (prescriptions, invoices) with OpenPDF.
 * Returns raw bytes so callers can stream or upload them.
 */
@Service
public class PdfService {

    public record RxLine(String medicine, String dosage, String frequency, String duration, String instructions) {}
    public record BillLine(String description, String hsnSac, BigDecimal amount) {}

    private static final Color BRAND = new Color(37, 99, 235);
    private static final Color MUTED = new Color(100, 116, 139);

    private static final Font H1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND);
    private static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font LABEL = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED);
    private static final Font BODY = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font BODY_B = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font TH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

    public byte[] prescriptionPdf(String clinicName, String doctorName, String patientName,
                                  String date, String chiefComplaint, String diagnosis,
                                  String advice, String followupDate, List<RxLine> meds) {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph(clinicName == null ? "Clinic" : clinicName, H1));
            doc.add(new Paragraph("Prescription", H2));
            doc.add(rule());

            PdfPTable meta = new PdfPTable(new float[]{1, 1});
            meta.setWidthPercentage(100);
            meta.setSpacingBefore(8);
            meta.addCell(kv("Patient", patientName));
            meta.addCell(kv("Date", date));
            meta.addCell(kv("Doctor", doctorName));
            meta.addCell(kv("Follow-up", followupDate == null || followupDate.isBlank() ? "—" : followupDate));
            doc.add(meta);

            section(doc, "Chief complaint", chiefComplaint);
            section(doc, "Examination", null); // placeholder spacing kept minimal
            section(doc, "Diagnosis", diagnosis);

            doc.add(spacer());
            doc.add(new Paragraph("Rx", H2));
            PdfPTable t = new PdfPTable(new float[]{3, 1.5f, 1.5f, 1.5f, 2.5f});
            t.setWidthPercentage(100);
            t.setSpacingBefore(4);
            for (String h : new String[]{"Medicine", "Dosage", "Frequency", "Duration", "Instructions"}) {
                t.addCell(th(h));
            }
            if (meds != null) {
                for (RxLine m : meds) {
                    t.addCell(td(m.medicine()));
                    t.addCell(td(m.dosage()));
                    t.addCell(td(m.frequency()));
                    t.addCell(td(m.duration()));
                    t.addCell(td(m.instructions()));
                }
            }
            doc.add(t);

            section(doc, "Advice", advice);

            doc.add(spacer());
            Paragraph sign = new Paragraph("\n\n_________________________\n" + (doctorName == null ? "Doctor" : doctorName), BODY);
            sign.setAlignment(Element.ALIGN_RIGHT);
            doc.add(sign);

            doc.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to build prescription PDF", e);
        }
    }

    public byte[] invoicePdf(String clinicName, String invoiceNumber, String patientName, String billedAt,
                             List<BillLine> items, BigDecimal subtotal, BigDecimal cgst, BigDecimal sgst,
                             BigDecimal total, String paymentMode) {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph(clinicName == null ? "Clinic" : clinicName, H1));
            doc.add(new Paragraph("Tax Invoice", H2));
            doc.add(rule());

            PdfPTable meta = new PdfPTable(new float[]{1, 1});
            meta.setWidthPercentage(100);
            meta.setSpacingBefore(8);
            meta.addCell(kv("Invoice #", invoiceNumber));
            meta.addCell(kv("Date", billedAt));
            meta.addCell(kv("Patient", patientName));
            meta.addCell(kv("Payment", paymentMode));
            doc.add(meta);

            doc.add(spacer());
            PdfPTable t = new PdfPTable(new float[]{4, 2, 2});
            t.setWidthPercentage(100);
            t.addCell(th("Description"));
            t.addCell(th("HSN/SAC"));
            t.addCell(thRight("Amount"));
            if (items != null) {
                for (BillLine it : items) {
                    t.addCell(td(it.description()));
                    t.addCell(td(it.hsnSac()));
                    t.addCell(tdRight(money(it.amount())));
                }
            }
            doc.add(t);

            PdfPTable totals = new PdfPTable(new float[]{3, 1});
            totals.setWidthPercentage(45);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.setSpacingBefore(8);
            totalRow(totals, "Subtotal", money(subtotal), false);
            totalRow(totals, "CGST", money(cgst), false);
            totalRow(totals, "SGST", money(sgst), false);
            totalRow(totals, "Total", money(total), true);
            doc.add(totals);

            doc.add(spacer());
            Paragraph note = new Paragraph("This is a computer-generated invoice.", LABEL);
            doc.add(note);

            doc.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to build invoice PDF", e);
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static String money(BigDecimal v) {
        return "INR " + (v == null ? "0" : v.toPlainString());
    }

    private static void section(Document doc, String label, String value) throws DocumentException {
        if (value == null || value.isBlank()) return;
        doc.add(spacer());
        Paragraph l = new Paragraph(label.toUpperCase(), LABEL);
        l.setSpacingAfter(2);
        doc.add(l);
        doc.add(new Paragraph(value, BODY));
    }

    private static Paragraph spacer() {
        Paragraph p = new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4));
        return p;
    }

    private static Paragraph rule() {
        Paragraph p = new Paragraph();
        p.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(0.5f, 100, MUTED, Element.ALIGN_CENTER, -2)));
        return p;
    }

    private static PdfPCell kv(String k, String v) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPaddingBottom(6);
        c.addElement(new Paragraph(k.toUpperCase(), LABEL));
        c.addElement(new Paragraph(v == null || v.isBlank() ? "—" : v, BODY_B));
        return c;
    }

    private static PdfPCell th(String s) {
        PdfPCell c = new PdfPCell(new Paragraph(s, TH));
        c.setBackgroundColor(BRAND);
        c.setPadding(6);
        c.setBorderColor(Color.WHITE);
        return c;
    }

    private static PdfPCell thRight(String s) {
        PdfPCell c = th(s);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    private static PdfPCell td(String s) {
        PdfPCell c = new PdfPCell(new Paragraph(s == null ? "" : s, BODY));
        c.setPadding(5);
        c.setBorderColor(new Color(226, 232, 240));
        return c;
    }

    private static PdfPCell tdRight(String s) {
        PdfPCell c = td(s);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    private static void totalRow(PdfPTable t, String label, String value, boolean bold) {
        Font f = bold ? BODY_B : BODY;
        PdfPCell l = new PdfPCell(new Paragraph(label, f));
        l.setBorder(Rectangle.NO_BORDER);
        PdfPCell v = new PdfPCell(new Paragraph(value, f));
        v.setBorder(Rectangle.NO_BORDER);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(l);
        t.addCell(v);
    }
}
