package com.frh.backend.controller;

import com.frh.backend.Model.Listing;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingRepository listingRepository;

    @MockBean
    private StoreRepository storeRepository;

    @Test
    void testGetAllListings_ReturnsEmptyList() throws Exception {
        // Arrange
        when(listingRepository.findAll()).thenReturn(Collections.emptyList());

        // Act + Assert
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testCreateListing_InvalidRescuePrice_ReturnsBadRequest() throws Exception {
        // Use a far future date so it always passes the "must be in future" checks
        String futureStart = "3026-01-01T10:00:00";
        String futureEnd   = "3026-01-01T12:00:00";
        String futureExpiry = "3026-01-02T12:00:00";

        String payload = """
                {
                  "title": "Bad Pricing Listing",
                  "description": "Some description",
                  "originalPrice": 5.00,
                  "rescuePrice": 10.00,
                  "pickupStart": "%s",
                  "pickupEnd": "%s",
                  "expiryAt": "%s"
                }
                """.formatted(futureStart, futureEnd, futureExpiry);

        mockMvc.perform(
                    post("/api/listings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // validateListing() returns a List<String>, so JSON is an array of messages
                .andExpect(jsonPath("$", hasItem("Rescue price must be lower than original price")));

        // For invalid input the controller should NOT try to save
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testCreateListing_ValidPayload_SavesAndReturnsListing() throws Exception {
        String futureStart = "3026-01-01T10:00:00";
        String futureEnd   = "3026-01-01T12:00:00";
        String futureExpiry = "3026-01-02T12:00:00";

        String payload = """
                {
                  "title": "Valid Listing",
                  "description": "Some description",
                  "originalPrice": 10.00,
                  "rescuePrice": 5.00,
                  "pickupStart": "%s",
                  "pickupEnd": "%s",
                  "expiryAt": "%s"
                }
                """.formatted(futureStart, futureEnd, futureExpiry);

        // This is what the controller will return as the saved listing
        Listing saved = new Listing();
        saved.setTitle("Valid Listing");
        saved.setDescription("Some description");
        saved.setOriginalPrice(BigDecimal.valueOf(10.00));
        saved.setRescuePrice(BigDecimal.valueOf(5.00));
        saved.setPickupStart(LocalDateTime.parse(futureStart));
        saved.setPickupEnd(LocalDateTime.parse(futureEnd));
        // (no need to set expiryAt or id for this test)

        when(listingRepository.save(any(Listing.class))).thenReturn(saved);

        mockMvc.perform(
                    post("/api/listings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Valid Listing"))
                .andExpect(jsonPath("$.originalPrice").value(10.00))
                .andExpect(jsonPath("$.rescuePrice").value(5.00));

        verify(listingRepository, times(1)).save(any(Listing.class));
    }
}
