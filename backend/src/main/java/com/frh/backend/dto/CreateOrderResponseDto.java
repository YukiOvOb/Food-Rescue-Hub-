package com.frh.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOrderResponseDto {
    private Long orderId;
    private String status;
    private BigDecimal totalAmount;
    private String pickupToken;
    private LocalDateTime pickupSlotStart;
    private LocalDateTime pickupSlotEnd;
    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        private Long listingId;
        private String title;
        private BigDecimal unitPrice;
        private int qty;
        private BigDecimal lineTotal;
    }
}