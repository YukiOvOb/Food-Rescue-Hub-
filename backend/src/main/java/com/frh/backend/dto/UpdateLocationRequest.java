package com.frh.backend.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateLocationRequest {
  private BigDecimal latitude;
  private BigDecimal longitude;
}
