package com.frh.backend.controller;

import com.frh.backend.Model.Inventory;
import com.frh.backend.Model.Listing;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.service.InventoryService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SupplierInventoryPageController.class)
@Import(SupplierInventoryPageControllerTest.TestViewConfig.class)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class SupplierInventoryPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private ListingRepository listingRepository;

    /* --------------------------------
       RENDER INVENTORY PAGE
       -------------------------------- */
    @Test
    void showInventory_success() throws Exception {

        Listing listing = new Listing();
        listing.setListingId(1L);

        Inventory inventory = new Inventory();
        inventory.setQtyAvailable(10);

        Mockito.when(listingRepository.findByStoreStoreId(100L))
                .thenReturn(List.of(listing));

        Mockito.when(inventoryService.getInventory(1L))
                .thenReturn(inventory);

        mockMvc.perform(get("/supplier/inventory/{storeId}", 100L))
                .andExpect(status().isOk())
                .andExpect(view().name("supplier/inventory-manage"))
                .andExpect(model().attributeExists("storeId"))
                .andExpect(model().attributeExists("rows"));
    }

    /* --------------------------------
       ADJUST STOCK – SUCCESS
       -------------------------------- */
    @Test
    void adjustStock_success() throws Exception {

        Mockito.when(inventoryService.adjustInventory(1L, 5))
                .thenReturn(new Inventory());

        mockMvc.perform(post("/supplier/inventory/{storeId}/adjust", 100L)
                        .param("listingId", "1")
                        .param("delta", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/supplier/inventory/100"))
                .andExpect(flash().attributeExists("successMsg"));
    }

    /* --------------------------------
       ADJUST STOCK – FAILURE
       -------------------------------- */
    @Test
    void adjustStock_failure() throws Exception {

        Mockito.doThrow(new RuntimeException("Cannot adjust below zero"))
                .when(inventoryService)
                .adjustInventory(1L, -5);

        mockMvc.perform(post("/supplier/inventory/{storeId}/adjust", 100L)
                        .param("listingId", "1")
                        .param("delta", "-5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/supplier/inventory/100"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

        @TestConfiguration
        static class TestViewConfig {
                @Bean
                @Primary
                ViewResolver testViewResolver() {
                        return new ViewResolver() {
                                @Override
                                public View resolveViewName(String viewName, Locale locale) {
                                        return new AbstractView() {
                                                @Override
                                                protected void renderMergedOutputModel(
                                                                Map<String, Object> model,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) {
                                                        response.setStatus(HttpServletResponse.SC_OK);
                                                }
                                        };
                                }
                        };
                }
        }
}
