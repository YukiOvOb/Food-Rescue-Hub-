package com.frh.backend.controller;

import com.frh.backend.model.SupplierProfile;
import com.frh.backend.service.SupplierProfileService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierProfileController {

  @Autowired private SupplierProfileService supplierProfileService;

  @GetMapping("/{supplierId}")
  public ResponseEntity<?> getSupplierProfile(@PathVariable Long supplierId) {
    try {
      SupplierProfile supplier =
          supplierProfileService
              .getSupplierById(supplierId)
              .orElseThrow(() -> new RuntimeException("Supplier not found"));

      Map<String, Object> response = new HashMap<>();
      response.put("supplierId", supplier.getSupplierId());
      response.put("email", supplier.getEmail());
      response.put("displayName", supplier.getDisplayName());
      response.put("phoneNumber", supplier.getPhone());
      response.put("businessName", supplier.getBusinessName());
      response.put("businessType", supplier.getBusinessType());
      response.put("role", supplier.getRole());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
  }

  @PutMapping("/{supplierId}")
  public ResponseEntity<?> updateSupplierProfile(
      @PathVariable Long supplierId, @RequestBody Map<String, String> updates) {
    try {
      String displayName = updates.get("displayName");
      String phoneNumber = updates.get("phoneNumber");

      SupplierProfile updatedSupplier =
          supplierProfileService.updateSupplierProfile(supplierId, displayName, phoneNumber);

      Map<String, Object> response = new HashMap<>();
      response.put("message", "Profile updated successfully");
      response.put("supplierId", updatedSupplier.getSupplierId());
      response.put("displayName", updatedSupplier.getDisplayName());
      response.put("phoneNumber", updatedSupplier.getPhone());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  }

  @PutMapping("/{supplierId}/password")
  public ResponseEntity<?> updatePassword(
      @PathVariable Long supplierId, @RequestBody Map<String, String> passwordData) {
    try {
      String currentPassword = passwordData.get("currentPassword");
      String newPassword = passwordData.get("newPassword");

      // TODO: Verify current password before updating
      // For now, just update the password
      supplierProfileService.updatePassword(supplierId, newPassword);

      Map<String, String> response = new HashMap<>();
      response.put("message", "Password updated successfully");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  }
}
