package com.frh.backend.service;

import com.frh.backend.dto.StoreRequest;
import com.frh.backend.Model.Store;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.StoreResponse;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.repository.SupplierProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito
@ActiveProfiles("test")
public class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private SupplierProfileRepository supplierProfileRepository;

    @InjectMocks
    private StoreService storeService;

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
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            storeService.deleteStore(storeId);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, never()).deleteById(anyLong());
    }
}