package com.frh.backend.controller;

import com.frh.backend.dto.StoreRequest;
import com.frh.backend.Model.Store;
import com.frh.backend.dto.StoreResponse;
import com.frh.backend.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreController.class) // 1. Only load the StoreController
public class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc; // 2. The tool to fake HTTP requests

    @MockitoBean
    private StoreService storeService; // 3. Mock the service (don't use the real database)

    private final ObjectMapper objectMapper = new ObjectMapper(); // 4. Tool to convert Objects <-> JSON string

    private Store sampleStore;
    private StoreRequest sampleRequest;

    @BeforeEach
    void setUp() {
        // Setup dummy data for tests
        sampleStore = new Store();
        sampleStore.setStoreId(1L);
        sampleStore.setStoreName("Bakery");
        sampleStore.setAddressLine("123 NUS Road");
        sampleStore.setLat(new BigDecimal("1.3521"));
        sampleStore.setLng(new BigDecimal("103.8198"));

        sampleRequest = new StoreRequest();
        sampleRequest.setStoreName("Bakery");
        sampleRequest.setAddressLine("123 NUS Road");
        sampleRequest.setLat(new BigDecimal("1.3521"));
        sampleRequest.setLng(new BigDecimal("103.8198"));
        sampleRequest.setSupplierId(5L);
    }

    // --- TEST 1: CREATE STORE (POST) ---
    // Happy Path
    @Test
    void shouldCreateNewStore() throws Exception {
        // 1. Create a StoreResponse (DTO) for the mock to return
        StoreResponse mockResponse = new StoreResponse();
        mockResponse.setStoreId(1L);
        mockResponse.setStoreName("Bakery");
        mockResponse.setAddressLine("123 NUS Road");
        mockResponse.setPostalCode("119077");
        mockResponse.setLat(new BigDecimal("1.3521"));
        mockResponse.setLng(new BigDecimal("103.8198"));
        mockResponse.setActive(true);

        // 2. Mock the service: it now returns a StoreResponse
        given(storeService.createStore(any(StoreRequest.class))).willReturn(mockResponse);

        // 3. Perform the request
        mockMvc.perform(post("/api/stores/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storeName").value("Bakery"))
                .andExpect(jsonPath("$.storeId").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    // --- TEST 2: GET ALL STORES (GET) ---
    @Test
    void shouldReturnStoresForSupplier() throws Exception {
        // A. Given
        // Create a list containing a DTO instead of an Entity
        StoreResponse mockResponse = new StoreResponse();
        mockResponse.setStoreId(1L);
        mockResponse.setStoreName("Bakery");

        List<StoreResponse> allStores = Collections.singletonList(mockResponse);
        given(storeService.getAllStores(5L)).willReturn(allStores);

        // B. When & Then
        mockMvc.perform(get("/api/stores/supplier/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].storeName").value("Bakery"))
                .andExpect(jsonPath("$[0].storeId").value(1));
    }

    // --- TEST 3: HANDLE INVALID INPUT (Bad Request) ---
    @Test
    void shouldReturn400_WhenStoreNameIsEmpty() throws Exception {
        // Create an invalid request (missing name)
        StoreRequest invalidRequest = new StoreRequest();
        invalidRequest.setAddressLine("Somewhere");
        // Name is null!

        mockMvc.perform(post("/api/stores/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request
        // (Note: This assumes you added @Valid in your controller)
    }

    @Test
    void shouldUpdateStore() throws Exception {
        // 1. Create a StoreResponse (DTO) for the mock to return instead of the Entity
        StoreResponse mockResponse = new StoreResponse();
        mockResponse.setStoreId(1L);
        mockResponse.setStoreName("Updated Bakery Name");
        mockResponse.setAddressLine("123 NUS Road");
        mockResponse.setPostalCode("119077");
        mockResponse.setLat(new java.math.BigDecimal("1.3521"));
        mockResponse.setLng(new java.math.BigDecimal("103.8198"));
        mockResponse.setActive(true);

        // 2. Mock the service: it must now return the StoreResponse DTO
        given(storeService.updateStore(any(Long.class), any(StoreRequest.class)))
                .willReturn(mockResponse);

        // 3. Perform the request
        mockMvc.perform(put("/api/stores/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("Updated Bakery Name"))
                .andExpect(jsonPath("$.storeId").value(1));
    }

    @Test
    void shouldDeleteStore() throws Exception {
        // We don't need 'given' for void methods unless we want to throw an error
        mockMvc.perform(delete("/api/stores/delete/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404_WhenDeletingNonExistentStore() throws Exception {
        // 1. Setup: Force service to throw the exception
        doThrow(new RuntimeException("Store not found"))
                .when(storeService).deleteStore(99L);
        // 2. Act & Assert
        mockMvc.perform(delete("/api/stores/delete/99"))
                .andExpect(status().isNotFound()) // This will now work because of the GlobalExceptionHandler
                .andExpect(jsonPath("$.error").value("Store not found"));
    }
}