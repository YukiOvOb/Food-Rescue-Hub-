package com.frh.backend.controller;

import com.frh.backend.Model.Inventory;
import com.frh.backend.dto.InventoryAdjustRequest;
import com.frh.backend.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SupplierInventoryController.class)
class SupplierInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    /* --------------------------------
       GET INVENTORY – SUCCESS
       -------------------------------- */
    @Test
    void getStock_success() throws Exception {

        Inventory inventory = new Inventory();
        inventory.setQtyAvailable(10);

        Mockito.when(inventoryService.getInventory(1L))
                .thenReturn(inventory);

        mockMvc.perform(get("/api/supplier/inventory/{listingId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(10));
    }

    /* --------------------------------
       ADJUST INVENTORY – SUCCESS
       -------------------------------- */
    @Test
    void adjustInventory_success() throws Exception {

        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setDelta(5);

        Inventory updated = new Inventory();
        updated.setQtyAvailable(15);

        Mockito.when(inventoryService.adjustInventory(1L, 5))
                .thenReturn(updated);

        mockMvc.perform(put("/api/supplier/inventory/{listingId}/adjust", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(15));
    }

    /* --------------------------------
       ADJUST INVENTORY – BUSINESS ERROR
       -------------------------------- */
    @Test
    void adjustInventory_negativeStock_error() throws Exception {

        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setDelta(-20);

        Mockito.when(inventoryService.adjustInventory(1L, -20))
                .thenThrow(new IllegalArgumentException("Cannot adjust below zero"));

        mockMvc.perform(put("/api/supplier/inventory/{listingId}/adjust", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Cannot adjust below zero"));
    }

    /* --------------------------------
       ADJUST INVENTORY – VALIDATION ERROR
       -------------------------------- */
    @Test
    void adjustInventory_validationError() throws Exception {

        // Invalid body: missing delta
        InventoryAdjustRequest invalidRequest = new InventoryAdjustRequest();

        mockMvc.perform(put("/api/supplier/inventory/{listingId}/adjust", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /* --------------------------------
       ADJUST INVENTORY – SERVICE ERROR
       -------------------------------- */
    @Test
    void adjustInventory_serviceError() throws Exception {

        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setDelta(10);

        Mockito.when(inventoryService.adjustInventory(1L, 10))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/supplier/inventory/{listingId}/adjust", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    /* --------------------------------
       GET INVENTORY – NOT FOUND
       -------------------------------- */
    @Test
    void getStock_notFound() throws Exception {

        Mockito.when(inventoryService.getInventory(99L))
                .thenThrow(new RuntimeException("Listing not found"));

        mockMvc.perform(get("/api/supplier/inventory/{listingId}", 99L))
                .andExpect(status().isNotFound());
    }

    /* --------------------------------
       ADJUST INVENTORY – ZERO DELTA
       -------------------------------- */
    @Test
    void adjustInventory_zeroDelta() throws Exception {

        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setDelta(0);

        Inventory updated = new Inventory();
        updated.setQtyAvailable(10);

        Mockito.when(inventoryService.adjustInventory(1L, 0))
                .thenReturn(updated);

        mockMvc.perform(put("/api/supplier/inventory/{listingId}/adjust", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(10));
    }

    /* --------------------------------
       ADJUST INVENTORY – LARGE POSITIVE DELTA
       -------------------------------- */
    @Test
    void adjustInventory_largeDelta() throws Exception {

        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setDelta(1000);

        Inventory updated = new Inventory();
        updated.setQtyAvailable(1010);

        Mockito.when(inventoryService.adjustInventory(1L, 1000))
                .thenReturn(updated);

        mockMvc.perform(put("/api/supplier/inventory/{listingId}/adjust", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(1010));
    }

    /* --------------------------------
       GET INVENTORY – EMPTY INVENTORY
       -------------------------------- */
    @Test
    void getStock_emptyInventory() throws Exception {

        Inventory inventory = new Inventory();
        inventory.setQtyAvailable(0);

        Mockito.when(inventoryService.getInventory(1L))
                .thenReturn(inventory);

        mockMvc.perform(get("/api/supplier/inventory/{listingId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(0));
    }

    /* --------------------------------
       ADJUST INVENTORY – EXACT ZERO RESULT
       -------------------------------- */
    @Test
    void adjustInventory_toZero() throws Exception {

        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setDelta(-10);

        Inventory updated = new Inventory();
        updated.setQtyAvailable(0);

        Mockito.when(inventoryService.adjustInventory(1L, -10))
                .thenReturn(updated);

        mockMvc.perform(put("/api/supplier/inventory/{listingId}/adjust", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(0));
    }

    /* --------------------------------
       GET INVENTORY – HIGH STOCK
       -------------------------------- */
    @Test
    void getStock_highQuantity() throws Exception {

        Inventory inventory = new Inventory();
        inventory.setQtyAvailable(9999);

        Mockito.when(inventoryService.getInventory(1L))
                .thenReturn(inventory);

        mockMvc.perform(get("/api/supplier/inventory/{listingId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(9999));
    }

    /* --------------------------------
       ADJUST INVENTORY – CONCURRENT ADJUSTMENT
       -------------------------------- */
    @Test
    void adjustInventory_multipleAdjustments() throws Exception {

        InventoryAdjustRequest request1 = new InventoryAdjustRequest();
        request1.setDelta(5);

        InventoryAdjustRequest request2 = new InventoryAdjustRequest();
        request2.setDelta(-3);

        Inventory inventory1 = new Inventory();
        inventory1.setQtyAvailable(15);

        Inventory inventory2 = new Inventory();
        inventory2.setQtyAvailable(12);

        Mockito.when(inventoryService.adjustInventory(1L, 5))
                .thenReturn(inventory1);
        Mockito.when(inventoryService.adjustInventory(1L, -3))
                .thenReturn(inventory2);

        // First adjustment
        mockMvc.perform(put("/api/supplier/inventory/{listingId}/adjust", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(15));

        // Second adjustment
        mockMvc.perform(put("/api/supplier/inventory/{listingId}/adjust", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(12));
    }

    /* --------------------------------
       GET INVENTORY – SPECIFIC LISTING ISOLATION
       -------------------------------- */
    @Test
    void getStock_differentListings() throws Exception {

        Inventory inventory1 = new Inventory();
        inventory1.setQtyAvailable(20);

        Inventory inventory2 = new Inventory();
        inventory2.setQtyAvailable(30);

        Mockito.when(inventoryService.getInventory(1L))
                .thenReturn(inventory1);
        Mockito.when(inventoryService.getInventory(2L))
                .thenReturn(inventory2);

        mockMvc.perform(get("/api/supplier/inventory/{listingId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(20));

        mockMvc.perform(get("/api/supplier/inventory/{listingId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(30));
    }
}
