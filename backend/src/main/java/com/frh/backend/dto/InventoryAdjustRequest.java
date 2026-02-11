package com.frh.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* Body sent by the supplier when manually adjusting stock.
 * {@code delta} is a signed integer:
 * positive to restock (add units)
 * negative to remove (e.g. spoiled items)
 * The service layer validates that the resulting qty never drops below the zero.*/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustRequest {

  @NotNull(message = "delta is required (positive to add, negative to remove)")
  private Integer delta;
}
