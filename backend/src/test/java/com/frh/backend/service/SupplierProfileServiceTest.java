package com.frh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frh.backend.model.SupplierProfile;
import com.frh.backend.repository.SupplierProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplierProfileServiceTest {

  @Mock private SupplierProfileRepository supplierProfileRepository;

  @InjectMocks private SupplierProfileService supplierProfileService;

  @Test
  void getSupplierById_returnsRepositoryResult() {
    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(1L);
    when(supplierProfileRepository.findById(1L)).thenReturn(Optional.of(supplier));

    Optional<SupplierProfile> result = supplierProfileService.getSupplierById(1L);

    assertEquals(1L, result.orElseThrow().getSupplierId());
  }

  @Test
  void updateSupplierProfile_updatesTrimmedFields() {
    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(2L);
    supplier.setDisplayName("Before");
    supplier.setPhone("123");

    when(supplierProfileRepository.findById(2L)).thenReturn(Optional.of(supplier));
    when(supplierProfileRepository.save(any(SupplierProfile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SupplierProfile result =
        supplierProfileService.updateSupplierProfile(2L, "  New Name  ", "  90000001  ");

    assertEquals("New Name", result.getDisplayName());
    assertEquals("90000001", result.getPhone());
  }

  @Test
  void updateSupplierProfile_ignoresBlankDisplayNameAndClearsPhoneWhenBlank() {
    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(3L);
    supplier.setDisplayName("Kept Name");
    supplier.setPhone("88888888");

    when(supplierProfileRepository.findById(3L)).thenReturn(Optional.of(supplier));
    when(supplierProfileRepository.save(any(SupplierProfile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SupplierProfile result = supplierProfileService.updateSupplierProfile(3L, "   ", "   ");

    assertEquals("Kept Name", result.getDisplayName());
    assertNull(result.getPhone());
  }

  @Test
  void updateSupplierProfile_allowsNullPhoneWithoutChangingExistingPhone() {
    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(4L);
    supplier.setDisplayName("Name");
    supplier.setPhone("77777777");

    when(supplierProfileRepository.findById(4L)).thenReturn(Optional.of(supplier));
    when(supplierProfileRepository.save(any(SupplierProfile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SupplierProfile result = supplierProfileService.updateSupplierProfile(4L, null, null);

    assertEquals("Name", result.getDisplayName());
    assertEquals("77777777", result.getPhone());
  }

  @Test
  void updateSupplierProfile_missingSupplier_throws() {
    when(supplierProfileRepository.findById(999L)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> supplierProfileService.updateSupplierProfile(999L, "A", "1"));

    assertEquals("Supplier not found with id: 999", ex.getMessage());
  }

  @Test
  void updatePassword_updatesAndSaves() {
    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(5L);
    supplier.setPassword("old");

    when(supplierProfileRepository.findById(5L)).thenReturn(Optional.of(supplier));

    supplierProfileService.updatePassword(5L, "new-pass");

    assertEquals("new-pass", supplier.getPassword());
    verify(supplierProfileRepository).save(supplier);
  }

  @Test
  void updatePassword_missingSupplier_throws() {
    when(supplierProfileRepository.findById(404L)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class, () -> supplierProfileService.updatePassword(404L, "pw"));

    assertEquals("Supplier not found with id: 404", ex.getMessage());
  }
}
