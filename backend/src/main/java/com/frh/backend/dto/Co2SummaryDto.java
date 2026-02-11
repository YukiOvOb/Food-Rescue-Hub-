package com.frh.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class Co2SummaryDto {
  private BigDecimal totalCo2Kg;
  private BigDecimal totalWeightKg;
  private int days;
  private LocalDateTime from;
  private LocalDateTime to;
  private List<Co2CategoryBreakdownDto> categories;
}
