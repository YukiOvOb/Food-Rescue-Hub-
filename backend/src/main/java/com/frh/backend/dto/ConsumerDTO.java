package com.frh.backend.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for Consumer Profile information */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerDto {

  private Long consumerId;
  private String email;
  private String phone;
  private String displayName;
  private String status;
  private String role;
  private BigDecimal defaultLat;
  private BigDecimal defaultLng;
  private Map<String, Object> preferences;
}
