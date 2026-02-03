package com.frh.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class CreateOrderResponseDto {
    private Long orderId;
    private BigDecimal totalAmount;
    private String pickupToken;
}