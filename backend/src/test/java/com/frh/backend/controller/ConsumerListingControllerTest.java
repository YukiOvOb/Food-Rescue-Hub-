package com.frh.backend.controller;

import com.frh.backend.dto.ListingDTO;
import com.frh.backend.service.ListingService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ConsumerListingController.class)
class ConsumerListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingService listingService;


    /* --------------------------------
       GET ALL LISTINGS
       -------------------------------- */
    @Test
    void getAllListings_success() throws Exception {

        ListingDTO listing = new ListingDTO();
        listing.setListingId(1L);
        listing.setTitle("Bread");

        Mockito.when(listingService.getAllActiveListings())
                .thenReturn(List.of(listing));

        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Bread"));
    }

    /* --------------------------------
       GET NEARBY LISTINGS
       -------------------------------- */
    @Test
    void getNearbyListings_success() throws Exception {

        ListingDTO listing = new ListingDTO();
        listing.setListingId(2L);
        listing.setTitle("Nearby Cake");

        Mockito.when(listingService.getNearbyListings(1.3521, 103.8198, 5.0))
                .thenReturn(List.of(listing));

        mockMvc.perform(get("/api/listings/nearby")
                        .param("lat", "1.3521")
                        .param("lng", "103.8198")
                        .param("radius", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Nearby Cake"));
    }

    /* --------------------------------
       GET LISTINGS BY CATEGORY
       -------------------------------- */
    @Test
    void getListingsByCategory_success() throws Exception {

        ListingDTO listing = new ListingDTO();
        listing.setListingId(3L);
        listing.setTitle("Bakery Item");

        Mockito.when(listingService.getListingsByCategory("Bakery"))
                .thenReturn(List.of(listing));

        mockMvc.perform(get("/api/listings/category/{category}", "Bakery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Bakery Item"));
    }
}
