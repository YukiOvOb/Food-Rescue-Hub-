package com.frh.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.frh.backend.dto.StoreRecommendationDTO;
import com.frh.backend.service.RecommendationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RecommendationService recommendationService;

  /* --------------------------------
  HOMEPAGE RECOMMENDATIONS – SUCCESS
  -------------------------------- */
  @Test
  void getHomepageRecommendations_success() throws Exception {

    StoreRecommendationDTO dto = new StoreRecommendationDTO();
    dto.setStoreId(1L);
    dto.setStoreName("Bakery Store");

    Mockito.when(
            recommendationService.recommendStoresForHomepage(
                Mockito.eq(1L), Mockito.eq(5), Mockito.any(), Mockito.any()))
        .thenReturn(List.of(dto));

    mockMvc
        .perform(
            get("/api/recommendations/homepage")
                .param("consumerId", "1")
                .param("topK", "5")
                .param("lat", "1.3521")
                .param("lng", "103.8198"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.consumerId").value(1))
        .andExpect(jsonPath("$.count").value(1))
        .andExpect(jsonPath("$.message").value("推荐获取成功"));
  }

  /* --------------------------------
  HOMEPAGE RECOMMENDATIONS – FAILURE
  -------------------------------- */
  @Test
  void getHomepageRecommendations_failure() throws Exception {

    Mockito.when(
            recommendationService.recommendStoresForHomepage(
                Mockito.anyLong(), Mockito.anyInt(), Mockito.any(), Mockito.any()))
        .thenThrow(new RuntimeException("ML service error"));

    mockMvc
        .perform(get("/api/recommendations/homepage").param("consumerId", "1"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("推荐获取失败"));
  }

  @Test
  void getHomepageRecommendations_notFound_returns404() throws Exception {
    Mockito.when(
            recommendationService.recommendStoresForHomepage(
                Mockito.anyLong(), Mockito.anyInt(), Mockito.any(), Mockito.any()))
        .thenThrow(new RuntimeException("consumer not found"));

    mockMvc
        .perform(get("/api/recommendations/homepage").param("consumerId", "1"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("推荐获取失败"));
  }

  /* --------------------------------
  SEARCH WITH RECOMMENDATIONS – SUCCESS
  -------------------------------- */
  @Test
  void searchWithRecommendations_success() throws Exception {

    StoreRecommendationDTO dto = new StoreRecommendationDTO();
    dto.setStoreId(2L);
    dto.setStoreName("Bread Shop");

    Mockito.when(
            recommendationService.searchWithRecommendations(
                Mockito.eq(1L), Mockito.eq("bread"), Mockito.eq(10), Mockito.any(), Mockito.any()))
        .thenReturn(List.of(dto));

    mockMvc
        .perform(
            get("/api/recommendations/search")
                .param("consumerId", "1")
                .param("query", "bread")
                .param("topK", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.query").value("bread"))
        .andExpect(jsonPath("$.count").value(1))
        .andExpect(jsonPath("$.message").value("Search recommendations retrieved successfully"));
  }

  /* --------------------------------
  SEARCH WITH RECOMMENDATIONS – FAILURE
  -------------------------------- */
  @Test
  void searchWithRecommendations_failure() throws Exception {

    Mockito.when(
            recommendationService.searchWithRecommendations(
                Mockito.anyLong(),
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenThrow(new RuntimeException("Search error"));

    mockMvc
        .perform(
            get("/api/recommendations/search").param("consumerId", "1").param("query", "bread"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Failed to retrieve search recommendations"));
  }

  @Test
  void searchWithRecommendations_notFound_returns404() throws Exception {

    Mockito.when(
            recommendationService.searchWithRecommendations(
                Mockito.anyLong(),
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenThrow(new RuntimeException("listing not found"));

    mockMvc
        .perform(
            get("/api/recommendations/search").param("consumerId", "1").param("query", "bread"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Failed to retrieve search recommendations"));
  }

  /* --------------------------------
  HEALTH CHECK
  -------------------------------- */
  @Test
  void healthCheck_success() throws Exception {

    mockMvc
        .perform(get("/api/recommendations/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.service").value("Recommendation API"));
  }
}
