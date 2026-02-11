package com.frh.backend.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ListingCategoryWeightDTO {
  private Long categoryId;
  private String categoryName;
  private BigDecimal weightKg;
}
