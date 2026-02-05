package com.frh.backend.controller;

import com.frh.backend.dto.StoreRequest;
import com.frh.backend.dto.StoreResponse;
import com.frh.backend.service.StoreService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreService storeService;

    @Autowired
    private ObjectMapper objectMapper;

    /* --------------------------------
       CREATE STORE – SUCCESS
       -------------------------------- */
    @Test
    void createNewStore_success() throws Exception {

        StoreRequest request = new StoreRequest();
        request.setSupplierId(10L);
        request.setStoreName("Bakery Shop");
        request.setAddressLine("Singapore");
        request.setLat(BigDecimal.valueOf(1.3521));
        request.setLng(BigDecimal.valueOf(103.8198));

        StoreResponse response = new StoreResponse();
        response.setStoreId(1L);
        response.setStoreName("Bakery Shop");

        Mockito.when(storeService.createStore(Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/stores/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storeId").value(1))
                .andExpect(jsonPath("$.storeName").value("Bakery Shop"));
    }

    /* --------------------------------
       CREATE STORE – VALIDATION ERROR
       -------------------------------- */
    @Test
    void createNewStore_validationError() throws Exception {

        StoreRequest invalidRequest = new StoreRequest(); // missing required fields

        mockMvc.perform(post("/api/stores/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /* --------------------------------
       GET STORE BY ID
       -------------------------------- */
    @Test
    void getStoreById_success() throws Exception {

        StoreResponse response = new StoreResponse();
        response.setStoreId(2L);
        response.setStoreName("Cafe");

        Mockito.when(storeService.getStoreResponseById(2L))
                .thenReturn(response);

        mockMvc.perform(get("/api/stores/{storeId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeId").value(2))
                .andExpect(jsonPath("$.storeName").value("Cafe"));
    }

    /* --------------------------------
       GET STORES BY SUPPLIER
       -------------------------------- */
    @Test
    void getStoresBySupplier_success() throws Exception {

        StoreResponse store = new StoreResponse();
        store.setStoreId(3L);
        store.setStoreName("Restaurant");

        Mockito.when(storeService.getAllStores(1L))
                .thenReturn(List.of(store));

        mockMvc.perform(get("/api/stores/supplier/{supplierId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].storeName").value("Restaurant"));
    }

    /* --------------------------------
       UPDATE STORE
       -------------------------------- */
    @Test
    void updateStore_success() throws Exception {

        StoreRequest request = new StoreRequest();
        request.setSupplierId(10L);
        request.setStoreName("Updated Store");
        request.setAddressLine("New Address");
        request.setLat(BigDecimal.valueOf(1.3521));
        request.setLng(BigDecimal.valueOf(103.8198));

        StoreResponse response = new StoreResponse();
        response.setStoreId(4L);
        response.setStoreName("Updated Store");

        Mockito.when(storeService.updateStore(Mockito.eq(4L), Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/stores/update/{storeId}", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("Updated Store"));
    }

    /* --------------------------------
       DELETE STORE
       -------------------------------- */
    @Test
    void deleteStore_success() throws Exception {

        Mockito.doNothing()
                .when(storeService)
                .deleteStore(5L);

        mockMvc.perform(delete("/api/stores/delete/{storeId}", 5L))
                .andExpect(status().isNoContent());
    }
}
