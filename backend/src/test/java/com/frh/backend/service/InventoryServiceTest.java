package com.frh.backend.service;

import com.frh.backend.Model.Inventory;
import com.frh.backend.exception.InsufficientStockException;
import com.frh.backend.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void checkStock_returnsTrueWhenEnough() {
        Inventory inventory = inventoryWithQty(7);
        when(inventoryRepository.findByListingListingId(1L)).thenReturn(Optional.of(inventory));

        boolean result = inventoryService.checkStock(1L, 5);

        assertEquals(true, result);
    }

    @Test
    void checkStock_missingInventory_throws() {
        when(inventoryRepository.findByListingListingId(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inventoryService.checkStock(1L, 2));
        assertEquals("Inventory not found for listing 1", ex.getMessage());
    }

    @Test
    void decrementStock_success() {
        Inventory inventory = inventoryWithQty(9);
        when(inventoryRepository.findByListingIdForUpdate(2L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.decrementStock(2L, 4);

        assertEquals(5, updated.getQtyAvailable());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void decrementStock_insufficient_throws() {
        Inventory inventory = inventoryWithQty(3);
        when(inventoryRepository.findByListingIdForUpdate(2L)).thenReturn(Optional.of(inventory));

        InsufficientStockException ex = assertThrows(
            InsufficientStockException.class,
            () -> inventoryService.decrementStock(2L, 4)
        );

        assertEquals(2L, ex.getListingId());
        assertEquals(4, ex.getRequested());
        assertEquals(3, ex.getAvailable());
    }

    @Test
    void restoreStock_addsQuantity() {
        Inventory inventory = inventoryWithQty(3);
        when(inventoryRepository.findByListingIdForUpdate(3L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.restoreStock(3L, 2);

        assertEquals(5, updated.getQtyAvailable());
    }

    @Test
    void adjustInventory_belowZero_throws() {
        Inventory inventory = inventoryWithQty(2);
        when(inventoryRepository.findByListingIdForUpdate(4L)).thenReturn(Optional.of(inventory));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> inventoryService.adjustInventory(4L, -3)
        );

        assertEquals("Cannot adjust inventory below zero. Current: 2, delta: -3", ex.getMessage());
    }

    @Test
    void adjustInventory_success() {
        Inventory inventory = inventoryWithQty(10);
        when(inventoryRepository.findByListingIdForUpdate(4L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.adjustInventory(4L, -3);

        assertEquals(7, updated.getQtyAvailable());
    }

    @Test
    void getInventory_returnsInventory() {
        Inventory inventory = inventoryWithQty(12);
        when(inventoryRepository.findByListingListingId(5L)).thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.getInventory(5L);

        assertEquals(12, result.getQtyAvailable());
    }

    @Test
    void getInventory_missing_throws() {
        when(inventoryRepository.findByListingListingId(5L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inventoryService.getInventory(5L));

        assertEquals("Inventory not found for listing 5", ex.getMessage());
    }

    private static Inventory inventoryWithQty(int qty) {
        Inventory inventory = new Inventory();
        inventory.setQtyAvailable(qty);
        inventory.setQtyReserved(0);
        return inventory;
    }
}
