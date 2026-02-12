package com.frh.backend.service;

import com.frh.backend.dto.Co2CategoryBreakdownDto;
import com.frh.backend.dto.Co2SummaryDto;
import com.frh.backend.model.SupplierProfile;
import com.frh.backend.repository.OrderRepository;
import com.frh.backend.repository.SupplierRepository;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Co2AnalyticsService {

  private final OrderRepository orderRepository;
  private final SupplierRepository supplierRepository;

  public Co2SummaryDto getCo2Summary(Long supplierId, int days) {
    int effectiveDays = Math.max(1, days);
    LocalDateTime to = LocalDateTime.now();
    LocalDateTime from = to.minusDays(effectiveDays);

    List<Co2CategoryBreakdownDto> breakdown =
        orderRepository.findCo2BreakdownBySupplierAndStatusesSince(
            supplierId, Arrays.asList("COMPLETED", "COLLECTED"), from);
    if (breakdown == null) {
      breakdown = List.of();
    }

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
    Document document = new Document(PageSize.A4, 36, 36, 72, 56);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String reportId = "FRH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String generatedAt =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

    PdfWriter writer = PdfWriter.getInstance(document, baos);
    writer.setPageEvent(new FooterPageEvent(reportId, generatedAt));

    document.open();

    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17);
    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
    Font smallMutedFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

    String supplierName = resolveSupplierName(supplierId);
    DateTimeFormatter periodFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    PdfPTable banner = new PdfPTable(1);
    banner.setWidthPercentage(100);
    banner.getDefaultCell().setBorder(Rectangle.NO_BORDER);

    PdfPCell logoCell = new PdfPCell();
    logoCell.setBorder(Rectangle.NO_BORDER);
    logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    Image logo = loadLogoIfPresent();
    if (logo != null) {
      logo.scaleToFit(210f, 72f);
      logoCell.addElement(logo);
    }
    banner.addCell(logoCell);
    document.add(banner);

    Paragraph certificateTitle = new Paragraph("CERTIFICATE OF IMPACT", titleFont);
    certificateTitle.setAlignment(Element.ALIGN_CENTER);
    certificateTitle.setSpacingBefore(4);
    document.add(certificateTitle);
    Paragraph certificateSubtitle =
        new Paragraph("Food Rescue Hub CO2 Savings Verification", headerFont);
    certificateSubtitle.setAlignment(Element.ALIGN_CENTER);
    document.add(certificateSubtitle);
    document.add(new Paragraph(" ", bodyFont));

    PdfPTable meta = new PdfPTable(2);
    meta.setWidthPercentage(100);
    meta.setWidths(new float[] {1.2f, 3.8f});
    meta.setSpacingBefore(4);
    meta.setSpacingAfter(10);
    meta.addCell(metaKeyCell("Awarded To"));
    meta.addCell(metaValueCell(supplierName));
    meta.addCell(metaKeyCell("Supplier ID"));
    meta.addCell(metaValueCell(String.valueOf(supplierId)));
    meta.addCell(metaKeyCell("Report ID"));
    meta.addCell(metaValueCell(reportId));
    meta.addCell(metaKeyCell("Period"));
    meta.addCell(
        metaValueCell(
            summary.getFrom().format(periodFmt) + " to " + summary.getTo().format(periodFmt)));
    meta.addCell(metaKeyCell("Issued On"));
    meta.addCell(metaValueCell(generatedAt));
    document.add(meta);

    document.add(new Paragraph("Summary", headerFont));

    PdfPTable summaryTable = new PdfPTable(2);
    summaryTable.setWidthPercentage(100);
    summaryTable.setWidths(new float[] {2.8f, 1.2f});
    summaryTable.setSpacingBefore(6);
    summaryTable.setSpacingAfter(12);
    summaryTable.addCell(metaKeyCell("Total CO2 Saved (kg)"));
    summaryTable.addCell(metaValueCell(format(summary.getTotalCo2Kg())));
    summaryTable.addCell(metaKeyCell("Total Food Weight Rescued (kg)"));
    summaryTable.addCell(metaValueCell(format(summary.getTotalWeightKg())));
    document.add(summaryTable);

    document.add(new Paragraph("Breakdown by Category", headerFont));
    PdfPTable table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.setSpacingBefore(8);
    table.setSpacingAfter(8);
    table.setWidths(new float[] {1.2f, 3.8f, 2.5f, 2.5f});

    table.addCell(headerCell("Category ID"));
    table.addCell(headerCell("Category Name"));
    table.addCell(headerCell("Total Weight (kg)"));
    table.addCell(headerCell("Total CO2 Saved (kg)"));

    boolean shaded = false;
    for (Co2CategoryBreakdownDto item : summary.getCategories()) {
      table.addCell(bodyCell(String.valueOf(item.getCategoryId()), Element.ALIGN_CENTER, shaded));
      table.addCell(bodyCell(item.getCategoryName(), Element.ALIGN_LEFT, shaded));
      table.addCell(bodyCell(format(item.getTotalWeightKg()), Element.ALIGN_RIGHT, shaded));
      table.addCell(bodyCell(format(item.getTotalCo2Kg()), Element.ALIGN_RIGHT, shaded));
      shaded = !shaded;
    }

    if (summary.getCategories().isEmpty()) {
      PdfPCell emptyCell = new PdfPCell(new Phrase("No category breakdown data available."));
      emptyCell.setPadding(8);
      emptyCell.setColspan(4);
      emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      table.addCell(emptyCell);
    }
    document.add(table);
    document.add(
        new Paragraph(
            "Methodology: Figures are based on completed orders and category-level emission"
                + " factors configured in the platform.",
            smallMutedFont));
    document.add(
        new Paragraph(
            "Generated by Food Rescue Hub analytics. For operational insight only.",
            smallMutedFont));

    document.close();
    return baos.toByteArray();
  }

  private PdfPCell headerCell(String text) {
    Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setPadding(6);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setBackgroundColor(new java.awt.Color(228, 240, 216));
    return cell;
  }

  private PdfPCell bodyCell(String text, int align, boolean shaded) {
    Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setPadding(6);
    cell.setHorizontalAlignment(align);
    if (shaded) {
      cell.setBackgroundColor(new java.awt.Color(248, 251, 245));
    }
    return cell;
  }

  private PdfPCell metaKeyCell(String text) {
    Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setPadding(6);
    cell.setBackgroundColor(new java.awt.Color(245, 245, 245));
    return cell;
  }

  private PdfPCell metaValueCell(String text) {
    Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setPadding(6);
    return cell;
  }

  private String resolveSupplierName(Long supplierId) {
    Optional<SupplierProfile> supplierOpt = Optional.empty();
    try {
      Optional<SupplierProfile> result = supplierRepository.findById(supplierId);
      if (result != null) {
        supplierOpt = result;
      }
    } catch (Exception ignored) {
    }

    if (supplierOpt.isEmpty()) {
      return "Unknown Supplier";
    }

    SupplierProfile supplier = supplierOpt.get();
    if (supplier.getBusinessName() != null && !supplier.getBusinessName().isBlank()) {
      return supplier.getBusinessName();
    }
    if (supplier.getDisplayName() != null && !supplier.getDisplayName().isBlank()) {
      return supplier.getDisplayName();
    }
    if (supplier.getEmail() != null && !supplier.getEmail().isBlank()) {
      return supplier.getEmail();
    }
    return "Unknown Supplier";
  }

  private Image loadLogoIfPresent() {
    String[] candidates = {
      "branding/resqfood-logo.png",
      "branding/app-logo.png",
      "static/branding/resqfood-logo.png"
    };

    for (String path : candidates) {
      try {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
          continue;
        }
        try (InputStream inputStream = resource.getInputStream()) {
          return Image.getInstance(inputStream.readAllBytes());
        }
      } catch (IOException | DocumentException ignored) {
      }
    }

    return null;
  }

  private static class FooterPageEvent extends PdfPageEventHelper {
    private final String reportId;
    private final String generatedAt;

    private FooterPageEvent(String reportId, String generatedAt) {
      this.reportId = reportId;
      this.generatedAt = generatedAt;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      Rectangle page = document.getPageSize();
      PdfContentByte cb = writer.getDirectContent();
      Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);

      cb.setLineWidth(1f);
      cb.rectangle(
          document.left() - 12,
          document.bottom() - 26,
          document.right() - document.left() + 24,
          document.top() - document.bottom() + 36);
      cb.stroke();

      cb.setLineWidth(0.5f);
      cb.moveTo(document.left(), document.bottom() - 6);
      cb.lineTo(document.right(), document.bottom() - 6);
      cb.stroke();

      ColumnText.showTextAligned(
          cb,
          Element.ALIGN_LEFT,
          new Phrase("Food Rescue Hub  |  " + reportId, font),
          document.left(),
          document.bottom() - 18,
          0);

      ColumnText.showTextAligned(
          cb,
          Element.ALIGN_CENTER,
          new Phrase("Generated: " + generatedAt, font),
          (page.getLeft() + page.getRight()) / 2,
          document.bottom() - 18,
          0);

      ColumnText.showTextAligned(
          cb,
          Element.ALIGN_RIGHT,
          new Phrase("Page " + writer.getPageNumber(), font),
          document.right(),
          document.bottom() - 18,
          0);
    }
  }

  private String format(BigDecimal value) {
    if (value == null) {
      return "0.000";
    }
    return value.setScale(3, RoundingMode.HALF_UP).toPlainString();
  }
}
