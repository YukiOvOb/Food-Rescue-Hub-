package com.frh.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.frh.backend.Model.Store;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.StoreRequest;
import com.frh.backend.dto.StoreResponse;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.repository.SupplierProfileRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class) // Enables Mockito
@ActiveProfiles("test")
public class StoreServiceTest {

  @Mock private StoreRepository storeRepository;

  @Mock private SupplierProfileRepository supplierProfileRepository;

  @InjectMocks private StoreService storeService;

  @Test
  void createStore_ShouldReturnStoreResponse_WhenValidRequest() {
    // 1. Arrange (Prepare fake data)
    Long supplierId = 1L;

    // The DTO we send in
    StoreRequest dto = new StoreRequest();
    dto.setSupplierId(supplierId);
    dto.setStoreName("Test Bakery");
    dto.setLat(new BigDecimal("1.29"));
    dto.setLng(new BigDecimal("103.85"));
    dto.setAddressLine("123 Street");
    dto.setPostalCode("123456");

    // The Dummy Supplier found in DB
    SupplierProfile mockSupplier = new SupplierProfile();
    mockSupplier.setSupplierId(supplierId);

    // The Store that gets saved (Simulated Entity)
    Store savedStore = new Store();
    savedStore.setStoreId(10L);
    savedStore.setStoreName("Test Bakery");
    savedStore.setAddressLine("123 Street");
    savedStore.setPostalCode("123456");
    savedStore.setLat(new BigDecimal("1.29"));
    savedStore.setLng(new BigDecimal("103.85"));
    savedStore.setSupplierProfile(mockSupplier);
    savedStore.setActive(true);

    // Tell Mockito what to do when repositories are called
    when(supplierProfileRepository.findById(supplierId)).thenReturn(Optional.of(mockSupplier));
    when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

    // 2. Act (Call the actual method)
    // The result type is now StoreResponse
    StoreResponse result = storeService.createStore(dto);

    // 3. Assert (Verify the results)
    assertNotNull(result);
    assertEquals(10L, result.getStoreId()); // Verify mapping from Entity ID to DTO ID
    assertEquals("Test Bakery", result.getStoreName());
    assertEquals(supplierId, result.getSupplierId()); // Verify supplier ID was extracted correctly
    assertTrue(result.isActive());

    // Verify dependencies were actually called
    verify(supplierProfileRepository, times(1)).findById(supplierId);
    verify(storeRepository, times(1)).save(any(Store.class));
  }

  @Test
  void getStoreResponseById_ShouldReturnStoreResponse_WhenStoreExists() {
    // 1. Arrange
    Long storeId = 10L;
    Store mockStore = new Store();
    mockStore.setStoreId(storeId);
    mockStore.setStoreName("Haziq's Bakery");
    mockStore.setPickupInstructions("Red Door");

    // Set up the mock supplier to avoid NullPointerException in mapToResponse
    SupplierProfile mockSupplier = new SupplierProfile();
    mockSupplier.setSupplierId(5L);
    mockStore.setSupplierProfile(mockSupplier);

    // Mock the repository behavior
    when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

    // 2. Act
    StoreResponse result = storeService.getStoreResponseById(storeId);

    // 3. Assert
    assertNotNull(result);
    assertEquals("Haziq's Bakery", result.getStoreName());
    assertEquals(5L, result.getSupplierId());
    assertEquals("Red Door", result.getPickupInstructions());

    verify(storeRepository, times(1)).findById(storeId);
  }

  @Test
  void deleteStore_ShouldDelete_WhenStoreExists() {
    // Arrange
    Long storeId = 1L;
    when(storeRepository.existsById(storeId)).thenReturn(true);

    // Act
    storeService.deleteStore(storeId);

    // Assert
    verify(storeRepository, times(1)).deleteById(storeId);
  }

  @Test
  void deleteStore_ShouldThrowException_WhenStoreDoesNotExist() {
    // Arrange
    Long storeId = 99L;
    when(storeRepository.existsById(storeId)).thenReturn(false);

    // Act & Assert
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              storeService.deleteStore(storeId);
            });

    assertEquals("Store not found", exception.getMessage());
    verify(storeRepository, never()).deleteById(anyLong());
  }

  @Test
  void createStore_ShouldThrow_WhenSupplierDoesNotExist() {
    StoreRequest dto = new StoreRequest();
    dto.setSupplierId(123L);

    when(supplierProfileRepository.findById(123L)).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class, () -> storeService.createStore(dto));

    assertEquals("Supplier not found", ex.getMessage());
    verify(storeRepository, never()).save(any(Store.class));
  }

  @Test
  void getAllStores_ShouldMapResponses_AndHandleNullSupplierProfile() {
    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(10L);

    Store withSupplier = new Store();
    withSupplier.setStoreId(1L);
    withSupplier.setStoreName("Store A");
    withSupplier.setSupplierProfile(supplier);

    Store withoutSupplier = new Store();
    withoutSupplier.setStoreId(2L);
    withoutSupplier.setStoreName("Store B");
    withoutSupplier.setSupplierProfile(null);

    when(storeRepository.findBySupplierProfile_SupplierId(10L))
        .thenReturn(List.of(withSupplier, withoutSupplier));

    List<StoreResponse> responses = storeService.getAllStores(10L);

    assertEquals(2, responses.size());
    assertEquals(10L, responses.get(0).getSupplierId());
    assertNull(responses.get(1).getSupplierId());
  }

  @Test
  void getStoreById_ShouldThrow_WhenStoreDoesNotExist() {
    when(storeRepository.findById(404L)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> storeService.getStoreById(404L));

    assertEquals("Store not found", ex.getMessage());
  }

  @Test
  void updateStore_ShouldUpdateAndReturnMappedResponse() {
    Long storeId = 50L;

    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(3L);

    Store existingStore = new Store();
    existingStore.setStoreId(storeId);
    existingStore.setSupplierProfile(supplier);

    StoreRequest dto = new StoreRequest();
    dto.setStoreName("Updated Store");
    dto.setAddressLine("Updated Address");
    dto.setPostalCode("654321");
    dto.setLat(new BigDecimal("1.31"));
    dto.setLng(new BigDecimal("103.91"));
    dto.setOpeningHours("9-5");
    dto.setDescription("Updated description");
    dto.setPickupInstructions("Collect at counter");

    when(storeRepository.findById(storeId)).thenReturn(Optional.of(existingStore));
    when(storeRepository.save(existingStore)).thenReturn(existingStore);

    StoreResponse response = storeService.updateStore(storeId, dto);

    assertEquals("Updated Store", response.getStoreName());
    assertEquals("Updated Address", response.getAddressLine());
    assertEquals("654321", response.getPostalCode());
    assertEquals(3L, response.getSupplierId());
    verify(storeRepository).save(existingStore);
  }

  @Test
  void updateStore_ShouldThrow_WhenStoreDoesNotExist() {
    when(storeRepository.findById(88L)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class, () -> storeService.updateStore(88L, new StoreRequest()));

    assertEquals("Store not found", ex.getMessage());
    verify(storeRepository, never()).save(any(Store.class));
  }
}
