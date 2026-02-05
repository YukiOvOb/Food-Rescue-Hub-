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
}
