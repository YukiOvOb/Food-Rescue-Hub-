package com.frh.backend.controller;

import com.frh.backend.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ListingControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ListingRepository listingRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        listingRepository.deleteAll(); // keep DB clean for each test
    }

    // 1) Simple sanity check – GET /api/listings returns 200
    @Test
    void testGetAllListings_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk());
    }

    // 2) Price validation – rescuePrice >= originalPrice => 400 Bad Request
    @Test
    void testCreateListing_InvalidRescuePrice_ReturnsBadRequest() throws Exception {
        String json = """
          {
            "title": "Test bread",
            "description": "Some surplus bread",
            "originalPrice": 5.00,
            "rescuePrice": 6.00,
            "pickupStart": "2030-01-01T10:00:00",
            "pickupEnd": "2030-01-01T12:00:00",
            "expiryAt": "2030-01-02T00:00:00"
          }
        """;

        mockMvc.perform(
                    post("/api/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        containsString("Rescue price must be lower than original price")
                ));
    }
}
