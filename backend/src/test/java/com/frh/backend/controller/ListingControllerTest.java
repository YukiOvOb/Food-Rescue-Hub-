package com.frh.backend.controller;

import com.frh.backend.Model.Listing;
import com.frh.backend.Model.Store;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.StoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ListingController.class)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListingRepository listingRepository;

    @MockitoBean
    private StoreRepository storeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /* -----------------------------
       HELPERS
       ----------------------------- */
    private Listing validListing() {
        Listing l = new Listing();
        l.setTitle("Bread");
        l.setDescription("Fresh bread");
        l.setOriginalPrice(BigDecimal.valueOf(10));
        l.setRescuePrice(BigDecimal.valueOf(5));
        l.setPickupStart(LocalDateTime.now().plusHours(1));
        l.setPickupEnd(LocalDateTime.now().plusHours(2));
        l.setExpiryAt(LocalDateTime.now().plusHours(3));
        return l;
    }

    /* -----------------------------
       CREATE – STORE MISSING
       ----------------------------- */
    @Test
    void createListing_storeMissing() throws Exception {

        mockMvc.perform(post("/api/supplier/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validListing())))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       CREATE – STORE NOT FOUND
       ----------------------------- */
    @Test
    void createListing_storeNotFound() throws Exception {

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validListing())))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       CREATE – VALIDATION ERROR
       ----------------------------- */
    @Test
    void createListing_validationError() throws Exception {

        Store store = new Store();
        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        Listing invalid = new Listing(); // missing fields

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       CREATE – SUCCESS
       ----------------------------- */
    @Test
    void createListing_success() throws Exception {

        Store store = new Store();
        Listing listing = validListing();
        listing.setListingId(1L);

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        Mockito.when(listingRepository.save(Mockito.any()))
                .thenReturn(listing);

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validListing())))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       CREATE – DB ERROR
       ----------------------------- */
    @Test
    void createListing_dbError() throws Exception {

        Store store = new Store();

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        Mockito.when(listingRepository.save(Mockito.any()))
                .thenThrow(new DataIntegrityViolationException("constraint"));

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validListing())))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       GET ALL
       ----------------------------- */
    @Test
    void getAllListings() throws Exception {

        Mockito.when(listingRepository.findAll())
                .thenReturn(List.of(new Listing()));

        mockMvc.perform(get("/api/supplier/listings"))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       GET BY SUPPLIER
       ----------------------------- */
    @Test
    void getListingsBySupplier() throws Exception {

        Mockito.when(listingRepository
                .findByStore_SupplierProfile_SupplierId(1L))
                .thenReturn(List.of(new Listing()));

        mockMvc.perform(get("/api/supplier/listings/supplier/{id}", 1L))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       GET BY ID – NOT FOUND
       ----------------------------- */
    @Test
    void getListingById_notFound() throws Exception {

        Mockito.when(listingRepository.findById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/supplier/listings/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    /* -----------------------------
       UPDATE – NOT FOUND
       ----------------------------- */
    @Test
    void updateListing_notFound() throws Exception {

        Mockito.when(listingRepository.findById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/supplier/listings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validListing())))
                .andExpect(status().isNotFound());
    }

    /* -----------------------------
       UPDATE – SUCCESS
       ----------------------------- */
    @Test
    void updateListing_success() throws Exception {

        Listing existing = validListing();
        existing.setListingId(1L);

        Mockito.when(listingRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        Mockito.when(listingRepository.save(Mockito.any()))
                .thenReturn(existing);

        mockMvc.perform(put("/api/supplier/listings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validListing())))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       DELETE – NOT FOUND
       ----------------------------- */
    @Test
    void deleteListing_notFound() throws Exception {

        Mockito.when(listingRepository.existsById(1L))
                .thenReturn(false);

        mockMvc.perform(delete("/api/supplier/listings/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    /* -----------------------------
       DELETE – SUCCESS
       ----------------------------- */
    @Test
    void deleteListing_success() throws Exception {

        Mockito.when(listingRepository.existsById(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/supplier/listings/{id}", 1L))
                .andExpect(status().isOk());
    }
}
