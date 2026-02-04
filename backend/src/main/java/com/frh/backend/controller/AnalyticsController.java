package com.frh.backend.controller;

import com.frh.backend.dto.TopSellingItemDto;
import com.frh.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final OrderService orderService;

    /**
     * Top selling items for a supplier (by quantity)
     * GET /api/analytics/supplier/{supplierId}/top-products?limit=3
     */
    @GetMapping("/supplier/{supplierId}/top-products")
    public ResponseEntity<?> getTopSellingProducts(
            @PathVariable Long supplierId,
            @RequestParam(defaultValue = "3") int limit) {
        try {
            List<TopSellingItemDto> items = orderService.getTopSellingItems(supplierId, "COMPLETED", limit);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error retrieving top selling products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve top selling products");
        }
    }
}
