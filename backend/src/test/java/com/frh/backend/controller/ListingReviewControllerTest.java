package com.frh.backend.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frh.backend.dto.CreateListingReviewRequest;
import com.frh.backend.dto.ListingReviewResponse;
import com.frh.backend.service.ListingReviewService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ListingReviewController.class)
class ListingReviewControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ListingReviewService listingReviewService;

  @Test
  void getReviewsByListing_success() throws Exception {
    ListingReviewResponse response = sampleResponse();
    Mockito.when(listingReviewService.getReviewsByListing(100L)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/api/reviews/listing/{listingId}", 100L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].reviewId").value(9L))
        .andExpect(jsonPath("$[0].listingId").value(100L));
  }

  @Test
  void createReview_withoutConsumerSession_returnsUnauthorized() throws Exception {
    CreateListingReviewRequest request = sampleRequest();

    mockMvc
        .perform(
            post("/api/reviews")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("User not authorised"));
  }

  @Test
  void createReview_withConsumerSession_returnsCreated() throws Exception {
    CreateListingReviewRequest request = sampleRequest();
    ListingReviewResponse response = sampleResponse();
    MockHttpSession session = consumerSession(10L);

    Mockito.when(listingReviewService.createReview(10L, request)).thenReturn(response);

    mockMvc
        .perform(
            post("/api/reviews")
                .session(session)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reviewId").value(9L))
        .andExpect(jsonPath("$.orderId").value(1L));
  }

  @Test
  void deleteReview_withConsumerSession_returnsOk() throws Exception {
    MockHttpSession session = consumerSession(10L);

    mockMvc
        .perform(delete("/api/reviews/{reviewId}", 9L).session(session))
        .andExpect(status().isOk());

    Mockito.verify(listingReviewService).deleteReview(9L, 10L);
  }

  private MockHttpSession consumerSession(Long consumerId) {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("USER_ID", consumerId);
    session.setAttribute("USER_ROLE", "CONSUMER");
    return session;
  }

  private CreateListingReviewRequest sampleRequest() {
    CreateListingReviewRequest request = new CreateListingReviewRequest();
    request.setOrderId(1L);
    request.setListingId(100L);
    request.setRating(5);
    request.setComment("Excellent");
    return request;
  }

  private ListingReviewResponse sampleResponse() {
    ListingReviewResponse response = new ListingReviewResponse();
    response.setReviewId(9L);
    response.setOrderId(1L);
    response.setListingId(100L);
    response.setRating(5);
    response.setComment("Excellent");
    response.setCreatedAt(LocalDateTime.now());
    response.setConsumerId(10L);
    response.setConsumerDisplayName("Alex");
    return response;
  }
}
