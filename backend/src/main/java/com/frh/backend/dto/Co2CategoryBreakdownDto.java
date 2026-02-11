package com.frh.backend.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Co2CategoryBreakdownDto {
  private Long categoryId;
  private String categoryName;
  private BigDecimal totalWeightKg;
  private BigDecimal totalCo2Kg;
}
