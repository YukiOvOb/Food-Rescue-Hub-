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

  public Co2CategoryBreakdownDto(
      Number categoryId, String categoryName, Number totalWeightKg, Number totalCo2Kg) {
    this.categoryId = categoryId == null ? null : categoryId.longValue();
    this.categoryName = categoryName;
    this.totalWeightKg = toBigDecimal(totalWeightKg);
    this.totalCo2Kg = toBigDecimal(totalCo2Kg);
  }

  private static BigDecimal toBigDecimal(Number value) {
    return value == null ? null : new BigDecimal(value.toString());
  }
}
