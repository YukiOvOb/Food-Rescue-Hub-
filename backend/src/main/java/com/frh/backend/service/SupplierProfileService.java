package com.frh.backend.service;

import com.frh.backend.model.SupplierProfile;
import com.frh.backend.repository.SupplierProfileRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierProfileService {

  @Autowired private SupplierProfileRepository supplierProfileRepository;

  public Optional<SupplierProfile> getSupplierById(Long supplierId) {
    return supplierProfileRepository.findById(supplierId);
  }

  @Transactional
  public SupplierProfile updateSupplierProfile(
      Long supplierId, String displayName, String phoneNumber) {
    SupplierProfile supplier =
        supplierProfileRepository
            .findById(supplierId)
            .orElseThrow(
                () -> new RuntimeException("Supplier not found with id: " + supplierId));

    // Update only allowed fields
    if (displayName != null && !displayName.trim().isEmpty()) {
      supplier.setDisplayName(displayName.trim());
    }

    if (phoneNumber != null) {
      // Allow empty string to clear phone number
      supplier.setPhone(phoneNumber.trim().isEmpty() ? null : phoneNumber.trim());
    }

    return supplierProfileRepository.save(supplier);
  }

  @Transactional
  public void updatePassword(Long supplierId, String newPassword) {
    SupplierProfile supplier =
        supplierProfileRepository
            .findById(supplierId)
            .orElseThrow(
                () -> new RuntimeException("Supplier not found with id: " + supplierId));

    supplier.setPassword(newPassword);
    supplierProfileRepository.save(supplier);
  }
}
