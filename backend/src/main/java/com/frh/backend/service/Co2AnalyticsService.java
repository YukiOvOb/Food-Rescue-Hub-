package com.frh.backend.service;

import com.frh.backend.dto.Co2CategoryBreakdownDto;
import com.frh.backend.dto.Co2SummaryDto;
import com.frh.backend.repository.OrderRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Co2AnalyticsService {

    private final OrderRepository orderRepository;

    public Co2SummaryDto getCo2Summary(Long supplierId, int days) {
        int effectiveDays = Math.max(1, days);
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(effectiveDays);

        List<Co2CategoryBreakdownDto> breakdown = orderRepository
            .findCo2BreakdownBySupplierAndStatusSince(supplierId, "COMPLETED", from);

        BigDecimal totalCo2 = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (Co2CategoryBreakdownDto item : breakdown) {
            if (item.getTotalCo2Kg() != null) {
                totalCo2 = totalCo2.add(item.getTotalCo2Kg());
            }
            if (item.getTotalWeightKg() != null) {
                totalWeight = totalWeight.add(item.getTotalWeightKg());
            }
        }

        Co2SummaryDto dto = new Co2SummaryDto();
        dto.setDays(effectiveDays);
        dto.setFrom(from);
        dto.setTo(to);
        dto.setCategories(breakdown);
        dto.setTotalCo2Kg(totalCo2);
        dto.setTotalWeightKg(totalWeight);
        return dto;
    }

    public byte[] generateCo2ReportPdf(Co2SummaryDto summary, Long supplierId) {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph("CO2 Savings Report", titleFont));
        document.add(new Paragraph("Supplier ID: " + supplierId, bodyFont));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        document.add(new Paragraph("Period: " + summary.getFrom().format(fmt) + " to " + summary.getTo().format(fmt), bodyFont));
        document.add(new Paragraph(" ", bodyFont));

        document.add(new Paragraph("Summary", headerFont));
        document.add(new Paragraph("Total CO2 saved (kg): " + format(summary.getTotalCo2Kg()), bodyFont));
        document.add(new Paragraph("Total weight (kg): " + format(summary.getTotalWeightKg()), bodyFont));
        document.add(new Paragraph(" ", bodyFont));

        document.add(new Paragraph("Breakdown by Category", headerFont));
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(8);
        table.setWidths(new float[] { 1.2f, 3.8f, 2.5f, 2.5f });

        table.addCell(headerCell("ID"));
        table.addCell(headerCell("Category"));
        table.addCell(headerCell("Total Weight (kg)"));
        table.addCell(headerCell("Total CO2 (kg)"));

        for (Co2CategoryBreakdownDto item : summary.getCategories()) {
            table.addCell(bodyCell(String.valueOf(item.getCategoryId())));
            table.addCell(bodyCell(item.getCategoryName()));
            table.addCell(bodyCell(format(item.getTotalWeightKg())));
            table.addCell(bodyCell(format(item.getTotalCo2Kg())));
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    private PdfPCell headerCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell bodyCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        return cell;
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "0.000";
        }
        return value.setScale(3, RoundingMode.HALF_UP).toPlainString();
    }
}
