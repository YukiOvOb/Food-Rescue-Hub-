package com.frh.backend.service;

import com.frh.backend.DTO.StoreRequestDTO;
import com.frh.backend.Model.Store;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.repository.SupplierProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito
public class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private SupplierProfileRepository supplierRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    void createStore_ShouldReturnStore_WhenValidRequest() {
        // 1. Arrange (Prepare fake data)
        Long supplierId = 1L;

        // The DTO we send in
        StoreRequestDTO dto = new StoreRequestDTO();
        dto.setSupplierId(supplierId);
        dto.setStoreName("Test Bakery");
        dto.setLat(new BigDecimal("1.29"));
        dto.setLng(new BigDecimal("103.85"));

        // The Dummy Supplier found in DB
        SupplierProfile mockSupplier = new SupplierProfile();
        mockSupplier.setSupplierId(supplierId);

        // The Store that gets saved (Simulated)
        Store savedStore = new Store();
        savedStore.setStoreId(10L); // DB gives it an ID
        savedStore.setStoreName("Test Bakery");
        savedStore.setSupplierProfile(mockSupplier);

        // Tell Mockito what to do when repositories are called
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(mockSupplier));
        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

        // 2. Act (Call the actual method)
        Store result = storeService.createStore(dto);

        // 3. Assert (Verify the results)
        assertNotNull(result);
        assertEquals(10L, result.getStoreId());
        assertEquals("Test Bakery", result.getStoreName());

        // Verify dependencies were actually called
        verify(supplierRepository, times(1)).findById(supplierId);
        verify(storeRepository, times(1)).save(any(Store.class));
    }
}