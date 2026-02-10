package com.frh.backend.controller;

import com.frh.backend.dto.TopSellingItemDto;
import com.frh.backend.dto.Co2CategoryBreakdownDto;
import com.frh.backend.dto.Co2SummaryDto;
import com.frh.backend.service.Co2AnalyticsService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @MockitoBean
    private Co2AnalyticsService co2AnalyticsService;

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

    @Test
    void getCo2Summary_success() throws Exception {
        Long supplierId = 1L;

        Co2SummaryDto summary = new Co2SummaryDto();
        summary.setDays(30);
        summary.setFrom(LocalDateTime.now().minusDays(30));
        summary.setTo(LocalDateTime.now());
        summary.setTotalCo2Kg(new BigDecimal("12.345"));
        summary.setTotalWeightKg(new BigDecimal("6.789"));
        summary.setCategories(List.of(
            new Co2CategoryBreakdownDto(1L, "Beef (beef herd)", new BigDecimal("2.000"), new BigDecimal("5.000"))
        ));

        Mockito.when(co2AnalyticsService.getCo2Summary(supplierId, 30)).thenReturn(summary);

        mockMvc.perform(get("/api/analytics/supplier/{supplierId}/co2", supplierId)
                        .param("days", "30")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCo2Kg").value(12.345))
                .andExpect(jsonPath("$.totalWeightKg").value(6.789))
                .andExpect(jsonPath("$.categories.length()").value(1))
                .andExpect(jsonPath("$.categories[0].categoryName").value("Beef (beef herd)"));
    }

    @Test
    void getCo2Summary_exception() throws Exception {
        Long supplierId = 1L;

        Mockito.when(co2AnalyticsService.getCo2Summary(supplierId, 30))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/analytics/supplier/{supplierId}/co2", supplierId)
                        .param("days", "30"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to retrieve CO2 summary"));
    }

    @Test
    void getCo2ReportPdf_success() throws Exception {
        Long supplierId = 1L;

        Co2SummaryDto summary = new Co2SummaryDto();
        summary.setDays(30);
        summary.setFrom(LocalDateTime.now().minusDays(30));
        summary.setTo(LocalDateTime.now());
        summary.setTotalCo2Kg(new BigDecimal("1.000"));
        summary.setTotalWeightKg(new BigDecimal("0.500"));
        summary.setCategories(List.of());

        Mockito.when(co2AnalyticsService.getCo2Summary(supplierId, 30)).thenReturn(summary);
        Mockito.when(co2AnalyticsService.generateCo2ReportPdf(summary, supplierId)).thenReturn(new byte[] { 1, 2, 3 });

        mockMvc.perform(get("/api/analytics/supplier/{supplierId}/co2/report", supplierId)
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment;")));
    }

    @Test
    void getCo2ReportPdf_exception() throws Exception {
        Long supplierId = 1L;

        Mockito.when(co2AnalyticsService.getCo2Summary(supplierId, 30))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/analytics/supplier/{supplierId}/co2/report", supplierId)
                        .param("days", "30"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to generate CO2 report PDF"));
    }
}
