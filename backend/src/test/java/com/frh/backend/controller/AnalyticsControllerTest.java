package com.frh.backend.controller;

import com.frh.backend.dto.TopSellingItemDto;
import com.frh.backend.service.OrderService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    /**
     * ✅ SUCCESS CASE
     */
    @Test
    void getTopSellingProducts_success() throws Exception {
        Long supplierId = 1L;

        TopSellingItemDto item1 = new TopSellingItemDto(101L, "Rice", 50L);
        TopSellingItemDto item2 = new TopSellingItemDto(102L, "Oil", 30L);

        Mockito.when(orderService.getTopSellingItems(
                supplierId, "COMPLETED", 3))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/analytics/supplier/{supplierId}/top-products", supplierId)
                        .param("limit", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Rice"))
                .andExpect(jsonPath("$[1].totalQuantity").value(30));
    }

    /**
     * ❌ ERROR CASE
     */
    @Test
    void getTopSellingProducts_exception() throws Exception {
        Long supplierId = 1L;

        Mockito.when(orderService.getTopSellingItems(
                supplierId, "COMPLETED", 3))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/analytics/supplier/{supplierId}/top-products", supplierId)
                        .param("limit", "3"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to retrieve top selling products"));
    }
}
