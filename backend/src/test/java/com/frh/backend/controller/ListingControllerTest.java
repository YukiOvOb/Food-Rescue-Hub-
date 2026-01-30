package com.frh.backend.controller;

import com.frh.backend.Model.Store;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.repository.SupplierProfileRepository;
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

    @Autowired
    private SupplierProfileRepository supplierProfileRepository;

    @Autowired
    private StoreRepository storeRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        listingRepository.deleteAll(); // keep DB clean for each test
      storeRepository.deleteAll();
      supplierProfileRepository.deleteAll();

      // create a minimal supplier and store to attach listings to
      SupplierProfile supplier = new SupplierProfile();
      supplier.setEmail("test@example.com");
      supplier.setPassword("password123");
      supplier = supplierProfileRepository.save(supplier);

      Store store = new Store();
      store.setSupplierProfile(supplier);
      store.setStoreName("Test Store");
      store.setAddressLine("123 Test St");
      store = storeRepository.save(store);

      // make storeId available via system property for tests if needed
      System.setProperty("test.storeId", String.valueOf(store.getStoreId()));
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

        String storeId = System.getProperty("test.storeId");

        mockMvc.perform(
              post("/api/supplier/listings?storeId=" + storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        containsString("Rescue price must be lower than original price")
                ));
    }
}
