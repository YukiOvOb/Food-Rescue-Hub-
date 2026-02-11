package com.frh.backend.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateOrderResponseDto {
  private Long orderId;
  private BigDecimal totalAmount;
  private String pickupToken;
}
