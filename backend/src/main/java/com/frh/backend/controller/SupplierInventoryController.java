package com.frh.backend.controller;

import com.frh.backend.Model.Inventory;
import com.frh.backend.dto.InventoryAdjustRequest;
import com.frh.backend.dto.InventoryResponseDto;
import com.frh.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/* REST endpoints for supplier inventory management*/

@RestController
@RequestMapping("/api/supplier/inventory")
@CrossOrigin(origins = "*")
public class SupplierInventoryController {

  @Autowired
  private InventoryService inventoryService;

  // Read current stock
  @GetMapping("/{listingId}")
  public ResponseEntity<?> getStock(@PathVariable Long listingId) {
    try {
      Inventory inv = inventoryService.getInventory(listingId);
      return ResponseEntity.ok(toInventoryResponse(inv));
    } catch (RuntimeException ex) {
      String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
      if (message.contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.status(500).body(java.util.Map.of("error", ex.getMessage()));
    }
  }

  /**
   * PUT /api/supplier/inventory/{listingId}/adjust
   * Body: { "delta": 5 } to adds 5 units
   * { "delta": -2 } to removes 2 units (e.g. spoiled)
   * 
   * Returns 400 when the resulting qty would be negative.
   */
  @PutMapping("/{listingId}/adjust")
  public ResponseEntity<?> adjustInventory(
      @PathVariable Long listingId,
      @Valid @RequestBody InventoryAdjustRequest body) {

    try {
      Inventory updated = inventoryService.adjustInventory(listingId, body.getDelta());
      return ResponseEntity.ok(toInventoryResponse(updated));
    } catch (IllegalArgumentException ex) {
      // "Cannot adjust below zero" - 400
      return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
    } catch (Exception ex) {
      return ResponseEntity.status(500).body(java.util.Map.of("error", ex.getMessage()));
    }
  }

  private InventoryResponseDto toInventoryResponse(Inventory inventory) {
    InventoryResponseDto dto = new InventoryResponseDto();
    if (inventory == null) {
      return dto;
    }

    dto.setInventoryId(inventory.getInventoryId());
    dto.setQtyAvailable(inventory.getQtyAvailable());
    dto.setQtyReserved(inventory.getQtyReserved());
    dto.setLastUpdated(inventory.getLastUpdated());
    if (inventory.getListing() != null) {
      dto.setListingId(inventory.getListing().getListingId());
    }

    return dto;
  }
}

/*
 * just for my reference - roughly
 * the given Routes
 * GET /api/supplier/inventory/{listingId} – current stock
 * PUT /api/supplier/inventory/{listingId}/adjust – manual restock / remove
 */
