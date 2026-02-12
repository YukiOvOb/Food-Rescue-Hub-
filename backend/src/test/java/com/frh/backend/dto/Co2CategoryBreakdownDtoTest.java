package com.frh.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Co2CategoryBreakdownDtoTest {

  @Test
  void numberConstructor_convertsValues() {
    Co2CategoryBreakdownDto dto =
        new Co2CategoryBreakdownDto((Number) 7, "Fruit", (Number) 1.25, (Number) 3);

    assertEquals(7L, dto.getCategoryId());
    assertEquals("Fruit", dto.getCategoryName());
    assertEquals(new BigDecimal("1.25"), dto.getTotalWeightKg());
    assertEquals(new BigDecimal("3"), dto.getTotalCo2Kg());
  }

  @Test
  void numberConstructor_handlesNullNumbers() {
    Co2CategoryBreakdownDto dto = new Co2CategoryBreakdownDto((Number) null, "Veg", null, null);

    assertNull(dto.getCategoryId());
    assertEquals("Veg", dto.getCategoryName());
    assertNull(dto.getTotalWeightKg());
    assertNull(dto.getTotalCo2Kg());
  }
}
