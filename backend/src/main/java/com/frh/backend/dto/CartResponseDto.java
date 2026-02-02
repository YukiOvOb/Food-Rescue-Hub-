package com.frh.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDto {
    private Long cartId;
    private Long supplierId;
    private List<CartItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal total;

    @Data
    public static class CartItemDto {
        private Long listingId;
        private String title;
        private String imageUrl;
        private BigDecimal unitPrice;
        private int qty;
        private BigDecimal lineTotal;
    }
}