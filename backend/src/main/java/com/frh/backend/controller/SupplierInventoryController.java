package com.frh.backend.controller;

import com.frh.backend.Model.Inventory;
import com.frh.backend.dto.InventoryAdjustRequest;
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
  public ResponseEntity<Inventory> getStock(@PathVariable Long listingId) {
    Inventory inv = inventoryService.getInventory(listingId);
    return ResponseEntity.ok(inv);
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
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException ex) {
      // "Cannot adjust below zero" - 400
      return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
    }
  }
}

/*
 * just for my reference - roughly
 * the given Routes
 * GET /api/supplier/inventory/{listingId} – current stock
 * PUT /api/supplier/inventory/{listingId}/adjust – manual restock / remove
 */