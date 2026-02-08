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

import static org.hamcrest.Matchers.containsString;
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
       CREATE - UNEXPECTED ERROR
       ----------------------------- */
    @Test
    void createListing_unexpectedError() throws Exception {

        Store store = new Store();

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        Mockito.when(listingRepository.save(Mockito.any()))
                .thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validListing())))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Unexpected server error")));
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
       GET BY ID - FOUND
       ----------------------------- */
    @Test
    void getListingById_found() throws Exception {

        Listing listing = validListing();
        listing.setListingId(1L);

        Mockito.when(listingRepository.findById(1L))
                .thenReturn(Optional.of(listing));

        mockMvc.perform(get("/api/supplier/listings/{id}", 1L))
                .andExpect(status().isOk());
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
       UPDATE - VALIDATION ERROR
       ----------------------------- */
    @Test
    void updateListing_validationError() throws Exception {

        Listing existing = validListing();
        existing.setListingId(1L);

        Listing invalid = validListing();
        invalid.setRescuePrice(BigDecimal.valueOf(20));

        Mockito.when(listingRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        mockMvc.perform(put("/api/supplier/listings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Rescue price must be lower than original price")));
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

    /* -----------------------------
       CREATE - TITLE BLANK
       ----------------------------- */
    @Test
    void createListing_titleBlank() throws Exception {

        Store store = new Store();
        Listing invalid = validListing();
        invalid.setTitle("   ");

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Title is required")));
    }

    /* -----------------------------
       CREATE - ORIGINAL PRICE MUST BE POSITIVE
       ----------------------------- */
    @Test
    void createListing_originalPriceMustBePositive() throws Exception {

        Store store = new Store();
        Listing invalid = validListing();
        invalid.setOriginalPrice(BigDecimal.ZERO);

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Original price must be greater than 0")));
    }

    /* -----------------------------
       CREATE - RESCUE PRICE CANNOT BE NEGATIVE
       ----------------------------- */
    @Test
    void createListing_rescuePriceCannotBeNegative() throws Exception {

        Store store = new Store();
        Listing invalid = validListing();
        invalid.setRescuePrice(BigDecimal.valueOf(-1));

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Rescue price cannot be negative")));
    }

    /* -----------------------------
       CREATE - RESCUE PRICE MUST BE LOWER
       ----------------------------- */
    @Test
    void createListing_rescuePriceMustBeLowerThanOriginalPrice() throws Exception {

        Store store = new Store();
        Listing invalid = validListing();
        invalid.setRescuePrice(BigDecimal.valueOf(10));

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Rescue price must be lower than original price")));
    }

    /* -----------------------------
       CREATE - PICKUP START MUST BE IN FUTURE
       ----------------------------- */
    @Test
    void createListing_pickupStartMustBeInFuture() throws Exception {

        Store store = new Store();
        Listing invalid = validListing();
        invalid.setPickupStart(LocalDateTime.now().minusHours(1));
        invalid.setPickupEnd(LocalDateTime.now().plusHours(1));
        invalid.setExpiryAt(LocalDateTime.now().plusHours(2));

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Pickup start time must be in the future")));
    }

    /* -----------------------------
       CREATE - PICKUP END BEFORE START
       ----------------------------- */
    @Test
    void createListing_pickupEndCannotBeBeforeStart() throws Exception {

        Store store = new Store();
        Listing invalid = validListing();
        invalid.setPickupStart(LocalDateTime.now().plusHours(3));
        invalid.setPickupEnd(LocalDateTime.now().plusHours(2));
        invalid.setExpiryAt(LocalDateTime.now().plusHours(4));

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Pickup end time cannot be before start time")));
    }

    /* -----------------------------
       CREATE - EXPIRY BEFORE PICKUP END
       ----------------------------- */
    @Test
    void createListing_expiryMustBeAfterPickupEnd() throws Exception {

        Store store = new Store();
        Listing invalid = validListing();
        invalid.setPickupStart(LocalDateTime.now().plusHours(1));
        invalid.setPickupEnd(LocalDateTime.now().plusHours(3));
        invalid.setExpiryAt(LocalDateTime.now().plusHours(2));

        Mockito.when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        mockMvc.perform(post("/api/supplier/listings")
                        .param("storeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Expiry time must be after pickup end time")));
    }
}
