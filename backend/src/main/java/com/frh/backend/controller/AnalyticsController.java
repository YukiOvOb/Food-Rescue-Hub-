package com.frh.backend.controller;

import com.frh.backend.dto.Co2SummaryDto;
import com.frh.backend.dto.TopSellingItemDto;
import com.frh.backend.service.Co2AnalyticsService;
import com.frh.backend.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalyticsController {

  private final OrderService orderService;
  private final Co2AnalyticsService co2AnalyticsService;

  /**
   * Top selling items for a supplier (by quantity) GET
   * /api/analytics/supplier/{supplierId}/top-products?limit=3
   */
  @GetMapping("/supplier/{supplierId}/top-products")
  public ResponseEntity<?> getTopSellingProducts(
      @PathVariable Long supplierId, @RequestParam(defaultValue = "3") int limit) {
    try {
      List<TopSellingItemDto> items =
          orderService.getTopSellingItems(supplierId, "COMPLETED", limit);
      return ResponseEntity.ok(items);
    } catch (Exception e) {
      log.error("Error retrieving top selling products", e);
      if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to retrieve top selling products");
    }
  }

  /**
   * CO2 savings summary for a supplier (default last 30 days) GET
   * /api/analytics/supplier/{supplierId}/co2?days=30
   */
  @GetMapping("/supplier/{supplierId}/co2")
  public ResponseEntity<?> getCo2Summary(
      @PathVariable Long supplierId, @RequestParam(defaultValue = "30") int days) {
    try {
      Co2SummaryDto summary = co2AnalyticsService.getCo2Summary(supplierId, days);
      return ResponseEntity.ok(summary);
    } catch (Exception e) {
      log.error("Error retrieving CO2 summary", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to retrieve CO2 summary");
    }
  }

  /**
   * CO2 savings PDF report for a supplier (default last 30 days) GET
   * /api/analytics/supplier/{supplierId}/co2/report?days=30
   */
  @GetMapping("/supplier/{supplierId}/co2/report")
  public ResponseEntity<?> getCo2ReportPdf(
      @PathVariable Long supplierId, @RequestParam(defaultValue = "30") int days) {
    try {
      Co2SummaryDto summary = co2AnalyticsService.getCo2Summary(supplierId, days);
      byte[] pdf = co2AnalyticsService.generateCo2ReportPdf(summary, supplierId);
      String filename =
          "co2-report-supplier-" + supplierId + "-last-" + Math.max(1, days) + "-days.pdf";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .contentType(MediaType.APPLICATION_PDF)
          .body(pdf);
    } catch (Exception e) {
      log.error("Error generating CO2 report PDF", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to generate CO2 report PDF");
    }
  }
}
