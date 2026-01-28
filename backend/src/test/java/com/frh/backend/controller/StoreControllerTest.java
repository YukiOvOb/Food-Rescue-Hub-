package com.frh.backend.controller;

import com.frh.backend.DTO.StoreRequestDTO;
import com.frh.backend.Model.Store;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreController.class) // 1. Only load the StoreController
public class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc; // 2. The tool to fake HTTP requests

    @MockitoBean
    private StoreService storeService; // 3. Mock the service (don't use the real database)

    private final ObjectMapper objectMapper = new ObjectMapper(); // 4. Tool to convert Objects <-> JSON string

    private Store sampleStore;
    private StoreRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        // Setup dummy data for tests
        sampleStore = new Store();
        sampleStore.setStoreId(1L);
        sampleStore.setStoreName("Bakery");
        sampleStore.setAddressLine("123 NUS Road");
        sampleStore.setLat(new BigDecimal("1.3521"));
        sampleStore.setLng(new BigDecimal("103.8198"));

        sampleRequest = new StoreRequestDTO();
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
        // A. Given (Mock the service behavior)
        // When service.createStore is called, return the sampleStore
        given(storeService.createStore(any(StoreRequestDTO.class))).willReturn(sampleStore);

        // B. When & Then (Perform the Fake Request)
        mockMvc.perform(post("/api/stores/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest))) // Convert Java Object -> JSON String

                // C. Expectations
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(jsonPath("$.storeName").value("Bakery")) // Check JSON response
                .andExpect(jsonPath("$.storeId").value(1));
    }

    // --- TEST 2: GET ALL STORES (GET) ---
    @Test
    void shouldReturnListOfStores() throws Exception {
        // A. Given
        List<Store> allStores = Collections.singletonList(sampleStore);
        given(storeService.getAllStores()).willReturn(allStores);

        // B. When & Then
        mockMvc.perform(get("/api/stores"))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.size()").value(1)) // Expect list size is 1
                .andExpect(jsonPath("$[0].storeName").value("Bakery"));
    }

    // --- TEST 3: HANDLE INVALID INPUT (Bad Request) ---
    @Test
    void shouldReturn400_WhenStoreNameIsEmpty() throws Exception {
        // Create an invalid request (missing name)
        StoreRequestDTO invalidRequest = new StoreRequestDTO();
        invalidRequest.setAddressLine("Somewhere");
        // Name is null!

        mockMvc.perform(post("/api/stores/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request
        // (Note: This assumes you added @Valid in your controller)
    }
}